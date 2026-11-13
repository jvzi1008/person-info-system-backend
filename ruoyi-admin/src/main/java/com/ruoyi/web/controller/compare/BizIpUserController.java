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
import com.ruoyi.compare.domain.BizIpUser;
import com.ruoyi.compare.service.IBizIpUserService;

@RestController
@RequestMapping("/compare/ipUser")
public class BizIpUserController extends BaseController {

    @Autowired
    private IBizIpUserService bizIpUserService;

    @PreAuthorize("@ss.hasPermi('compare:ipUser:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizIpUser bizIpUser) {
        startPage();
        List<BizIpUser> list = bizIpUserService.selectBizIpUserList(bizIpUser);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('compare:ipUser:list')")
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        return success(bizIpUserService.selectBizIpUserList(new BizIpUser()));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipUser:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bizIpUserService.selectBizIpUserById(id));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipUser:add')")
    @Log(title = "接入用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizIpUser bizIpUser) {
        if (StringUtils.isEmpty(bizIpUser.getLoginName())) {
            return error("登录账号不能为空");
        }
        if (StringUtils.isEmpty(bizIpUser.getPassword())) {
            return error("密码不能为空");
        }
        if (StringUtils.isEmpty(bizIpUser.getBindIp())) {
            return error("绑定IP不能为空");
        }
        return toAjax(bizIpUserService.insertBizIpUser(bizIpUser));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipUser:edit')")
    @Log(title = "接入用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizIpUser bizIpUser) {
        return toAjax(bizIpUserService.updateBizIpUser(bizIpUser));
    }

    @PreAuthorize("@ss.hasPermi('compare:ipUser:remove')")
    @Log(title = "接入用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bizIpUserService.deleteBizIpUserByIds(ids));
    }
}
