# 使用与你的项目相匹配的 Java 版本 (Java 21)
FROM eclipse-temurin:21-jre

# 设置工作目录
WORKDIR /opt/java

# 复制 JAR 文件到容器中
COPY amst-base-api-0.0.1-SNAPSHOT.jar .

# 暴露应用端口 (根据你的应用配置调整端口号)
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "amst-base-api-0.0.1-SNAPSHOT.jar"]