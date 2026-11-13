package com.ruoyi.web.controller.compare;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.compare.domain.dto.ExtAddIpBody;
import com.ruoyi.compare.domain.dto.ExtEmailRegisterBody;
import com.ruoyi.compare.domain.dto.ExtLoginBody;
import com.ruoyi.compare.domain.dto.ExtLoginUser;
import com.ruoyi.compare.domain.dto.ExtRegisterBody;
import com.ruoyi.compare.service.IExtAuthService;
import com.ruoyi.compare.utils.ExtIpUtils;
import com.ruoyi.web.interceptor.ExtAuthInterceptor;

@RestController
@RequestMapping("/api/ext/auth")
public class ExtAuthController {

    @Autowired
    private IExtAuthService extAuthService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody ExtRegisterBody body, HttpServletRequest request) {
        String clientIp = ExtIpUtils.getClientIp(request);
        extAuthService.register(body, clientIp);
        return AjaxResult.success("注册成功，请登录");
    }

    @PostMapping("/register/email")
    public AjaxResult registerByEmail(@RequestBody ExtEmailRegisterBody body, HttpServletRequest request) {
        String clientIp = ExtIpUtils.getClientIp(request);
        extAuthService.registerByEmail(body, clientIp);
        return AjaxResult.success("注册成功，请使用邮箱登录");
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestBody ExtLoginBody body, HttpServletRequest request) {
        String clientIp = ExtIpUtils.getClientIp(request);
        ExtLoginUser loginUser = extAuthService.login(body, clientIp);
        AjaxResult ajax = AjaxResult.success("登录成功");
        ajax.put("token", loginUser.getToken());
        ajax.put("user", loginUser);
        return ajax;
    }

    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request) {
        extAuthService.logout(ExtIpUtils.getToken(request));
        return AjaxResult.success("退出成功");
    }

    @GetMapping("/me")
    public AjaxResult me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(ExtAuthInterceptor.ATTR_USER_ID);
        return AjaxResult.success(extAuthService.getUserInfo(userId));
    }

    @GetMapping("/ips")
    public AjaxResult ips(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(ExtAuthInterceptor.ATTR_USER_ID);
        return AjaxResult.success(extAuthService.listIps(userId));
    }

    @PostMapping("/ip")
    public AjaxResult addIp(@RequestBody ExtAddIpBody body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(ExtAuthInterceptor.ATTR_USER_ID);
        String loginName = (String) request.getAttribute(ExtAuthInterceptor.ATTR_LOGIN_NAME);
        extAuthService.addIp(userId, loginName, body);
        return AjaxResult.success("IP添加成功");
    }

    @DeleteMapping("/ip/{id}")
    public AjaxResult deleteIp(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(ExtAuthInterceptor.ATTR_USER_ID);
        extAuthService.deleteIp(userId, id);
        return AjaxResult.success("IP删除成功");
    }
}
