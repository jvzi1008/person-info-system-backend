package com.ruoyi.web.controller.compare;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.compare.domain.dto.CompareQueryBody;
import com.ruoyi.compare.domain.vo.CompareResult;
import com.ruoyi.compare.service.ICompareService;
import com.ruoyi.compare.utils.ExtIpUtils;
import com.ruoyi.web.interceptor.ExtAuthInterceptor;

@RestController
@RequestMapping("/api/ext/compare")
public class ExtCompareController {

    @Autowired
    private ICompareService compareService;

    @PostMapping("/person")
    public AjaxResult comparePerson(@RequestBody CompareQueryBody body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(ExtAuthInterceptor.ATTR_USER_ID);
        String loginName = (String) request.getAttribute(ExtAuthInterceptor.ATTR_LOGIN_NAME);
        String clientIp = ExtIpUtils.getClientIp(request);

        CompareResult result = compareService.compare(
                body,
                userId,
                loginName,
                clientIp,
                request.getRequestURI(),
                request.getMethod(),
                request.getHeader("User-Agent")
        );
        return AjaxResult.success(result);
    }
}
