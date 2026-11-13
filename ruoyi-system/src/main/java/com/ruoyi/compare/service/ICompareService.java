package com.ruoyi.compare.service;

import com.ruoyi.compare.domain.dto.CompareQueryBody;
import com.ruoyi.compare.domain.vo.CompareResult;

public interface ICompareService {

    CompareResult compare(CompareQueryBody body,
                          Long userId,
                          String loginName,
                          String clientIp,
                          String requestUri,
                          String requestMethod,
                          String userAgent);
}
