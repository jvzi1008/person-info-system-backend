package com.ruoyi.compare.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpAuth;
import com.ruoyi.compare.mapper.BizIpAuthMapper;
import com.ruoyi.compare.service.IBizIpAuthService;
import com.ruoyi.compare.utils.ExtIpUtils;

@Service
public class BizIpAuthServiceImpl implements IBizIpAuthService {

    @Autowired
    private BizIpAuthMapper bizIpAuthMapper;

    @Override
    public List<BizIpAuth> selectBizIpAuthList(BizIpAuth bizIpAuth) {
        return bizIpAuthMapper.selectBizIpAuthList(bizIpAuth);
    }

    @Override
    public BizIpAuth selectBizIpAuthById(Long id) {
        return bizIpAuthMapper.selectBizIpAuthById(id);
    }

    @Override
    public List<BizIpAuth> selectActiveByUserId(Long userId) {
        return bizIpAuthMapper.selectActiveByUserId(userId);
    }

    @Override
    public BizIpAuth selectByUserIdAndIp(Long userId, String ipAddress) {
        return bizIpAuthMapper.selectByUserIdAndIp(userId, ipAddress);
    }

    @Override
    public int insertBizIpAuth(BizIpAuth bizIpAuth) {
        bizIpAuth.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(bizIpAuth.getCreateBy())) {
            bizIpAuth.setCreateBy("system");
        }
        if (StringUtils.isEmpty(bizIpAuth.getSourceType())) {
            bizIpAuth.setSourceType("3");
        }
        if (StringUtils.isEmpty(bizIpAuth.getAuthType())) {
            bizIpAuth.setAuthType("1");
        }
        if (StringUtils.isEmpty(bizIpAuth.getStatus())) {
            bizIpAuth.setStatus("0");
        }
        if (bizIpAuth.getMaxQps() == null) {
            bizIpAuth.setMaxQps(20);
        }
        return bizIpAuthMapper.insertBizIpAuth(bizIpAuth);
    }

    @Override
    public int updateBizIpAuth(BizIpAuth bizIpAuth) {
        bizIpAuth.setUpdateTime(DateUtils.getNowDate());
        return bizIpAuthMapper.updateBizIpAuth(bizIpAuth);
    }

    @Override
    public int deleteBizIpAuthByIds(Long[] ids) {
        return bizIpAuthMapper.deleteBizIpAuthByIds(ids);
    }

    @Override
    public int deleteBizIpAuthByUserId(Long userId) {
        return bizIpAuthMapper.deleteBizIpAuthByUserId(userId);
    }

    @Override
    public boolean isIpAuthorized(Long userId, String ipAddress) {
        if (userId == null || StringUtils.isEmpty(ipAddress)) {
            return false;
        }
        String currentIp = ExtIpUtils.normalize(ipAddress);
        List<BizIpAuth> authList = bizIpAuthMapper.selectActiveByUserId(userId);
        for (BizIpAuth auth : authList) {
            if (ExtIpUtils.matches(auth.getIpAddress(), currentIp)) {
                return true;
            }
        }
        return false;
    }
}
