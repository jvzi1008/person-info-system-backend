package com.ruoyi.web.interceptor;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpUser;
import com.ruoyi.compare.domain.dto.ExtLoginUser;
import com.ruoyi.compare.service.ExtTokenService;
import com.ruoyi.compare.service.IBizIpAuthService;
import com.ruoyi.compare.service.IBizIpUserService;
import com.ruoyi.compare.utils.ExtIpUtils;

@Component
public class ExtAuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "extUserId";
    public static final String ATTR_LOGIN_NAME = "extLoginName";

    @Autowired
    private ExtTokenService extTokenService;

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @Autowired
    private IBizIpUserService bizIpUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = ExtIpUtils.getToken(request);
        if (StringUtils.isEmpty(token)) {
            return writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "未登录或Token缺失");
        }

        ExtLoginUser loginUser = extTokenService.getLoginUser(token);
        if (loginUser == null) {
            return writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "登录已过期，请重新登录");
        }

        String currentIp = ExtIpUtils.getClientIp(request);
        if (StringUtils.isEmpty(currentIp) || !currentIp.equals(loginUser.getLoginIp())) {
            return writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "登录IP已变化，请重新登录");
        }

        if (!bizIpAuthService.isIpAuthorized(loginUser.getUserId(), currentIp)) {
            return writeJson(response, HttpServletResponse.SC_FORBIDDEN, "当前IP未授权");
        }

        BizIpUser user = bizIpUserService.selectBizIpUserById(loginUser.getUserId());
        if (user == null || !"0".equals(user.getStatus())) {
            return writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "账号已停用或不存在");
        }

        request.setAttribute(ATTR_USER_ID, loginUser.getUserId());
        request.setAttribute(ATTR_LOGIN_NAME, loginUser.getLoginName());
        extTokenService.refresh(token);
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
