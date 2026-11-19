package com.ruoyi.compare.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;

/**
 * 普通用户首次登录自动绑定 IP（免审）
 */
@Service
public class NormalUserLoginService {

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @Transactional(rollbackFor = Exception.class)
    public void bindFirstLoginIp(Long userId, String loginName, String loginIp) {
        if (userId == null || StringUtils.isEmpty(loginIp)) {
            return;
        }

        BizIpAuth query = new BizIpAuth();
        query.setUserId(userId);
        List<BizIpAuth> list = bizIpAuthService.selectBizIpAuthList(query);
        for (BizIpAuth auth : list) {
            if ("0".equals(auth.getStatus()) && "1".equals(auth.getAuditStatus())) {
                return;
            }
        }

        BizIpAuth auth = new BizIpAuth();
        auth.setUserId(userId);
        auth.setIpAddress(loginIp);
        auth.setSourceType("1");
        auth.setAuthType("1");
        auth.setStatus("0");
        auth.setAuditStatus("1");
        auth.setAuditBy("system");
        auth.setAuditTime(new Date());
        auth.setAuditRemark("首次登录绑定IP");
        // 立即生效：置空避免 now() 秒级截断导致刚绑定被判未生效
        auth.setEffectiveTime(null);
        auth.setMaxQps(20);
        auth.setCreateBy(loginName);
        bizIpAuthService.insertBizIpAuth(auth);
    }
}
