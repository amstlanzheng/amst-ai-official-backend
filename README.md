# AMST智能公众号管理

## 项目介绍

这是一个基于Spring Boot 3开发的智能公众号管理项目，包含了构建现代公众号应用程序所需的各种功能。

### 核心特性

- **统一异常管理**: 全局异常处理器，标准化错误响应格式
- **统一返回结果**: 规范化的API响应结构，便于前端处理
- **跨域请求配置**: 支持跨域请求，方便前后端分离开发
- **数据库配置**: 集成MySQL数据库，使用MyBatis-Flex作为ORM框架
- **Redis集成**: 集成Redis用于缓存和会话管理
- **用户认证授权**: 完整的用户注册、登录、权限控制机制
- **接口文档**: 集成Swagger/Knife4j自动生成API文档
- **代码生成**: 内置MyBatis代码生成器，提高开发效率
- **微信公众号管理**: 支持多公众号接入和管理
- **智能AI回复**: 集成AI大模型，支持智能对话回复
- **微信素材管理**: 支持微信公众号素材上传、下载和管理
- **自动回复规则**: 支持多种类型的消息自动回复规则配置

## 技术栈

### 后端技术
- **核心框架**: Spring Boot 3.4.9
- **编程语言**: Java 21
- **数据库**: MySQL 8
- **持久层**: MyBatis-Flex 1.11.0
- **缓存**: Redis
- **接口文档**: Knife4j + SpringDoc OpenAPI 3
- **工具库**: Hutool、Apache Commons Lang3
- **其他**: Lombok 1.18.38、AOP、Session管理

## 环境要求

- JDK 21
- MySQL 8+
- Redis
- Maven 3.6+

## 快速开始

### 1. 环境配置

在运行项目之前，需要配置以下环境变量或修改[application.yml](src/main/resources/application.yml)文件:

```bash
MYSQL_HOST=localhost      # MySQL服务器地址
MYSQL_PORT=3306          # MySQL端口号
MYSQL_DATABASE=amst_db   # 数据库名
MYSQL_USER=root          # 数据数据库用户名
MYSQL_PASSWORD=password  # 数据库密码

REDIS_HOST=localhost     # Redis服务器地址
REDIS_PORT=6379          # Redis端口号
REDIS_PASSWORD=          # Redis密码(如果有的话)

OPENAI_API_KEY=sk-xxxxxx # OpenAI API密钥(可选)
DEEPSEEK_API_KEY=sk-xxxxxx # DeepSeek API密钥(可选)
```

### 2. 数据库初始化

执行SQL脚本[src/main/resources/sql/schema.sql](src/main/resources/sql/schema.sql)创建数据库表结构。

### 3. 项目启动

1. 使用Maven命令启动项目:

```bash
mvn clean install
mvn spring-boot:run
```

2. 在IDE中直接运行[AmstBaseApiApplication.java](src/main/java/com/amst/api/AmstBaseApiApplication.java)主类。

3. 线上部署启动
    <1> 使用`mvn package`命令打jar包,
    <2> 修改根目录下[.env-example](.env-example)内容，并且重命名为.env
    <3> 将[Dockerfile](Dockerfile),[.env](.env)和jar包放在同一目录，执行
   `docker rm -f amst-ai-app`
   `docker build -t amst-ai-app .`  
   `docker run -d --name amst-ai-app -p 8080:8080 --env-file .env amst-ai-app`
    <4> 使用命令查看运行中的容器 `docker ps`
    
### 4. 访问接口

项目启动后，可通过以下地址访问:

- API根路径: http://localhost:8866/api
- API文档: http://localhost:8866/api/swagger-ui.html
- 文档地址: http://localhost:8866/api/doc.html

## 项目结构

```
src/main/java/com/amst/api/
├── annotation/     # 自定义注解
├── aop/            # 切面编程
├── common/         # 公共模块
│   ├── config/     # 配置类
│   ├── constant/   # 常量定义
│   ├── exception/  # 异常处理
│   └── response/   # 统一响应
├── controller/     # 控制层
├── mapper/         # 数据访问层
├── model/          # 数据模型
│   ├── dto/        # 数据传输对象
│   ├── entity/     # 实体类
│   └── vo/         # 视图对象
├── service/        # 业务逻辑层
└── AmstBaseApiApplication.java  # 启动类
```

## 功能模块

### 用户模块

- 用户注册 [/api/user/register](src/main/java/com/amst/api/controller/UserController.java)
- 用户登录 [/api/user/login](src/main/java/com/amst/api/controller/UserController.java)
- 获取当前登录用户 [/api/user/get/login](src/main/java/com/amst/api/controller/UserController.java)
- 用户注销 [/api/user/logout](src/main/java/com/amst/api/controller/UserController.java)
- 创建用户 [/api/user/add](src/main/java/com/amst/api/controller/UserController.java)
- 获取用户信息 [/api/user/get](src/main/java/com/amst/api/controller/UserController.java)
- 删除用户 [/api/user/delete](src/main/java/com/amst/api/controller/UserController.java)
- 更新用户 [/api/user/update](src/main/java/com/amst/api/controller/UserController.java)
- 分页查询用户 [/api/user/list/page/vo](src/main/java/com/amst/api/controller/UserController.java)

### 微信公众号模块

- 公众号接入认证 [/api/wx/msg/{appId}](src/main/java/com/amst/api/controller/WxMpPortalController.java)
- 公众号管理(增删改查) [/api/wx/account/*](src/main/java/com/amst/api/controller/WxAccountController.java)
- 公众号素材管理(上传/下载) [/api/wx/material/*](src/main/java/com/amst/api/controller/WxMaterialController.java)
- 消息自动回复规则 [/api/wx/reply/*](src/main/java/com/amst/api/controller/WxReplyRuleController.java)
- AI智能回复 [/api/aiReplyRecord/*](src/main/java/com/amst/api/controller/AiReplyRecordController.java)

#### 回复规则类型

系统支持四种类型的回复规则：

1. **关键字回复** - 当用户发送的消息包含特定关键字时触发
2. **收到消息回复** - 当收到任何消息时的默认回复
3. **被关注回复** - 当用户关注公众号时自动发送的欢迎消息
4. **菜单栏点击事件回复** - 当用户点击自定义菜单时触发

#### 回复内容类型

每种回复规则支持多种内容类型：

- **文字** - 普通文本消息
- **图片** - 图片素材消息
- **语音** - 语音素材消息
- **视频** - 视频素材消息
- **图文** - 图文组合消息

### AI智能回复

系统集成了AI大模型，当没有匹配的自动回复规则时，系统会自动调用AI生成回复内容。AI回复具有以下特点：

- 支持上下文对话记忆
- 异步处理机制，避免微信回调超时
- 回复内容缓存，防止重复生成
- 支持多种AI模型（OpenAI、DeepSeek等）

## 开发指南

### 代码规范

- 使用Lombok简化Java代码
- 使用统一的异常处理机制
- 使用DTO进行数据传输
- 使用VO进行数据展示

### 权限控制

通过[@AuthCheck](src/main/java/com/amst/api/annotation/AuthCheck.java)注解实现接口级别的权限控制:

```java
 // 设置仅管理员可访问
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<User> test(@RequestParam long id) {}
```
- 无权限注解: 该接口 可被所有用户访问
- @AuthCheck: 要至少普通用户访问
- @AuthCheck(mustRole = UserConstant.USER_ROLE): 只能被管理员用户访问

### 微信公众号接入说明

1. 在系统中添加公众号配置信息
2. 在微信公众号平台设置服务器地址(URL):
   ```
   https://你的域名/api/wx/msg/你的AppId
   ```
3. 微信服务器将向该地址发送GET请求进行验证
4. 验证通过后，系统即可接收并处理微信消息

### AI集成配置

系统支持多种AI大模型，在配置文件中设置相应的API密钥即可：

```bash
# OpenAI API密钥
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# DeepSeek API密钥
DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

系统默认使用DeepSeek模型，可根据需要切换至其他模型。

### 注意事项

1. JDK版本和SpringBoot版本可根据需求调整，但需注意Lombok版本兼容性
2. 修改YAML配置或设置相应环境变量以匹配实际部署环境
3. 生产环境中应关闭开发工具(devtools)并加强安全配置
4. 数据库连接池使用HikariCP，默认配置可根据实际负载情况调优
5. 微信公众号接入时确保服务器地址可以通过公网访问
6. AI回复功能需要配置有效的API密钥才能正常工作


### 后续完善计划
1. 添加ai对话记忆功能
2. 添加招呼语
3. ai更多功能完善，包括联网搜索，图片表情包下载，数据库存储，多模态等等
4. 文章发布
5. 更好的ai人设，更优秀的聊天