package com.ruoyi.web.controller.compare;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysLogininfor;
import com.ruoyi.system.service.ISysLogininforService;

/**
 * 普通用户「我的登录日志」
 *
 * 只返回当前登录账号的记录；账号名一律取自登录态，忽略前端传入，避免越权查看他人日志。
 */
@RestController
@RequestMapping("/compare/my/loginLog")
public class MyLoginLogController extends BaseController {

    @Autowired
    private ISysLogininforService logininforService;

    @GetMapping
    public TableDataInfo list(SysLogininfor logininfor) {
        startPage();
        // 强制覆盖为当前登录账号，精确匹配
        logininfor.setUserName(SecurityUtils.getUsername());
        List<SysLogininfor> list = logininforService.selectMyLogininforList(logininfor);
        return getDataTable(list);
    }
}
