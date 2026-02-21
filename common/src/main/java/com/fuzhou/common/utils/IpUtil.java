package com.fuzhou.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * IP工具类：从HttpServletRequest中获取客户端真实IP地址
 */
@Slf4j
public class IpUtil {

    /**
     * 获取客户端真实IP地址
     * 考虑代理服务器的情况（Nginx、负载均衡等）
     *
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = null;

        ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            if (isValidIp(ip)) {
                return ip;
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip) && isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip) && isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip) && isValidIp(ip)) {
            return ip;
        }

        ip = request.getRemoteAddr();
        if (StringUtils.hasText(ip)) {
            if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                return "127.0.0.1";
            }
            return ip;
        }

        return "127.0.0.1";
    }

    private static boolean isValidIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }
        String ipPattern = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";
        return ip.matches(ipPattern);
    }
}
