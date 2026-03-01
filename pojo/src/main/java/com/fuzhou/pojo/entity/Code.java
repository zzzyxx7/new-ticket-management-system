package com.fuzhou.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    private Integer id;
    private String code;             // 验证码
    private String userEmail;        // 用户邮箱
    private Boolean used;            // 是否已使用
    private LocalDateTime expireTime;// 过期时间
}
