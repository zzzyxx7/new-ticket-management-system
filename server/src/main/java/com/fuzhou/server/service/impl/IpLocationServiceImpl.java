package com.fuzhou.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fuzhou.server.service.IpLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * IP定位服务实现
 * 使用免费的IP定位API：ip-api.com
 * 注意：免费版有请求频率限制（45次/分钟），生产环境建议使用付费API或自建IP库
 */
@Service
@Slf4j
public class IpLocationServiceImpl implements IpLocationService {

    // 免费IP定位API（ip-api.com），返回JSON格式
    private static final String IP_API_URL = "http://ip-api.com/json/%s?lang=zh-CN&fields=status,message,city";
    
    @Override
    public String getCityByIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            log.warn("IP地址为空，无法定位");
            return null;
        }
        
        // 本地IP不调用API
        if ("127.0.0.1".equals(ip) || "localhost".equals(ip) || "::1".equals(ip)) {
            log.info("本地IP：{}，返回默认城市：北京市", ip);
            return "北京市";
        }
        
        try {
            String url = String.format(IP_API_URL, ip);
            log.info("调用IP定位API：{}", url);
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000); // 3秒超时
            connection.setReadTimeout(3000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                log.warn("IP定位API调用失败，响应码：{}", responseCode);
                return null;
            }
            
            // 读取响应
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }
            
            // 解析JSON响应
            JSONObject jsonObject = JSONObject.parseObject(response.toString());
            String status = jsonObject.getString("status");
            
            if (!"success".equals(status)) {
                String message = jsonObject.getString("message");
                log.warn("IP定位失败，IP：{}，错误信息：{}", ip, message);
                return null;
            }
            
            String city = jsonObject.getString("city");
            if (StringUtils.hasText(city)) {
                // 确保城市名称格式统一（如"北京" -> "北京市"）
                String formattedCity = formatCityName(city);
                log.info("IP定位成功，IP：{}，城市：{}", ip, formattedCity);
                return formattedCity;
            }
            
            log.warn("IP定位API返回的城市为空，IP：{}", ip);
            return null;
            
        } catch (Exception e) {
            log.error("IP定位异常，IP：{}，错误：{}", ip, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 格式化城市名称，确保与数据库中的城市名称格式一致
     * 例如："北京" -> "北京市"
     */
    private String formatCityName(String city) {
        if (!StringUtils.hasText(city)) {
            return city;
        }
        
        // 如果城市名称不包含"市"、"省"、"自治区"等后缀，统一添加"市"
        if (!city.contains("市") && !city.contains("省") && !city.contains("自治区") 
            && !city.contains("特别行政区") && !city.contains("县")) {
            return city + "市";
        }
        
        return city;
    }
}

