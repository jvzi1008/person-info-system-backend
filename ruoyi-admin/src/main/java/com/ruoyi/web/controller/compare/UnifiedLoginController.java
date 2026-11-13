package com.ruoyi.web.controller.compare;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpUser;
import com.ruoyi.compare.domain.dto.ExtLoginBody;
import com.ruoyi.compare.domain.dto.ExtLoginUser;
import com.ruoyi.compare.service.IBizIpUserService;
import com.ruoyi.compare.service.IExtAuthService;
import com.ruoyi.compare.utils.ExtIpUtils;
import com.ruoyi.framework.web.service.SysLoginService;

@RestController
@RequestMapping("/api/unified")
public class UnifiedLoginController {

    @Autowired
    private SysLoginService sysLoginService;

    @Autowired
    private IBizIpUserService bizIpUserService;

    @Autowired
    private IExtAuthService extAuthService;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody body, HttpServletRequest request) {
        String loginName = body.getUsername() == null ? null : body.getUsername().trim();
        if (StringUtils.isEmpty(loginName) || StringUtils.isEmpty(body.getPassword())) {
            return AjaxResult.error("账号和密码不能为空");
        }

        BizIpUser extUser = bizIpUserService.selectBizIpUserByLoginName(loginName);
        if (extUser == null && loginName.indexOf('@') >= 0) {
            extUser = bizIpUserService.selectBizIpUserByLoginName(loginName.toLowerCase());
        }

        if (extUser != null) {
            ExtLoginBody extBody = new ExtLoginBody();
            extBody.setLoginName(loginName);
            extBody.setPassword(body.getPassword());
            ExtLoginUser loginUser = extAuthService.login(extBody, ExtIpUtils.getClientIp(request));
            AjaxResult ajax = AjaxResult.success("登录成功");
            ajax.put("type", "ext");
            ajax.put("token", loginUser.getToken());
            ajax.put("user", loginUser);
            return ajax;
        }

        String token = sysLoginService.login(body.getUsername(), body.getPassword(), body.getCode(), body.getUuid());
        AjaxResult ajax = AjaxResult.success("登录成功");
        ajax.put("type", "admin");
        ajax.put("token", token);
        return ajax;
    }
}
