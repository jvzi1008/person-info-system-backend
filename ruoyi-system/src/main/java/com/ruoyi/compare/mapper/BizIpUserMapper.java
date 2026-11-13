package com.ruoyi.compare.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.compare.domain.BizIpUser;

public interface BizIpUserMapper {
    BizIpUser selectBizIpUserById(Long id);

    BizIpUser selectBizIpUserByLoginName(String loginName);

    List<BizIpUser> selectBizIpUserList(BizIpUser bizIpUser);

    int insertBizIpUser(BizIpUser bizIpUser);

    int updateBizIpUser(BizIpUser bizIpUser);

    int deleteBizIpUserByIds(Long[] ids);

    int updateLoginInfo(@Param("id") Long id,
                        @Param("lastLoginIp") String lastLoginIp,
                        @Param("lastLoginTime") Date lastLoginTime);
}
