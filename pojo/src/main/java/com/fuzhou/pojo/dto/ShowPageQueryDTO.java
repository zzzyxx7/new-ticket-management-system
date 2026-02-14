package com.fuzhou.pojo.dto;

import lombok.Data;

/**
 * 管理端演出分页查询DTO
 */
@Data
public class ShowPageQueryDTO {
    /**
     * 页码（默认第1页）
     */
    private Integer page = 1;

    /**
     * 每页记录数（默认10条）
     */
    private Integer pageSize = 10;

    /**
     * 演出名称（模糊查询）
     */
    private String title;

    /**
     * 城市（精确查询）
     */
    private String city;

    /**
     * 分类ID（精确查询）
     */
    private Long sortId;

    /**
     * 场馆名称（模糊查询）
     */
    private String venueName;
}



















