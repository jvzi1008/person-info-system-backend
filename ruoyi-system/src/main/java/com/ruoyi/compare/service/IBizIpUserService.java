package com.ruoyi.compare.service;

import java.util.Date;
import java.util.List;
import com.ruoyi.compare.domain.BizIpUser;

public interface IBizIpUserService {
    List<BizIpUser> selectBizIpUserList(BizIpUser bizIpUser);

    BizIpUser selectBizIpUserById(Long id);

    BizIpUser selectBizIpUserByLoginName(String loginName);

    int insertBizIpUser(BizIpUser bizIpUser);

    int updateBizIpUser(BizIpUser bizIpUser);

    int deleteBizIpUserByIds(Long[] ids);

    int updateLoginInfo(Long id, String lastLoginIp, Date lastLoginTime);
}
