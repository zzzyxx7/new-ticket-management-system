# 项目部署指南

## 一、传统部署（JAR包方式）

### 1. 打包项目

在项目根目录执行：

```bash
# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 打包后的jar文件位置
# server/target/server-0.0.1-SNAPSHOT.jar
```

### 2. 准备生产环境配置

创建 `application-prod.yml` 文件（放在 `server/src/main/resources/` 目录）：

```yaml
server:
  port: 8080

spring:
  profiles:
    active: prod
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://你的MySQL地址:3306/你的数据库名?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: 你的数据库用户名
    password: 你的数据库密码
  data:
    redis:
      host: 你的Redis地址
      port: 6379
      database: 0
      password: 你的Redis密码（如果有）

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
    name: logs/damai.log

fuzhou:
  jwt:
    user-secret-key: 你的JWT密钥（生产环境请使用强密码）
    user-ttl: 900000
    user-token-name: token
    refresh-secret-key: 你的刷新令牌密钥
    refresh-ttl: 86400000
  alioss:
    endpoint: 你的OSS端点
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
    bucket-name: 你的Bucket名称
  alipay:
    app-id: 你的支付宝AppId
    private-key: 你的支付宝私钥
    public-key: 你的支付宝公钥
    gateway: https://openapi.alipay.com/gateway.do
    return-url: 你的支付回调地址
    notify-url: 你的支付通知地址
```

### 3. 修改主配置文件

修改 `application.yml`，将 `active: dev` 改为 `active: prod`（或通过启动参数指定）

### 4. 重新打包

```bash
mvn clean package -DskipTests
```

### 5. 部署到服务器

#### 方式A：直接运行

```bash
# 上传jar包到服务器
scp server/target/server-0.0.1-SNAPSHOT.jar user@服务器IP:/opt/damai/

# SSH登录服务器
ssh user@服务器IP

# 进入目录
cd /opt/damai

# 运行（前台运行，测试用）
java -jar -Dspring.profiles.active=prod server-0.0.1-SNAPSHOT.jar

# 后台运行（推荐）
nohup java -jar -Dspring.profiles.active=prod server-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
```

#### 方式B：使用systemd服务（推荐）

创建服务文件 `/etc/systemd/system/damai.service`：

```ini
[Unit]
Description=DaMai Platform Service
After=network.target mysql.service redis.service

[Service]
Type=simple
User=你的用户名
WorkingDirectory=/opt/damai
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/damai/server-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
# 重载systemd配置
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start damai

# 设置开机自启
sudo systemctl enable damai

# 查看状态
sudo systemctl status damai

# 查看日志
sudo journalctl -u damai -f
```

### 6. 前置依赖检查

确保服务器已安装并运行：

- **Java 17+**
  ```bash
  java -version
  ```

- **MySQL 8.0+**
  ```bash
  # 检查MySQL是否运行
  sudo systemctl status mysql
  # 或
  sudo systemctl status mysqld
  ```

- **Redis**
  ```bash
  # 检查Redis是否运行
  redis-cli ping
  # 应该返回 PONG
  ```

### 7. 防火墙配置

```bash
# 开放8080端口（如果使用防火墙）
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# 或使用iptables
sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
```

---

## 二、Docker部署（推荐）

### 1. 创建Dockerfile

在项目根目录创建 `Dockerfile`：

```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY common/pom.xml ./common/
COPY pojo/pom.xml ./pojo/
COPY server/pom.xml ./server/
# 先下载依赖（利用Docker缓存）
RUN mvn dependency:go-offline -B
# 复制源代码
COPY . .
# 打包
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app
# 从构建阶段复制jar包
COPY --from=build /app/server/target/server-0.0.1-SNAPSHOT.jar app.jar
# 暴露端口
EXPOSE 8080
# 启动命令
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

### 2. 创建docker-compose.yml

在项目根目录创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: damai-mysql
    environment:
      MYSQL_ROOT_PASSWORD: 你的MySQL root密码
      MYSQL_DATABASE: 你的数据库名
      MYSQL_USER: 你的数据库用户
      MYSQL_PASSWORD: 你的数据库密码
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    networks:
      - damai-network

  redis:
    image: redis:7-alpine
    container_name: damai-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --requirepass 你的Redis密码
    networks:
      - damai-network

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: damai-app
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      # 数据库配置（使用Docker服务名）
      FUZHOU_DATASOURCE_HOST: mysql
      FUZHOU_DATASOURCE_PORT: 3306
      FUZHOU_DATASOURCE_DATABASE: 你的数据库名
      FUZHOU_DATASOURCE_USERNAME: 你的数据库用户
      FUZHOU_DATASOURCE_PASSWORD: 你的数据库密码
      # Redis配置
      FUZHOU_REDIS_HOST: redis
      FUZHOU_REDIS_PORT: 6379
      FUZHOU_REDIS_PASSWORD: 你的Redis密码
    depends_on:
      - mysql
      - redis
    networks:
      - damai-network
    restart: unless-stopped

volumes:
  mysql_data:
  redis_data:

networks:
  damai-network:
    driver: bridge
```

### 3. 构建和运行

```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v
```

---

## 三、环境变量配置（推荐用于生产环境）

如果使用环境变量，可以在 `application.yml` 中这样配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${FUZHOU_DATASOURCE_HOST:localhost}:${FUZHOU_DATASOURCE_PORT:3306}/${FUZHOU_DATASOURCE_DATABASE:damai}
    username: ${FUZHOU_DATASOURCE_USERNAME:root}
    password: ${FUZHOU_DATASOURCE_PASSWORD:}
```

然后在启动时传入环境变量：

```bash
export FUZHOU_DATASOURCE_HOST=你的MySQL地址
export FUZHOU_DATASOURCE_USERNAME=你的用户名
export FUZHOU_DATASOURCE_PASSWORD=你的密码
java -jar server-0.0.1-SNAPSHOT.jar
```

---

## 四、部署检查清单

- [ ] 数据库已创建并导入表结构
- [ ] Redis已启动并可连接
- [ ] 生产环境配置文件已创建（application-prod.yml）
- [ ] JWT密钥已更换为强密码
- [ ] 支付宝配置已填写（如果使用支付功能）
- [ ] OSS配置已填写（如果使用文件上传）
- [ ] 防火墙端口已开放（8080）
- [ ] 日志目录已创建（logs/）
- [ ] 服务器资源充足（内存、CPU）

---

## 五、常见问题

### 1. 启动失败：无法连接MySQL
- 检查MySQL是否运行
- 检查数据库连接配置是否正确
- 检查防火墙是否开放3306端口

### 2. 启动失败：无法连接Redis
- 检查Redis是否运行
- 检查Redis密码是否正确
- 检查Redis配置中的host和port

### 3. 端口被占用
```bash
# 查看端口占用
netstat -tlnp | grep 8080
# 或
lsof -i:8080

# 修改application.yml中的端口
```

### 4. 内存不足
```bash
# 启动时指定JVM参数
java -Xms512m -Xmx1024m -jar server-0.0.1-SNAPSHOT.jar
```

---

## 六、性能优化建议

1. **JVM参数优化**
   ```bash
   java -Xms1g -Xmx2g -XX:+UseG1GC -jar server-0.0.1-SNAPSHOT.jar
   ```

2. **数据库连接池优化**
   - 在 `application-prod.yml` 中配置连接池参数

3. **Redis连接池优化**
   - 已在 `RedissonConfig` 中配置，可根据实际情况调整

4. **日志级别**
   - 生产环境建议设置为 `info` 或 `warn`




