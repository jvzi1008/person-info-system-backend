package com.ruoyi.compare.mapper;

import java.util.List;
import com.ruoyi.compare.domain.BizCompareAccessLog;

public interface BizCompareAccessLogMapper {
    BizCompareAccessLog selectBizCompareAccessLogById(Long id);

    List<BizCompareAccessLog> selectBizCompareAccessLogList(BizCompareAccessLog log);

    int insertBizCompareAccessLog(BizCompareAccessLog log);

    int deleteBizCompareAccessLogByIds(Long[] ids);
}
