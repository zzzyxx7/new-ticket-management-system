package com.fuzhou.pojo.dto;
import lombok.Data;

@Data
public class OrderPageQueryDTO {
    private Integer page = 1;          // 页码
    private Integer pageSize = 10;       // 每页数量
    private Integer orderStatus;    // 订单状态（可选筛选）
    private String orderNo;         // 订单号（可选筛选，模糊查询）
    private Long userId;            // 用户ID（可选筛选）
    private String beginTime;       // 开始时间（可选筛选）
    private String endTime;         // 结束时间（可选筛选）
}