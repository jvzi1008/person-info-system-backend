package com.ruoyi.web.controller.compare;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;
import com.ruoyi.compare.service.IBizIpAuthService;
import com.ruoyi.system.service.ISysUserService;

@RestController
@RequestMapping("/compare/ipAuth")
public class BizIpAuthController extends BaseController {

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @Autowired
    private ISysUserService sysUserService;

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizIpAuth bizIpAuth) {
        startPage();
        List<BizIpAuth> list = bizIpAuthService.selectBizIpAuthList(bizIpAuth);
        return getDataTable(list);
    }

    /**
     * IP 授权页面的用户下拉：只返回已通过审核的普通用户
     */
    @PreAuthorize("@ss.hasPermi('compare:ipAuth:list')")
    @GetMapping("/userOptions")
    public AjaxResult userOptions() {
        SysUser query = new SysUser();
        query.setUserType("01");
        query.setStatus("0");
        // 只列已通过审核的普通用户（pendingUser 无法登录，无需分配 IP）
        query.setAuditStatus("1");
        query.getParams().put("dataScope", "");
        return success(sysUserService.selectUserList(query));
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
        // 管理员新增的 IP 默认已通过审核
        if (StringUtils.isEmpty(bizIpAuth.getAuditStatus())) {
            bizIpAuth.setAuditStatus("1");
        }
        bizIpAuth.setAuditBy(getUsername());
        bizIpAuth.setAuditTime(new Date());
        if (StringUtils.isEmpty(bizIpAuth.getStatus())) {
            bizIpAuth.setStatus("0");
        }
        return toAjax(bizIpAuthService.insertBizIpAuth(bizIpAuth));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:edit')")
    @Log(title = "IP授权", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizIpAuth bizIpAuth) {
        return toAjax(bizIpAuthService.updateBizIpAuth(bizIpAuth));
    }

    /**
     * 审核用户自助添加的 IP（1通过 2驳回）
     */
    @PreAuthorize("@ss.hasPermi('compare:ipAuth:edit')")
    @Log(title = "IP授权审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody BizIpAuth body) {
        if (body.getId() == null) {
            return error("ID不能为空");
        }
        if (!"1".equals(body.getAuditStatus()) && !"2".equals(body.getAuditStatus())) {
            return error("审核状态不正确");
        }
        if ("2".equals(body.getAuditStatus()) && StringUtils.isEmpty(body.getAuditRemark())) {
            return error("驳回必须填写原因");
        }
        BizIpAuth update = new BizIpAuth();
        update.setId(body.getId());
        update.setAuditStatus(body.getAuditStatus());
        update.setAuditBy(getUsername());
        update.setAuditTime(new Date());
        update.setAuditRemark(body.getAuditRemark());
        update.setStatus("1".equals(body.getAuditStatus()) ? "0" : "1");
        update.setUpdateBy(getUsername());
        return toAjax(bizIpAuthService.updateBizIpAuth(update));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipAuth:remove')")
    @Log(title = "IP授权", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bizIpAuthService.deleteBizIpAuthByIds(ids));
    }
}
