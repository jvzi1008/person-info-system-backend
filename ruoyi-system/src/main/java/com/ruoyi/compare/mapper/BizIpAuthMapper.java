package com.ruoyi.compare.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.compare.domain.BizIpAuth;

public interface BizIpAuthMapper {
    BizIpAuth selectBizIpAuthById(Long id);

    List<BizIpAuth> selectBizIpAuthList(BizIpAuth bizIpAuth);

    List<BizIpAuth> selectActiveByUserId(Long userId);

    BizIpAuth selectByUserIdAndIp(@Param("userId") Long userId,
                                  @Param("ipAddress") String ipAddress);

    int insertBizIpAuth(BizIpAuth bizIpAuth);

    int updateBizIpAuth(BizIpAuth bizIpAuth);

    int deleteBizIpAuthByIds(Long[] ids);

    int deleteBizIpAuthByUserId(Long userId);

    int disableByUserIds(Long[] userIds);
}
