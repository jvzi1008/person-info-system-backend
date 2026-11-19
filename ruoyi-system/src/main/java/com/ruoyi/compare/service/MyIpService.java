package com.ruoyi.compare.service;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;
import com.ruoyi.compare.domain.dto.ExtAddIpBody;
import com.ruoyi.compare.exception.ExtAuthException;
import com.ruoyi.compare.utils.ExtIpUtils;

/**
 * 普通用户「我的 IP 授权」自助管理
 */
@Service
public class MyIpService {

    private static final int MAX_IPS_PER_USER = 10;

    @Autowired
    private IBizIpAuthService bizIpAuthService;

    public List<BizIpAuth> list(Long userId) {
        BizIpAuth query = new BizIpAuth();
        query.setUserId(userId);
        return bizIpAuthService.selectBizIpAuthList(query);
    }

    public void add(Long userId, String loginName, ExtAddIpBody body) {
        if (StringUtils.isEmpty(body.getIpAddress())) {
            throw new ExtAuthException(400, "IP地址不能为空");
        }

        String ip = ExtIpUtils.normalize(body.getIpAddress());
        if (!ExtIpUtils.isValidIpOrCidr(ip)) {
            throw new ExtAuthException(400, "IP地址格式不正确");
        }

        List<BizIpAuth> activeList = bizIpAuthService.selectActiveByUserId(userId);
        if (activeList.size() >= MAX_IPS_PER_USER) {
            throw new ExtAuthException(400, "每个用户最多绑定" + MAX_IPS_PER_USER + "个有效IP");
        }

        BizIpAuth query = new BizIpAuth();
        query.setUserId(userId);
        int pending = 0;
        for (BizIpAuth item : bizIpAuthService.selectBizIpAuthList(query)) {
            if ("0".equals(item.getAuditStatus())) {
                pending++;
            }
        }
        if (pending >= 3) {
            throw new ExtAuthException(400, "待审核的 IP 最多 3 条，请等待管理员处理");
        }

        if (bizIpAuthService.selectByUserIdAndIp(userId, ip) != null) {
            throw new ExtAuthException(409, "该IP已绑定");
        }

        BizIpAuth auth = new BizIpAuth();
        auth.setUserId(userId);
        auth.setIpAddress(ip);
        auth.setSourceType("2");
        auth.setAuthType("1");
        auth.setStatus("0");
        auth.setAuditStatus("0");
        auth.setAuditRemark("用户自助添加，待管理员审核");
        auth.setEffectiveTime(body.getEffectiveTime() == null ? new Date() : body.getEffectiveTime());
        auth.setExpireTime(body.getExpireTime());
        auth.setMaxQps(body.getMaxQps() == null ? 20 : body.getMaxQps());
        auth.setCreateBy(loginName);
        auth.setRemark(body.getRemark());
        bizIpAuthService.insertBizIpAuth(auth);
    }

    public void delete(Long userId, Long authId) {
        BizIpAuth auth = bizIpAuthService.selectBizIpAuthById(authId);
        if (auth == null || !userId.equals(auth.getUserId())) {
            throw new ExtAuthException(404, "IP授权记录不存在");
        }

        if ("0".equals(auth.getStatus())) {
            List<BizIpAuth> activeList = bizIpAuthService.selectActiveByUserId(userId);
            if (activeList.size() <= 1) {
                throw new ExtAuthException(400, "至少保留一个有效IP，不能删除最后一个");
            }
        }

        bizIpAuthService.deleteBizIpAuthByIds(new Long[] { authId });
    }
}
