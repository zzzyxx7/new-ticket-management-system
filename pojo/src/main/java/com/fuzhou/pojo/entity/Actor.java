package com.fuzhou.pojo.entity;
import lombok.Data;

@Data
public class Actor {
    private Long id;
    private String name;             // 演员姓名
    private Integer popularity;      // 人气值
}
