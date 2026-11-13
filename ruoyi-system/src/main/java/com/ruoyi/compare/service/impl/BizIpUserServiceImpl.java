package com.ruoyi.compare.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.BizIpUser;
import com.ruoyi.compare.mapper.BizIpAuthMapper;
import com.ruoyi.compare.mapper.BizIpUserMapper;
import com.ruoyi.compare.service.IBizIpUserService;

@Service
public class BizIpUserServiceImpl implements IBizIpUserService {

    @Autowired
    private BizIpUserMapper bizIpUserMapper;

    @Autowired
    private BizIpAuthMapper bizIpAuthMapper;

    @Override
    public List<BizIpUser> selectBizIpUserList(BizIpUser bizIpUser) {
        return bizIpUserMapper.selectBizIpUserList(bizIpUser);
    }

    @Override
    public BizIpUser selectBizIpUserById(Long id) {
        return bizIpUserMapper.selectBizIpUserById(id);
    }

    @Override
    public BizIpUser selectBizIpUserByLoginName(String loginName) {
        return bizIpUserMapper.selectBizIpUserByLoginName(loginName);
    }

    @Override
    public int insertBizIpUser(BizIpUser bizIpUser) {
        BizIpUser exist = bizIpUserMapper.selectBizIpUserByLoginName(bizIpUser.getLoginName());
        if (exist != null) {
            throw new ServiceException("登录账号已存在");
        }
        bizIpUser.setPassword(encodePassword(bizIpUser.getPassword()));
        bizIpUser.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(bizIpUser.getCreateBy())) {
            bizIpUser.setCreateBy("system");
        }
        if (StringUtils.isEmpty(bizIpUser.getStatus())) {
            bizIpUser.setStatus("0");
        }
        if (StringUtils.isEmpty(bizIpUser.getDelFlag())) {
            bizIpUser.setDelFlag("0");
        }
        return bizIpUserMapper.insertBizIpUser(bizIpUser);
    }

    @Override
    public int updateBizIpUser(BizIpUser bizIpUser) {
        if (StringUtils.isNotEmpty(bizIpUser.getPassword())) {
            bizIpUser.setPassword(encodePassword(bizIpUser.getPassword()));
        } else {
            bizIpUser.setPassword(null);
        }
        bizIpUser.setUpdateTime(DateUtils.getNowDate());
        return bizIpUserMapper.updateBizIpUser(bizIpUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBizIpUserByIds(Long[] ids) {
        bizIpAuthMapper.disableByUserIds(ids);
        return bizIpUserMapper.deleteBizIpUserByIds(ids);
    }

    @Override
    public int updateLoginInfo(Long id, String lastLoginIp, Date lastLoginTime) {
        return bizIpUserMapper.updateLoginInfo(id, lastLoginIp, lastLoginTime);
    }

    private String encodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return password;
        }
        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            return password;
        }
        return SecurityUtils.encryptPassword(password);
    }
}
