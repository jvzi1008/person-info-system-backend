package com.ruoyi.compare.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;
import com.ruoyi.compare.domain.BizIpUser;
import com.ruoyi.compare.domain.dto.ExtAddIpBody;
import com.ruoyi.compare.domain.dto.ExtEmailRegisterBody;
import com.ruoyi.compare.domain.dto.ExtLoginBody;
import com.ruoyi.compare.domain.dto.ExtLoginUser;
import com.ruoyi.compare.domain.dto.ExtRegisterBody;
import com.ruoyi.compare.exception.ExtAuthException;
import com.ruoyi.compare.service.ExtTokenService;
import com.ruoyi.compare.service.IBizIpAuthService;
import com.ruoyi.compare.service.IBizIpUserService;
import com.ruoyi.compare.service.IExtAuthService;
import com.ruoyi.compare.utils.ExtIpUtils;

@Service
public class ExtAuthServiceImpl implements IExtAuthService {

    private static final int MAX_IPS_PER_USER = 10;

    @Autowired
    private IBizIpUserService bizIpUserService;

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @Autowired
    private ExtTokenService extTokenService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(ExtRegisterBody body, String clientIp) {
        validateLoginName(body.getLoginName());
        validatePassword(body.getPassword());
        if (StringUtils.isEmpty(body.getUserName())) {
            throw new ExtAuthException(400, "姓名不能为空");
        }

        String ip = ExtIpUtils.normalize(clientIp);
        if (!ExtIpUtils.isValidIpOrCidr(ip)) {
            throw new ExtAuthException(400, "无法识别客户端IP");
        }

        String loginName = body.getLoginName().trim();
        if (bizIpUserService.selectBizIpUserByLoginName(loginName) != null) {
            throw new ExtAuthException(409, "登录账号已存在");
        }

        BizIpUser user = new BizIpUser();
        user.setLoginName(loginName);
        user.setPassword(body.getPassword());
        user.setUserName(body.getUserName().trim());
        user.setUserType(StringUtils.isEmpty(body.getUserType()) ? "2" : body.getUserType());
        user.setOrgName(body.getOrgName());
        user.setContactPhone(body.getContactPhone());
        user.setContactEmail(body.getContactEmail());
        user.setBindIp(ip);
        user.setStatus("0");
        user.setDelFlag("0");
        user.setCreateBy(loginName);
        user.setRemark(body.getRemark());
        bizIpUserService.insertBizIpUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerByEmail(ExtEmailRegisterBody body, String clientIp) {
        String email = body.getEmail() == null ? null : body.getEmail().trim().toLowerCase();
        validateEmail(email);
        validatePassword(body.getPassword());

        String ip = ExtIpUtils.normalize(clientIp);
        if (!ExtIpUtils.isValidIpOrCidr(ip)) {
            throw new ExtAuthException(400, "无法识别客户端IP");
        }

        if (bizIpUserService.selectBizIpUserByLoginName(email) != null) {
            throw new ExtAuthException(409, "邮箱已注册");
        }

        BizIpUser user = new BizIpUser();
        user.setLoginName(email);
        user.setPassword(body.getPassword());
        user.setUserName(email.substring(0, email.indexOf('@')));
        user.setUserType("2");
        user.setContactEmail(email);
        user.setBindIp(ip);
        user.setStatus("0");
        user.setDelFlag("0");
        user.setCreateBy(email);
        bizIpUserService.insertBizIpUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExtLoginUser login(ExtLoginBody body, String clientIp) {
        if (StringUtils.isEmpty(body.getLoginName()) || StringUtils.isEmpty(body.getPassword())) {
            throw new ExtAuthException(400, "账号和密码不能为空");
        }

        BizIpUser user = bizIpUserService.selectBizIpUserByLoginName(body.getLoginName().trim());
        if (user == null && body.getLoginName().indexOf('@') >= 0) {
            user = bizIpUserService.selectBizIpUserByLoginName(body.getLoginName().trim().toLowerCase());
        }
        if (user == null || StringUtils.isEmpty(user.getPassword())
                || !SecurityUtils.matchesPassword(body.getPassword(), user.getPassword())) {
            throw new ExtAuthException(401, "账号或密码错误");
        }

        if (!"0".equals(user.getStatus())) {
            throw new ExtAuthException(403, "账号已停用");
        }

        String ip = ExtIpUtils.normalize(clientIp);
        if (!ExtIpUtils.isValidIpOrCidr(ip)) {
            throw new ExtAuthException(400, "无法识别客户端IP");
        }

        List<BizIpAuth> activeList = bizIpAuthService.selectActiveByUserId(user.getId());
        if (activeList.isEmpty()) {
            bindFirstLoginIp(user, ip);
        } else if (!bizIpAuthService.isIpAuthorized(user.getId(), ip)) {
            throw new ExtAuthException(403, "当前IP未授权，请在已绑定IP上登录或联系管理员添加IP");
        }

        bizIpUserService.updateLoginInfo(user.getId(), ip, new Date());
        return createLoginUser(user, ip);
    }

    @Override
    public void logout(String token) {
        extTokenService.delete(token);
    }

    @Override
    public BizIpUser getUserInfo(Long userId) {
        BizIpUser user = bizIpUserService.selectBizIpUserById(userId);
        if (user == null) {
            throw new ExtAuthException(401, "用户不存在或已停用");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public List<BizIpAuth> listIps(Long userId) {
        BizIpAuth query = new BizIpAuth();
        query.setUserId(userId);
        return bizIpAuthService.selectBizIpAuthList(query);
    }

    @Override
    public void addIp(Long userId, String loginName, ExtAddIpBody body) {
        if (StringUtils.isEmpty(body.getIpAddress())) {
            throw new ExtAuthException(400, "IP地址不能为空");
        }

        String ip = ExtIpUtils.normalize(body.getIpAddress());
        if (!ExtIpUtils.isValidIpOrCidr(ip)) {
            throw new ExtAuthException(400, "IP地址格式不正确");
        }

        List<BizIpAuth> activeList = bizIpAuthService.selectActiveByUserId(userId);
        if (activeList.size() >= MAX_IPS_PER_USER) {
            throw new ExtAuthException(400, "每个用户最多绑定" + MAX_IPS_PER_USER + "个有效IP");
        }

        if (bizIpAuthService.selectByUserIdAndIp(userId, ip) != null) {
            throw new ExtAuthException(409, "该IP已绑定");
        }

        BizIpAuth auth = new BizIpAuth();
        auth.setUserId(userId);
        auth.setIpAddress(ip);
        auth.setSourceType("2");
        auth.setAuthType("1");
        auth.setStatus("0");
        auth.setEffectiveTime(body.getEffectiveTime() == null ? new Date() : body.getEffectiveTime());
        auth.setExpireTime(body.getExpireTime());
        auth.setMaxQps(body.getMaxQps() == null ? 20 : body.getMaxQps());
        auth.setCreateBy(loginName);
        auth.setRemark(body.getRemark());
        bizIpAuthService.insertBizIpAuth(auth);
    }

    @Override
    public void deleteIp(Long userId, Long authId) {
        BizIpAuth auth = bizIpAuthService.selectBizIpAuthById(authId);
        if (auth == null || !userId.equals(auth.getUserId())) {
            throw new ExtAuthException(404, "IP授权记录不存在");
        }

        if ("0".equals(auth.getStatus())) {
            List<BizIpAuth> activeList = bizIpAuthService.selectActiveByUserId(userId);
            if (activeList.size() <= 1) {
                throw new ExtAuthException(400, "至少保留一个有效IP，不能删除最后一个");
            }
        }

        bizIpAuthService.deleteBizIpAuthByIds(new Long[] { authId });
    }

    private void bindFirstLoginIp(BizIpUser user, String ip) {
        BizIpAuth existing = bizIpAuthService.selectByUserIdAndIp(user.getId(), ip);
        if (existing != null) {
            existing.setStatus("0");
            existing.setEffectiveTime(new Date());
            existing.setExpireTime(null);
            existing.setUpdateBy(user.getLoginName());
            bizIpAuthService.updateBizIpAuth(existing);
        } else {
            BizIpAuth auth = new BizIpAuth();
            auth.setUserId(user.getId());
            auth.setIpAddress(ip);
            auth.setSourceType("1");
            auth.setAuthType("1");
            auth.setStatus("0");
            auth.setEffectiveTime(new Date());
            auth.setMaxQps(20);
            auth.setCreateBy(user.getLoginName());
            auth.setRemark("首次登录绑定IP");
            bizIpAuthService.insertBizIpAuth(auth);
        }

        BizIpUser patch = new BizIpUser();
        patch.setId(user.getId());
        patch.setBindIp(ip);
        patch.setUpdateBy(user.getLoginName());
        bizIpUserService.updateBizIpUser(patch);
        user.setBindIp(ip);
    }

    private ExtLoginUser createLoginUser(BizIpUser user, String loginIp) {
        ExtLoginUser loginUser = new ExtLoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setLoginName(user.getLoginName());
        loginUser.setUserName(user.getUserName());
        loginUser.setUserType(user.getUserType());
        loginUser.setLoginIp(loginIp);
        loginUser.setLoginTime(new Date());
        extTokenService.createToken(loginUser);
        return loginUser;
    }

    private void validateEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            throw new ExtAuthException(400, "邮箱不能为空");
        }
        if (email.length() > 64) {
            throw new ExtAuthException(400, "邮箱长度不能超过64位");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ExtAuthException(400, "邮箱格式不正确");
        }
    }

    private void validateLoginName(String loginName) {
        if (StringUtils.isEmpty(loginName)) {
            throw new ExtAuthException(400, "登录账号不能为空");
        }
        if (!loginName.trim().matches("^[a-zA-Z0-9_]{4,32}$")) {
            throw new ExtAuthException(400, "登录账号必须为4-32位字母、数字或下划线");
        }
    }

    private void validatePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            throw new ExtAuthException(400, "密码不能为空");
        }
        if (password.length() < 8 || password.length() > 20) {
            throw new ExtAuthException(400, "密码长度必须为8-20位");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new ExtAuthException(400, "密码必须同时包含字母和数字");
        }
    }
}
