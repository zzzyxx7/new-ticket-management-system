package com.fuzhou.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// 用 scanBasePackages 统一指定所有需要扫描的包，覆盖默认规则
// 排除 UserDetailsServiceAutoConfiguration：项目用 JWT 认证，不需要 Spring 生成的默认密码，避免启动时 WARN
@SpringBootApplication(
        scanBasePackages = {
                "com.fuzhou.server",    // 包含 server 下的所有子包（config、controller、service 等）
                "com.fuzhou.common"     // 包含 common 下的 Bean（MailUtils 等）
        },
        exclude = { UserDetailsServiceAutoConfiguration.class }
)
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        System.out.println("提示：启动成功");
    }

}