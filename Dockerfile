# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 复制 pom 文件（利用 Docker 缓存）
COPY pom.xml .
COPY common/pom.xml common/
COPY pojo/pom.xml pojo/
COPY server/pom.xml server/

# 下载依赖（不运行代码）
RUN mvn dependency:go-offline -B -q || true

# 复制源码
COPY common common/
COPY pojo pojo/
COPY server server/

# 打包（跳过测试）
RUN mvn clean package -DskipTests -B -q

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 时区（可选）
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从构建阶段复制 jar
COPY --from=build /app/server/target/server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# 使用 prod 配置，具体连接信息由环境变量或挂载的 application-prod.yml 提供
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
