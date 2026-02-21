package com.fuzhou.server.service;

/**
 * IP定位服务接口
 * 根据IP地址获取城市信息
 */
public interface IpLocationService {
    
    /**
     * 根据IP地址获取城市名称
     * 
     * @param ip IP地址
     * @return 城市名称，如"北京市"、"上海市"，获取失败返回null
     */
    String getCityByIp(String ip);
}

