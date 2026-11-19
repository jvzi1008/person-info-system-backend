package com.ruoyi.compare.service;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.compare.mapper.BizCompareAccessLogArchiveMapper;

/**
 * 比对访问日志归档：超过保留天数的记录搬入 biz_compare_access_log_archive。
 *
 * 默认每天 03:30 执行，保留 90 天，可通过 application.yml 调整：
 * <pre>
 * compare:
 *   log:
 *     enabled: true
 *     retention-days: 90
 *     archive-cron: 0 30 3 * * ?
 * </pre>
 */
@Service
public class CompareLogArchiveService {

    private static final Logger log = LoggerFactory.getLogger(CompareLogArchiveService.class);

    /** 每批处理条数，避免长事务锁表 */
    private static final int BATCH_SIZE = 1000;

    @Autowired
    private BizCompareAccessLogArchiveMapper archiveMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Value("${compare.log.retention-days:90}")
    private int retentionDays;

    @Value("${compare.log.enabled:true}")
    private boolean enabled;

    @Scheduled(cron = "${compare.log.archive-cron:0 30 3 * * ?}")
    public void scheduledArchive() {
        if (!enabled) {
            return;
        }
        try {
            int moved = archiveOldLogs();
            if (moved > 0) {
                log.info("比对访问日志归档完成，共归档 {} 条（保留 {} 天）", moved, retentionDays);
            }
        } catch (Exception e) {
            log.error("比对访问日志归档失败", e);
        }
    }

    /**
     * 归档早于保留期的日志，返回归档条数。
     */
    public int archiveOldLogs() {
        if (retentionDays <= 0) {
            return 0;
        }
        Date cutoff = DateUtils.addDays(DateUtils.getNowDate(), -retentionDays);
        int total = 0;
        while (true) {
            List<Long> ids = archiveMapper.selectExpiredIds(cutoff, BATCH_SIZE);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            Integer moved = transactionTemplate.execute(status -> {
                archiveMapper.archiveByIds(ids);
                return archiveMapper.deleteByIds(ids);
            });
            total += moved == null ? 0 : moved;
        }
        return total;
    }

    /**
     * 统计待归档条数，供管理端展示。
     */
    public int countExpired() {
        if (retentionDays <= 0) {
            return 0;
        }
        Date cutoff = DateUtils.addDays(DateUtils.getNowDate(), -retentionDays);
        return archiveMapper.countExpired(cutoff);
    }

    public int getRetentionDays() {
        return retentionDays;
    }
}
