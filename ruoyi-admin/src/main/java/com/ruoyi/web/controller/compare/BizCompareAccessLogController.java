package com.ruoyi.web.controller.compare;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.compare.domain.BizCompareAccessLog;
import com.ruoyi.compare.service.CompareLogArchiveService;
import com.ruoyi.compare.service.IBizCompareAccessLogService;

@RestController
@RequestMapping("/compare/accessLog")
public class BizCompareAccessLogController extends BaseController {

    @Autowired
    private IBizCompareAccessLogService bizCompareAccessLogService;

    @Autowired
    private CompareLogArchiveService compareLogArchiveService;

    @PreAuthorize("@ss.hasPermi('compare:accessLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizCompareAccessLog log) {
        startPage();
        List<BizCompareAccessLog> list = bizCompareAccessLogService.selectBizCompareAccessLogList(log);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('compare:accessLog:export')")
    @Log(title = "比对访问日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BizCompareAccessLog log) {
        List<BizCompareAccessLog> list = bizCompareAccessLogService.selectBizCompareAccessLogList(log);
        ExcelUtil<BizCompareAccessLog> util = new ExcelUtil<BizCompareAccessLog>(BizCompareAccessLog.class);
        util.exportExcel(response, list, "比对访问日志");
    }

    /**
     * 待归档日志统计（超过保留天数的条数）
     */
    @PreAuthorize("@ss.hasPermi('compare:accessLog:list')")
    @GetMapping("/archiveInfo")
    public AjaxResult archiveInfo() {
        return success().put("retentionDays", compareLogArchiveService.getRetentionDays())
                .put("expired", compareLogArchiveService.countExpired());
    }

    /**
     * 手动触发归档
     */
    @PreAuthorize("@ss.hasPermi('compare:accessLog:remove')")
    @Log(title = "比对访问日志归档", businessType = BusinessType.OTHER)
    @PostMapping("/archive")
    public AjaxResult archive() {
        int moved = compareLogArchiveService.archiveOldLogs();
        return success("已归档 " + moved + " 条日志");
    }

    @PreAuthorize("@ss.hasPermi('compare:accessLog:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bizCompareAccessLogService.selectBizCompareAccessLogById(id));
    }

    @PreAuthorize("@ss.hasPermi('compare:accessLog:remove')")
    @Log(title = "比对访问日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bizCompareAccessLogService.deleteBizCompareAccessLogByIds(ids));
    }
}
