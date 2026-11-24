package com.ruoyi.compare.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;

/**
 * 普通用户登录时的 IP 处理。
 *
 * <p>1) 当前 IP 已授权：什么都不做
 * <p>2) 一条有效 IP 都没有（首次登录）：绑定当前 IP，免审
 * <p>3) 有有效 IP 但当前 IP 不在其中（换网络）：自动提交一条待审记录，管理员通过后生效
 */
@Service
public class NormalUserLoginService {

    /** 来源：1 首次登录 2 自助添加 3 管理员添加 4 登录IP变更 */
    private static final String SOURCE_LOGIN_IP_CHANGE = "4";

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    @Transactional(rollbackFor = Exception.class)
    public void handleLoginIp(Long userId, String loginName, String loginIp)
    {
        if (userId == null || StringUtils.isEmpty(loginIp))
        {
            return;
        }
        if (bizIpAuthService.isIpAuthorized(userId, loginIp))
        {
            return;
        }

        BizIpAuth query = new BizIpAuth();
        query.setUserId(userId);
        List<BizIpAuth> list = bizIpAuthService.selectBizIpAuthList(query);

        boolean hasValid = false;
        for (BizIpAuth auth : list)
        {
            if ("0".equals(auth.getStatus()) && "1".equals(auth.getAuditStatus()))
            {
                hasValid = true;
                break;
            }
        }

        if (!hasValid)
        {
            insertFirstLoginIp(userId, loginName, loginIp);
            return;
        }

        // 同一 IP 已有记录（待审或停用）就不再重复提交，避免每次登录都写一条
        if (bizIpAuthService.selectByUserIdAndIp(userId, loginIp) != null)
        {
            return;
        }

        BizIpAuth pending = new BizIpAuth();
        pending.setUserId(userId);
        pending.setIpAddress(loginIp);
        pending.setSourceType(SOURCE_LOGIN_IP_CHANGE);
        pending.setAuthType("1");
        pending.setStatus("0");
        pending.setAuditStatus("0");
        pending.setAuditRemark("登录IP变更，系统自动提交待审核");
        pending.setEffectiveTime(null);
        pending.setMaxQps(20);
        pending.setCreateBy(loginName);
        bizIpAuthService.insertBizIpAuth(pending);
    }

    /**
     * 首次登录绑定当前 IP（免审，立即生效）
     */
    private void insertFirstLoginIp(Long userId, String loginName, String loginIp)
    {
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
