# 前后端分离项目完整部署指南

## 📋 目录
1. [部署架构说明](#一部署架构说明)
2. [服务器环境准备](#二服务器环境准备)
3. [安装基础软件](#三安装基础软件)
4. [数据库初始化](#四数据库初始化)
5. [后端部署](#五后端部署)
6. [Nginx配置](#六nginx配置)
7. [前端部署与接口调用](#七前端部署与接口调用)
8. [常见问题排查](#八常见问题排查)

---

## 一、部署架构说明

### 1.1 前后端分离架构

```
用户浏览器
    ↓
前端页面（HTML/CSS/JS）
    ↓ HTTP请求
Nginx（反向代理）
    ↓
后端API（Spring Boot，端口8080）
    ↓
MySQL数据库 + Redis缓存
```

### 1.2 为什么需要Nginx？

1. **反向代理**：前端通过Nginx访问后端API，避免跨域问题
2. **静态资源服务**：部署前端打包后的静态文件（HTML/CSS/JS）
3. **负载均衡**：如果有多台后端服务器，可以分发请求
4. **SSL证书**：配置HTTPS访问

### 1.3 接口调用流程

**前端调用接口的两种方式：**

**方式一：直接调用后端（开发环境）**
```
前端地址：http://localhost:3000
后端地址：http://localhost:8080
前端直接请求：http://localhost:8080/user/login
```

**方式二：通过Nginx代理（生产环境）**
```
前端地址：http://your-domain.com
后端地址：http://your-domain.com/api
前端请求：http://your-domain.com/api/user/login
Nginx自动转发到：http://localhost:8080/user/login
```

---

## 二、服务器环境准备

### 2.1 购买云服务器

推荐云服务商：
- **阿里云**：https://www.aliyun.com
- **腾讯云**：https://cloud.tencent.com
- **华为云**：https://www.huaweicloud.com

**最低配置建议：**
- CPU：2核
- 内存：4GB
- 硬盘：40GB SSD
- 带宽：3Mbps
- 操作系统：**CentOS 7.x** 或 **Ubuntu 20.04 LTS**

### 2.2 连接服务器

**Windows系统：**
1. 下载 **PuTTY** 或使用 **Windows Terminal**
2. 使用SSH连接：
   ```bash
   ssh root@你的服务器IP
   # 输入密码
   ```

**Mac/Linux系统：**
```bash
ssh root@你的服务器IP
# 输入密码
```

### 2.3 基础系统配置

```bash
# 1. 更新系统软件包
yum update -y  # CentOS
# 或
apt update && apt upgrade -y  # Ubuntu

# 2. 安装常用工具
yum install -y wget curl vim git  # CentOS
# 或
apt install -y wget curl vim git  # Ubuntu

# 3. 设置时区
timedatectl set-timezone Asia/Shanghai

# 4. 查看系统信息
uname -a
cat /etc/os-release
```

---

## 三、安装基础软件

### 3.1 安装Java 17

```bash
# CentOS 7
# 下载OpenJDK 17
cd /opt
wget https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz

# 解压
tar -xzf openjdk-17.0.2_linux-x64_bin.tar.gz
mv jdk-17.0.2 java17

# 配置环境变量
vim /etc/profile
# 在文件末尾添加：
export JAVA_HOME=/opt/java17
export PATH=$JAVA_HOME/bin:$PATH

# 使配置生效
source /etc/profile

# 验证安装
java -version
# 应该显示：openjdk version "17.0.2"
```

**Ubuntu 20.04：**
```bash
# 添加OpenJDK仓库
sudo apt install -y openjdk-17-jdk

# 验证
java -version
```

### 3.2 安装MySQL 8.0

```bash
# CentOS 7
# 下载MySQL官方YUM源
wget https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm
rpm -ivh mysql80-community-release-el7-3.noarch.rpm

# 安装MySQL
yum install -y mysql-server

# 启动MySQL
systemctl start mysqld
systemctl enable mysqld

# 获取临时密码
grep 'temporary password' /var/log/mysqld.log

# 安全配置（设置root密码）
mysql_secure_installation
```

**Ubuntu 20.04：**
```bash
# 安装MySQL
sudo apt install -y mysql-server

# 启动MySQL
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

**配置MySQL：**
```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE fuzhou CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户（可选，也可以直接用root）
CREATE USER 'damai'@'localhost' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON fuzhou.* TO 'damai'@'localhost';
FLUSH PRIVILEGES;

# 退出
exit;
```

### 3.3 安装Redis

```bash
# CentOS 7
# 安装EPEL源
yum install -y epel-release

# 安装Redis
yum install -y redis

# 启动Redis
systemctl start redis
systemctl enable redis

# 配置Redis密码（可选但推荐）
vim /etc/redis.conf
# 找到 # requirepass foobared，取消注释并修改：
requirepass 你的Redis密码

# 重启Redis
systemctl restart redis

# 测试连接
redis-cli
# 如果设置了密码，需要：
redis-cli -a 你的Redis密码
# 输入：PING，应该返回：PONG
```

**Ubuntu 20.04：**
```bash
# 安装Redis
sudo apt install -y redis-server

# 配置密码
sudo vim /etc/redis/redis.conf
# 找到 # requirepass foobared，修改为：
requirepass 你的Redis密码

# 重启Redis
sudo systemctl restart redis

# 测试
redis-cli -a 你的Redis密码
PING
```

### 3.4 安装Nginx

```bash
# CentOS 7
# 安装EPEL源（如果还没安装）
yum install -y epel-release

# 安装Nginx
yum install -y nginx

# 启动Nginx
systemctl start nginx
systemctl enable nginx

# 检查状态
systemctl status nginx

# 测试访问
curl http://localhost
```

**Ubuntu 20.04：**
```bash
# 安装Nginx
sudo apt install -y nginx

# 启动Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# 检查状态
sudo systemctl status nginx
```

**配置防火墙：**
```bash
# CentOS 7（firewalld）
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --reload

# Ubuntu（ufw）
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

### 3.5 安装Maven（用于打包，可选）

如果要在服务器上打包项目：

```bash
# CentOS 7
cd /opt
wget https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz
tar -xzf apache-maven-3.9.5-bin.tar.gz
mv apache-maven-3.9.5 maven

# 配置环境变量
vim /etc/profile
# 添加：
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH

source /etc/profile
mvn -version
```

---

## 四、数据库初始化

### 4.1 导入数据库结构

```bash
# 1. 将数据库SQL文件上传到服务器
# 方式一：使用scp（在本地电脑执行）
scp server/fuzhou.sql root@你的服务器IP:/root/

# 方式二：在服务器上直接下载（如果有Git仓库）
cd /root
wget https://你的Git仓库地址/fuzhou.sql

# 2. 导入数据库
mysql -u root -p fuzhou < /root/fuzhou.sql

# 3. 验证导入
mysql -u root -p
USE fuzhou;
SHOW TABLES;
# 应该能看到所有表
```

### 4.2 检查数据库表

```sql
-- 登录MySQL
mysql -u root -p

USE fuzhou;

-- 查看所有表
SHOW TABLES;

-- 查看某个表的结构
DESC user;
DESC `show`;
DESC sessions;
DESC `order`;

-- 退出
exit;
```

---

## 五、后端部署

### 5.1 本地打包项目

**在本地电脑（Windows/Mac）执行：**

```bash
# 进入项目根目录
cd D:\code\DaMaiPlatform

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 打包成功后，jar文件位置：
# server/target/server-0.0.1-SNAPSHOT.jar
```

### 5.2 创建生产环境配置文件

在 `server/src/main/resources/` 目录创建 `application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  profiles:
    active: prod
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/fuzhou?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: 你的MySQL密码
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: 你的Redis密码  # 如果设置了密码

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.fuzhou
  configuration:
    map-underscore-to-camel-case: true

logging:
  level:
    com.fuzhou: info
    root: info
  file:
    name: /opt/damai/logs/app.log

fuzhou:
  jwt:
    user-secret-key: 你的JWT密钥（生产环境请使用强密码，至少32位）
    user-ttl: 900000
    user-token-name: token
    refresh-secret-key: 你的刷新令牌密钥（生产环境请使用强密码）
    refresh-ttl: 86400000
  alioss:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
    bucket-name: 你的Bucket名称
  alipay:
    app-id: 你的支付宝AppId
    private-key: 你的支付宝私钥
    public-key: 你的支付宝公钥
    gateway: https://openapi.alipay.com/gateway.do
    return-url: https://your-domain.com/alipay/return
    notify-url: https://your-domain.com/alipay/notify
```

**重要：** 修改完配置后，重新打包：
```bash
mvn clean package -DskipTests
```

### 5.3 上传jar包到服务器

```bash
# 在本地电脑执行（Windows PowerShell或CMD）
scp server/target/server-0.0.1-SNAPSHOT.jar root@你的服务器IP:/opt/damai/

# 如果scp不可用，可以使用：
# 1. 使用FTP工具（FileZilla、WinSCP）
# 2. 使用云服务器控制台的文件上传功能
```

### 5.4 在服务器上创建目录结构

```bash
# 创建项目目录
mkdir -p /opt/damai
mkdir -p /opt/damai/logs

# 如果jar包还没上传，先创建目录
# 如果已上传，检查文件
ls -lh /opt/damai/
```

### 5.5 创建systemd服务（推荐）

```bash
# 创建服务文件
vim /etc/systemd/system/damai.service
```

**文件内容：**
```ini
[Unit]
Description=DaMai Platform Backend Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/damai
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar -Dspring.profiles.active=prod /opt/damai/server-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

**启动服务：**
```bash
# 重载systemd配置
systemctl daemon-reload

# 启动服务
systemctl start damai

# 设置开机自启
systemctl enable damai

# 查看状态
systemctl status damai

# 查看日志
journalctl -u damai -f
# 或查看应用日志
tail -f /opt/damai/logs/app.log
```

### 5.6 验证后端是否启动成功

```bash
# 1. 检查端口是否监听
netstat -tlnp | grep 8080
# 或
ss -tlnp | grep 8080

# 2. 测试接口（应该返回JSON）
curl http://localhost:8080/user/show
# 或
curl http://你的服务器IP:8080/user/show

# 3. 如果看到JSON响应，说明后端启动成功
```

---

## 六、Nginx配置

### 6.1 配置Nginx反向代理

```bash
# 编辑Nginx配置文件
vim /etc/nginx/nginx.conf
```

**在 `http { }` 块内添加：**

```nginx
# 后端API代理配置
upstream damai_backend {
    server localhost:8080;
    keepalive 64;
}

server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名，或使用服务器IP

    # 前端静态文件（如果前端也部署在这台服务器）
    location / {
        root /opt/damai/frontend;  # 前端打包后的文件目录
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://damai_backend/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 跨域配置（如果后端没有配置CORS）
        add_header Access-Control-Allow-Origin *;
        add_header Access-Control-Allow-Methods 'GET, POST, PUT, DELETE, OPTIONS';
        add_header Access-Control-Allow-Headers 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization,token';
        
        # 处理OPTIONS预检请求
        if ($request_method = 'OPTIONS') {
            return 204;
        }
    }

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|woff|woff2|ttf|svg)$ {
        root /opt/damai/frontend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

**如果只有后端API，简化配置：**

```nginx
upstream damai_backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    # 后端API代理（所有请求都转发到后端）
    location / {
        proxy_pass http://damai_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 6.2 测试并重启Nginx

```bash
# 测试配置文件语法
nginx -t

# 如果显示 "syntax is ok"，则重启Nginx
systemctl restart nginx

# 检查状态
systemctl status nginx
```

### 6.3 验证Nginx配置

```bash
# 测试API代理是否生效
curl http://localhost/api/user/show
# 或
curl http://你的服务器IP/api/user/show

# 应该返回和直接访问 http://localhost:8080/user/show 相同的结果
```

---

## 七、前端部署与接口调用

### 7.1 前端项目配置

**前端项目需要配置API基础地址：**

**Vue项目（vue.config.js 或 .env.production）：**
```javascript
// vue.config.js
module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://your-domain.com',  // 生产环境域名
        changeOrigin: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }
    }
  }
}

// 或 .env.production
VUE_APP_API_BASE_URL=http://your-domain.com/api
```

**React项目（.env.production）：**
```bash
REACT_APP_API_BASE_URL=http://your-domain.com/api
```

**Axios配置示例：**
```javascript
// api/request.js
import axios from 'axios'

const request = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://your-domain.com/api',
  timeout: 10000
})

// 请求拦截器（添加token）
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    return Promise.reject(error)
  }
)

export default request
```

### 7.2 前端打包

```bash
# Vue项目
npm run build
# 打包后的文件在 dist/ 目录

# React项目
npm run build
# 打包后的文件在 build/ 目录
```

### 7.3 部署前端静态文件

**方式一：部署到Nginx服务器（推荐）**

```bash
# 1. 上传前端打包文件到服务器
# 在本地执行
scp -r dist/* root@你的服务器IP:/opt/damai/frontend/

# 2. 设置权限
chmod -R 755 /opt/damai/frontend

# 3. 重启Nginx
systemctl restart nginx
```

**方式二：部署到CDN或对象存储（阿里云OSS、腾讯云COS等）**

1. 将打包后的文件上传到OSS/COS
2. 配置OSS/COS的静态网站托管
3. 绑定域名和SSL证书

### 7.4 前端如何调用接口

**开发环境（本地开发）：**
```javascript
// 直接调用后端
axios.get('http://localhost:8080/user/show')
```

**生产环境（部署后）：**
```javascript
// 方式一：通过Nginx代理（推荐）
axios.get('/api/user/show')
// 实际请求：http://your-domain.com/api/user/show
// Nginx转发到：http://localhost:8080/user/show

// 方式二：直接调用后端（需要配置CORS）
axios.get('http://your-domain.com:8080/user/show')
// 注意：需要后端配置允许跨域
```

### 7.5 接口调用示例

```javascript
// 登录接口
async function login(username, password) {
  const response = await axios.post('/api/user/login', {
    account: username,
    password: password
  })
  // 保存token
  localStorage.setItem('token', response.data.data.token)
  return response
}

// 获取演出列表
async function getShowList() {
  const response = await axios.get('/api/user/show', {
    params: {
      city: '北京市',
      page: 1,
      pageSize: 10
    }
  })
  return response
}

// 需要token的接口
async function getUserInfo() {
  const response = await axios.get('/api/user/info', {
    headers: {
      'token': localStorage.getItem('token')
    }
  })
  return response
}
```

---

## 八、常见问题排查

### 8.1 后端无法启动

**问题：端口被占用**
```bash
# 查看8080端口占用
netstat -tlnp | grep 8080
# 或
lsof -i:8080

# 杀死占用进程
kill -9 进程ID
```

**问题：无法连接MySQL**
```bash
# 检查MySQL是否运行
systemctl status mysql

# 检查MySQL端口
netstat -tlnp | grep 3306

# 测试连接
mysql -u root -p -h localhost
```

**问题：无法连接Redis**
```bash
# 检查Redis是否运行
systemctl status redis

# 测试连接
redis-cli -a 你的密码 ping
```

**问题：查看详细错误日志**
```bash
# 查看应用日志
tail -f /opt/damai/logs/app.log

# 查看systemd日志
journalctl -u damai -n 100 -f
```

### 8.2 前端无法调用接口

**问题：跨域错误（CORS）**

检查后端 `WebMvcConfiguration.java` 是否配置了跨域：
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("*")  // 生产环境建议改为具体域名
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .maxAge(3600);
}
```

**问题：404 Not Found**

1. 检查Nginx配置是否正确
2. 检查后端是否启动
3. 检查API路径是否正确（注意 `/api/` 前缀）

**问题：401 Unauthorized**

1. 检查token是否正确传递
2. 检查token是否过期
3. 检查JWT密钥配置是否正确

### 8.3 Nginx相关问题

**问题：502 Bad Gateway**

```bash
# 检查后端是否运行
systemctl status damai

# 检查后端端口
netstat -tlnp | grep 8080

# 检查Nginx错误日志
tail -f /var/log/nginx/error.log
```

**问题：403 Forbidden**

```bash
# 检查文件权限
ls -la /opt/damai/frontend

# 修改权限
chmod -R 755 /opt/damai/frontend
chown -R nginx:nginx /opt/damai/frontend
```

### 8.4 性能优化建议

**1. JVM参数优化**
```ini
# 修改 /etc/systemd/system/damai.service
ExecStart=/usr/bin/java -Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar ...
```

**2. Nginx缓存配置**
```nginx
# 在server块中添加
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=api_cache:10m max_size=100m inactive=60m;

location /api/ {
    proxy_cache api_cache;
    proxy_cache_valid 200 60m;
    proxy_cache_key "$scheme$request_method$host$request_uri";
    # ... 其他配置
}
```

**3. 数据库连接池优化**
在 `application-prod.yml` 中添加：
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

---

## 九、完整部署检查清单

### 部署前
- [ ] 服务器已购买并可以SSH连接
- [ ] 服务器防火墙已开放80、443、8080端口
- [ ] Java 17已安装并配置环境变量
- [ ] MySQL 8.0已安装并创建数据库
- [ ] Redis已安装并配置密码
- [ ] Nginx已安装并启动

### 数据库
- [ ] 数据库 `fuzhou` 已创建
- [ ] SQL文件已导入
- [ ] 数据库用户权限已配置
- [ ] 数据库连接测试通过

### 后端
- [ ] `application-prod.yml` 已创建并配置正确
- [ ] 项目已打包（`mvn clean package -DskipTests`）
- [ ] jar包已上传到服务器
- [ ] systemd服务已创建并启动
- [ ] 后端日志正常，无错误
- [ ] 接口测试通过（`curl http://localhost:8080/user/show`）

### Nginx
- [ ] Nginx配置文件语法正确（`nginx -t`）
- [ ] 反向代理配置正确
- [ ] Nginx已重启
- [ ] API代理测试通过（`curl http://localhost/api/user/show`）

### 前端
- [ ] 前端API基础地址已配置
- [ ] 前端项目已打包
- [ ] 静态文件已上传到服务器或CDN
- [ ] 前端页面可以正常访问
- [ ] 前端可以正常调用后端接口

### 安全
- [ ] JWT密钥已更换为强密码
- [ ] MySQL root密码已修改
- [ ] Redis密码已设置
- [ ] 服务器SSH密钥登录已配置（可选但推荐）
- [ ] 防火墙规则已配置

---

## 十、快速部署脚本（可选）

创建一键部署脚本 `deploy.sh`：

```bash
#!/bin/bash

# 后端部署脚本
echo "开始部署后端..."

# 停止旧服务
systemctl stop damai

# 备份旧jar包
cp /opt/damai/server-0.0.1-SNAPSHOT.jar /opt/damai/backup/server-0.0.1-SNAPSHOT.jar.$(date +%Y%m%d%H%M%S)

# 上传新jar包（需要手动上传或使用scp）
# scp server/target/server-0.0.1-SNAPSHOT.jar root@服务器IP:/opt/damai/

# 启动服务
systemctl start damai

# 等待启动
sleep 10

# 检查状态
systemctl status damai

echo "后端部署完成！"
```

使用：
```bash
chmod +x deploy.sh
./deploy.sh
```

---

## 十一、域名和SSL配置（可选）

### 11.1 配置域名解析

1. 在域名服务商（如阿里云、腾讯云）添加A记录：
   - 主机记录：`@` 或 `www`
   - 记录值：你的服务器IP
   - TTL：600

### 11.2 安装SSL证书（Let's Encrypt免费证书）

```bash
# 安装Certbot
yum install -y certbot python3-certbot-nginx  # CentOS
# 或
apt install -y certbot python3-certbot-nginx  # Ubuntu

# 申请证书
certbot --nginx -d your-domain.com

# 自动续期
certbot renew --dry-run
```

### 11.3 Nginx HTTPS配置

Certbot会自动修改Nginx配置，或手动配置：

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    # ... 其他配置
}

# HTTP重定向到HTTPS
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 总结

完成以上步骤后，你的前后端分离项目就可以正常运行了：

1. **后端**：运行在 `http://服务器IP:8080` 或通过Nginx代理 `http://域名/api`
2. **前端**：访问 `http://域名` 或 `http://服务器IP`
3. **接口调用**：前端通过 `/api/xxx` 调用后端接口

**测试流程：**
1. 访问前端页面
2. 打开浏览器开发者工具（F12）
3. 查看Network标签，确认接口请求是否成功
4. 检查接口返回的JSON数据

如有问题，查看日志文件排查：
- 后端日志：`/opt/damai/logs/app.log` 或 `journalctl -u damai -f`
- Nginx日志：`/var/log/nginx/access.log` 和 `/var/log/nginx/error.log`




