package com.ruoyi.compare.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 比对访问日志归档
 */
public interface BizCompareAccessLogArchiveMapper {

    /**
     * 查询早于截止时间的日志ID（分批）
     */
    List<Long> selectExpiredIds(@Param("cutoff") Date cutoff, @Param("limit") int limit);

    /**
     * 按ID批量写入归档表
     */
    int archiveByIds(@Param("ids") List<Long> ids);

    /**
     * 按ID批量从主表删除
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 统计早于截止时间的日志数量
     */
    int countExpired(@Param("cutoff") Date cutoff);
}
