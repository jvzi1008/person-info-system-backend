package com.ruoyi.compare.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.compare.domain.dto.ExtLoginUser;

@Service
public class ExtTokenService {

    private static final String TOKEN_PREFIX = "ext:login:";
    private static final int EXPIRE_MINUTES = 30;

    @Autowired
    private RedisCache redisCache;

    public String createToken(ExtLoginUser loginUser) {
        String token = UUID.randomUUID().toString().replace("-", "");
        loginUser.setToken(token);
        redisCache.setCacheObject(TOKEN_PREFIX + token, loginUser, EXPIRE_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    public ExtLoginUser getLoginUser(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        Object obj = redisCache.getCacheObject(TOKEN_PREFIX + token);
        if (obj instanceof ExtLoginUser) {
            return (ExtLoginUser) obj;
        }
        return null;
    }

    public void refresh(String token) {
        if (StringUtils.isNotEmpty(token)) {
            redisCache.expire(TOKEN_PREFIX + token, EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void delete(String token) {
        if (StringUtils.isNotEmpty(token)) {
            redisCache.deleteObject(TOKEN_PREFIX + token);
        }
    }
}
