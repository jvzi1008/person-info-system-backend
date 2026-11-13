package com.ruoyi.web.controller.compare;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;
import com.ruoyi.compare.service.IBizIpAuthService;

@RestController
@RequestMapping("/compare/ipAuth")
public class BizIpAuthController extends BaseController {

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizIpAuth bizIpAuth) {
        startPage();
        List<BizIpAuth> list = bizIpAuthService.selectBizIpAuthList(bizIpAuth);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bizIpAuthService.selectBizIpAuthById(id));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:add')")
    @Log(title = "IP授权", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizIpAuth bizIpAuth) {
        if (bizIpAuth.getUserId() == null) {
            return error("用户ID不能为空");
        }
        if (StringUtils.isEmpty(bizIpAuth.getIpAddress())) {
            return error("IP地址不能为空");
        }
        BizIpAuth exist = bizIpAuthService.selectByUserIdAndIp(bizIpAuth.getUserId(), bizIpAuth.getIpAddress());
        if (exist != null) {
            return error("该用户已绑定该IP");
        }
        if (bizIpAuthService.selectActiveByUserId(bizIpAuth.getUserId()).size() >= 10) {
            return error("每个用户最多绑定10个有效IP");
        }
        return toAjax(bizIpAuthService.insertBizIpAuth(bizIpAuth));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:edit')")
    @Log(title = "IP授权", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizIpAuth bizIpAuth) {
        return toAjax(bizIpAuthService.updateBizIpAuth(bizIpAuth));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:remove')")
    @Log(title = "IP授权", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bizIpAuthService.deleteBizIpAuthByIds(ids));
    }
}
