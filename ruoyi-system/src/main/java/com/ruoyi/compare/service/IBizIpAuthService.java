package com.ruoyi.compare.service;

import java.util.List;
import com.ruoyi.compare.domain.BizIpAuth;

public interface IBizIpAuthService {
    List<BizIpAuth> selectBizIpAuthList(BizIpAuth bizIpAuth);

    BizIpAuth selectBizIpAuthById(Long id);

    List<BizIpAuth> selectActiveByUserId(Long userId);

    BizIpAuth selectByUserIdAndIp(Long userId, String ipAddress);

    int insertBizIpAuth(BizIpAuth bizIpAuth);

    int updateBizIpAuth(BizIpAuth bizIpAuth);

    int deleteBizIpAuthByIds(Long[] ids);

    int deleteBizIpAuthByUserId(Long userId);

    boolean isIpAuthorized(Long userId, String ipAddress);
}
