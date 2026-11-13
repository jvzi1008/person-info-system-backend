package com.ruoyi.compare.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizCompareAccessLog;
import com.ruoyi.compare.mapper.BizCompareAccessLogMapper;
import com.ruoyi.compare.service.IBizCompareAccessLogService;

@Service
public class BizCompareAccessLogServiceImpl implements IBizCompareAccessLogService {

    @Autowired
    private BizCompareAccessLogMapper bizCompareAccessLogMapper;

    @Override
    public List<BizCompareAccessLog> selectBizCompareAccessLogList(BizCompareAccessLog log) {
        return bizCompareAccessLogMapper.selectBizCompareAccessLogList(log);
    }

    @Override
    public BizCompareAccessLog selectBizCompareAccessLogById(Long id) {
        return bizCompareAccessLogMapper.selectBizCompareAccessLogById(id);
    }

    @Override
    public int insertBizCompareAccessLog(BizCompareAccessLog log) {
        Date now = DateUtils.getNowDate();
        if (log.getRequestTime() == null) {
            log.setRequestTime(now);
        }
        if (log.getMatchCount() == null) {
            log.setMatchCount(0);
        }
        if (StringUtils.isEmpty(log.getConflictFlag())) {
            log.setConflictFlag("0");
        }
        if (StringUtils.isEmpty(log.getNeedReview())) {
            log.setNeedReview("0");
        }
        return bizCompareAccessLogMapper.insertBizCompareAccessLog(log);
    }

    @Override
    public int deleteBizCompareAccessLogByIds(Long[] ids) {
        return bizCompareAccessLogMapper.deleteBizCompareAccessLogByIds(ids);
    }
}
