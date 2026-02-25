package com.fuzhou.pojo.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminUserVO {


        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String name;

    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;

        /**
         * 账号
         */
        private String account;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 状态（0-禁用，1-普通用户，2-管理员）
         */
        private Integer status;

        /**
         * 头像URL
         */
        private String image;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        private String city;
}
