package com.ruoyi.web.controller.compare;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.compare.domain.dto.CompareQueryBody;
import com.ruoyi.compare.domain.vo.CompareResult;
import com.ruoyi.compare.service.ICompareService;
import com.ruoyi.compare.utils.ExtIpUtils;

@RestController
@RequestMapping("/compare/person")
public class BizCompareController extends BaseController {

    @Autowired
    private ICompareService compareService;

    @PreAuthorize("@ss.hasPermi('compare:person:query')")
    @PostMapping
    public AjaxResult compare(@RequestBody CompareQueryBody body, HttpServletRequest request) {
        CompareResult result = compareService.compare(
                body,
                getUserId(),
                getUsername(),
                ExtIpUtils.getClientIp(request),
                request.getRequestURI(),
                request.getMethod(),
                request.getHeader("User-Agent")
        );
        return success(result);
    }
}
