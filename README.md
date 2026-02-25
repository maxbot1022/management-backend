# 小说转视频管理平台 - 后端

基于 Spring Boot 2.4.5 + JDK 1.8 的小说转视频管理后台系统。

## 功能模块

- **项目管理** - 统一管理小说转视频项目
- **小说整理** - 使用火山引擎大模型将小说文本整理为分镜脚本
- **图片制作** - 对接火山引擎生图API，支持参考图保持角色一致性
- **视频制作** - 对接火山引擎视频生成API，支持图片首帧

## 技术栈

- Java 1.8
- Spring Boot 2.4.5
- MyBatis Plus 3.4.3
- MySQL 8.0
- Redis
- 阿里云 OSS
- 火山引擎 Ark API

## 项目结构

```
management-backend/
├── src/main/java/com/maxbot/management/
│   ├── controller/    # API控制器
│   ├── service/       # 业务逻辑
│   ├── mapper/        # 数据访问
│   ├── entity/        # 实体类
│   ├── config/        # 配置类
│   ├── job/           # 定时任务
│   └── utils/         # 工具类
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/init.sql    # 数据库初始化脚本
└── pom.xml
```

## 配置说明

在 `application-dev.yml` 中配置以下信息：

- MySQL 数据库连接
- Redis 连接
- 阿里云 OSS
- 火山引擎 Ark API Key

## 运行步骤

1. 创建数据库并执行 `init.sql`
2. 修改 `application-dev.yml` 中的配置
3. 运行 `ManagementApplication`

```bash
# 打包
mvn clean package

# 运行
java -jar target/management-backend-1.0.0.jar
```

## API 文档

启动后访问：http://localhost:8080/api

主要接口：
- `POST /api/project/create` - 创建项目
- `POST /api/file/upload` - 文件上传
- `POST /api/task/novel/create` - 创建小说整理任务
- `POST /api/task/image/create` - 创建图片生成任务
- `POST /api/task/video/create` - 创建视频生成任务
