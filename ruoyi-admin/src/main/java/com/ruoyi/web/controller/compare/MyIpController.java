package com.ruoyi.web.controller.compare;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.compare.domain.dto.ExtAddIpBody;
import com.ruoyi.compare.service.MyIpService;

/**
 * 普通用户「我的 IP 授权」
 */
@RestController
@RequestMapping("/compare/my/ip")
public class MyIpController extends BaseController {

    @Autowired
    private MyIpService myIpService;

    @GetMapping
    public AjaxResult list() {
        return success(myIpService.list(getUserId()));
    }

    @PostMapping
    public AjaxResult add(@RequestBody ExtAddIpBody body) {
        myIpService.add(getUserId(), getUsername(), body);
        return success("IP已提交，待管理员审核");
    }

    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        myIpService.delete(getUserId(), id);
        return success("IP删除成功");
    }
}
