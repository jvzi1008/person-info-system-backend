package com.ruoyi.web.interceptor;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.service.IBizIpAuthService;
import com.ruoyi.compare.utils.ExtIpUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * 普通用户 IP 白名单校验（管理员跳过）
 */
@Component
public class NormalUserIpInterceptor implements HandlerInterceptor {

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            return true;
        }

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        if (SecurityUtils.isAdmin(loginUser.getUserId())) {
            return true;
        }
        if (loginUser.getUser() != null && loginUser.getUser().getRoles() != null) {
            for (SysRole role : loginUser.getUser().getRoles()) {
                if ("admin".equals(role.getRoleKey())) {
                    return true;
                }
            }
        }

        // 只约束普通用户（user_type=01）；系统用户（00）是内部账号，不走 IP 白名单
        if (loginUser.getUser() == null || !"01".equals(loginUser.getUser().getUserType()))
        {
            return true;
        }

        String currentIp = ExtIpUtils.getClientIp(request);
        if (StringUtils.isEmpty(currentIp) || !currentIp.equals(loginUser.getIpaddr())) {
            return writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "登录IP已变化，请重新登录");
        }

        if (!bizIpAuthService.isIpAuthorized(loginUser.getUserId(), currentIp)) {
            return writeJson(response, HttpServletResponse.SC_FORBIDDEN, "当前IP未授权，请在「我的IP」提交申请或联系管理员");
        }
        return true;
    }

    private boolean writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(AjaxResult.error(status, message)));
        return false;
    }
}
