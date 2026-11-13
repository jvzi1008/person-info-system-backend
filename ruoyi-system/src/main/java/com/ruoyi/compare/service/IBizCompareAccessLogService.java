package com.ruoyi.compare.service;

import java.util.List;
import com.ruoyi.compare.domain.BizCompareAccessLog;

public interface IBizCompareAccessLogService {
    List<BizCompareAccessLog> selectBizCompareAccessLogList(BizCompareAccessLog log);

    BizCompareAccessLog selectBizCompareAccessLogById(Long id);

    int insertBizCompareAccessLog(BizCompareAccessLog log);

    int deleteBizCompareAccessLogByIds(Long[] ids);
}
