package com.ruoyi.compare.service;

import java.util.List;
import com.ruoyi.compare.domain.BizIpAuth;
import com.ruoyi.compare.domain.BizIpUser;
import com.ruoyi.compare.domain.dto.ExtAddIpBody;
import com.ruoyi.compare.domain.dto.ExtEmailRegisterBody;
import com.ruoyi.compare.domain.dto.ExtLoginBody;
import com.ruoyi.compare.domain.dto.ExtLoginUser;
import com.ruoyi.compare.domain.dto.ExtRegisterBody;

public interface IExtAuthService {

    void register(ExtRegisterBody body, String clientIp);

    void registerByEmail(ExtEmailRegisterBody body, String clientIp);

    ExtLoginUser login(ExtLoginBody body, String clientIp);

    void logout(String token);

    BizIpUser getUserInfo(Long userId);

    List<BizIpAuth> listIps(Long userId);

    void addIp(Long userId, String loginName, ExtAddIpBody body);

    void deleteIp(Long userId, Long authId);
}
