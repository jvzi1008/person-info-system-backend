package com.ruoyi.compare.utils;

import java.net.InetAddress;
import jakarta.servlet.http.HttpServletRequest;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;

public class ExtIpUtils {

    public static final String TOKEN_HEADER = "X-Ext-Token";

    private static final String IPV4_REGEX =
            "^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";

    private static final String IPV6_REGEX = "^[0-9a-fA-F:]+$";

    public static String getClientIp(HttpServletRequest request) {
        return normalize(IpUtils.getIpAddr(request));
    }

    public static String normalize(String ip) {
        if (StringUtils.isEmpty(ip)) {
            return ip;
        }
        ip = ip.trim();
        if (ip.startsWith("[") && ip.endsWith("]")) {
            ip = ip.substring(1, ip.length() - 1);
        }
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    public static boolean isValidIpOrCidr(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        String text = value.trim();
        String base = text;
        int prefix = -1;
        if (text.contains("/")) {
            String[] parts = text.split("/", 2);
            if (parts.length != 2) {
                return false;
            }
            base = parts[0];
            try {
                prefix = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                return false;
            }
        }
        if (!isIpLiteral(base)) {
            return false;
        }
        if (prefix >= 0) {
            try {
                int bits = InetAddress.getByName(base).getAddress().length * 8;
                return prefix >= 0 && prefix <= bits;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static boolean matches(String pattern, String ip) {
        if (StringUtils.isEmpty(pattern) || StringUtils.isEmpty(ip)) {
            return false;
        }
        String p = pattern.trim();
        String current = normalize(ip);
        if (!p.contains("/")) {
            return p.equalsIgnoreCase(current);
        }
        String[] parts = p.split("/", 2);
        if (parts.length != 2) {
            return false;
        }
        try {
            byte[] patternBytes = InetAddress.getByName(parts[0]).getAddress();
            byte[] ipBytes = InetAddress.getByName(current).getAddress();
            if (patternBytes.length != ipBytes.length) {
                return false;
            }
            int prefix = Integer.parseInt(parts[1]);
            if (prefix < 0 || prefix > patternBytes.length * 8) {
                return false;
            }
            int fullBytes = prefix / 8;
            int remainingBits = prefix % 8;
            for (int i = 0; i < fullBytes; i++) {
                if (patternBytes[i] != ipBytes[i]) {
                    return false;
                }
            }
            if (remainingBits > 0) {
                int mask = 0xFF << (8 - remainingBits);
                if ((patternBytes[fullBytes] & mask) != (ipBytes[fullBytes] & mask)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (StringUtils.isNotEmpty(token)) {
            return token.trim();
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(authorization) && authorization.startsWith("Ext ")) {
            return authorization.substring(4).trim();
        }
        return null;
    }

    private static boolean isIpLiteral(String value) {
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        if (value.contains(":")) {
            return value.matches(IPV6_REGEX) && value.indexOf(':') >= 0;
        }
        return value.matches(IPV4_REGEX);
    }
}
