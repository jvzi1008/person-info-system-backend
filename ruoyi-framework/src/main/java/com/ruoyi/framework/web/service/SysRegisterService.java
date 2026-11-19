package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.common.core.domain.entity.SysRole;
import java.util.List;

/**
 * 注册校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysRegisterService
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private com.ruoyi.system.mapper.SysRoleMapper roleMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody)
    {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (!username.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            msg = "邮箱格式不正确";
        }
        else if (username.length() > 64)
        {
            msg = "邮箱长度不能超过64位";
        }
        else if (password.length() < 8 || password.length() > 20)
        {
            msg = "密码长度必须为8-20位";
        }
        else if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*"))
        {
            msg = "密码必须同时包含字母和数字";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            String nickName = username.substring(0, username.indexOf('@'));
            if (nickName.length() > 30)
            {
                nickName = nickName.substring(0, 30);
            }
            sysUser.setNickName(nickName);
            sysUser.setEmail(username);
            sysUser.setUserType("01");
            sysUser.setStatus("0");
            sysUser.setAuditStatus("0");
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                bindNormalRole(sysUser.getUserId());
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, "注册成功，待管理员审核"));
            }
        }
        return msg;
    }

    /**
     * 绑定普通用户角色
     */
    private void bindNormalRole(Long userId)
    {
        if (userId == null)
        {
            return;
        }
        SysRole query = new SysRole();
        query.setRoleKey("normal");
        query.getParams().put("dataScope", "");
        List<SysRole> roles = roleMapper.selectRoleList(query);
        if (roles != null && !roles.isEmpty())
        {
            userService.insertUserAuth(userId, new Long[] { roles.get(0).getRoleId() });
        }
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}
