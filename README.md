# Webcam 人脸识别系统

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

现代化的基于Spring Boot的Webcam人脸识别应用，使用HTML5 MediaDevices API进行摄像头访问，集成Face++ API进行人脸识别和属性分析。

## 📋 目录

- [项目简介](#项目简介)
- [主要特性](#主要特性)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [API文档](#api文档)
- [配置说明](#配置说明)
- [部署指南](#部署指南)
- [故障排除](#故障排除)
- [从旧版本迁移](#从旧版本迁移)
- [贡献指南](#贡献指南)

## 🎯 项目简介

这是一个基于Web的人脸识别匹配系统（"NXNS云匹配认别系统"），用户可以通过浏览器摄像头拍摄照片，系统会调用Face++ API进行人脸识别，分析性别、年龄、笑容、眼镜等属性，并展示匹配结果。

### 核心功能

- 📷 **实时摄像头访问** - 使用HTML5 MediaDevices API，无需Flash插件
- 🔍 **人脸识别** - 集成Face++ API进行人脸检测和属性分析
- 📊 **属性分析** - 识别性别、年龄、笑容、眼镜状态等
- 🎨 **匹配展示** - 根据识别结果展示匹配信息和描述

## ✨ 主要特性

- ✅ **完全现代化** - 从Java 1.6 + Flash升级到Java 17 + HTML5
- ✅ **Spring Boot架构** - 使用Spring Boot 3.2.0，简化配置和部署
- ✅ **RESTful API** - 清晰的API设计，易于集成和扩展
- ✅ **配置外部化** - API密钥等敏感信息通过配置文件管理
- ✅ **完善的错误处理** - 友好的错误提示和日志记录
- ✅ **响应式设计** - 支持不同屏幕尺寸（主要针对1920x1080优化）
- ✅ **安全考虑** - 输入验证、文件大小限制等安全措施

## 🛠 技术栈

### 后端
- **Java 17 LTS** - 现代Java版本
- **Spring Boot 3.2.0** - 企业级应用框架
- **Jackson** - JSON序列化/反序列化
- **Spring RestTemplate** - HTTP客户端
- **SLF4J + Logback** - 日志框架

### 前端
- **HTML5 MediaDevices API** - 摄像头访问（替代Flash）
- **Fetch API** - 现代HTTP客户端（替代jQuery AJAX）
- **CSS3** - 现代化样式
- **JSP** - 服务端视图渲染

### 外部服务
- **Face++ API** - 人脸识别服务

## 🚀 快速开始

### 前置要求

- **JDK 17** 或更高版本
- **Maven 3.6+**
- **现代浏览器**（Chrome 53+, Firefox 36+, Edge 12+, Safari 11+）
- **Face++ API密钥**（[申请地址](https://www.faceplusplus.com.cn/)）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd webcam
   ```

2. **配置API密钥**

   编辑 `src/main/resources/application.properties`：
   ```properties
   faceplusplus.api.key=your_api_key
   faceplusplus.api.secret=your_api_secret
   ```
   
   或使用环境变量：
   ```bash
   export FACEPLUSPLUS_API_KEY=your_api_key
   export FACEPLUSPLUS_API_SECRET=your_api_secret
   ```

3. **编译项目**
   ```bash
   mvn clean compile
   ```

4. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

5. **访问应用**
   
   打开浏览器访问：`http://localhost:8080/index.html`

## 📁 项目结构

```
webcam/
├── src/
│   ├── main/
│   │   ├── java/webcam/
│   │   │   ├── WebcamApplication.java          # Spring Boot主启动类
│   │   │   ├── controller/                      # 控制器层
│   │   │   │   ├── WebcamController.java        # 图像处理REST控制器
│   │   │   │   └── ResultController.java        # 结果展示MVC控制器
│   │   │   ├── config/                          # 配置类
│   │   │   │   ├── WebConfig.java               # Web配置（静态资源、视图解析）
│   │   │   │   ├── RestTemplateConfig.java      # HTTP客户端配置
│   │   │   │   ├── FacePlusPlusProperties.java  # Face++配置属性
│   │   │   │   └── UploadProperties.java        # 文件上传配置属性
│   │   │   └── MapUtil.java                     # 工具类（数据映射）
│   │   ├── resources/
│   │   │   ├── application.properties           # 主配置文件
│   │   │   ├── application-dev.properties       # 开发环境配置
│   │   │   └── application-prod.properties      # 生产环境配置
│   │   └── webapp/                              # Web资源目录
│   │       ├── index.html                       # 首页
│   │       ├── capture.html                     # 拍照页面（HTML5）
│   │       ├── css/
│   │       │   └── main.css                     # 主样式文件
│   │       ├── images/                          # 图片资源
│   │       ├── male/                            # 男性匹配图片
│   │       ├── female/                          # 女性匹配图片
│   │       └── WEB-INF/views/
│   │           └── result.jsp                   # 结果展示页面
│   └── test/                                    # 测试代码
├── target/                                      # 编译输出目录
├── upload/                                      # 上传文件目录（运行时创建）
├── pom.xml                                      # Maven配置文件
├── README.md                                    # 项目说明文档
└── .gitignore                                   # Git忽略配置
```

## 📡 API文档

### POST /webcam

处理图像上传和Face++ API调用。

**请求格式**: `application/x-www-form-urlencoded`

**请求参数**:
- `image` (String, 必需): Base64编码的图像数据，支持 `data:image/jpeg;base64,` 前缀

**响应格式**: `application/json`

**成功响应** (HTTP 200):
```json
{
  "result": "1",
  "msg": {
    "img": "http://localhost:8080/upload/uuid.jpeg",
    "faceToken": "face_token_string",
    "gender": "男性",
    "age": 25,
    "smile": "微笑",
    "eyestatus": "不带眼镜并且睁眼"
  }
}
```

**失败响应** (HTTP 200, result="0"):
```json
{
  "result": "0",
  "msg": "错误信息"
}
```

**错误响应** (HTTP 4xx/5xx):
```json
{
  "result": "0",
  "msg": "处理图像时发生错误: 详细错误信息"
}
```

### GET /result

显示人脸识别结果页面。

**查询参数**:
- `msg` (String, 必需): JSON格式的结果数据（URL编码）

**响应**: JSP页面渲染

**示例URL**:
```
http://localhost:8080/result?msg=%7B%22img%22%3A%22...%22%2C%22gender%22%3A%22%E7%94%B7%E6%80%A7%22%7D
```

## ⚙️ 配置说明

### 应用配置 (application.properties)

#### 服务器配置
```properties
server.port=8080
server.servlet.context-path=/
```

#### 文件上传配置
```properties
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
webcam.upload.path=upload/
webcam.upload.base-url=http://localhost:8080/upload/
```

#### Face++ API配置
```properties
faceplusplus.api.url=https://api-cn.faceplusplus.com/facepp/v3/detect
faceplusplus.api.key=${FACEPLUSPLUS_API_KEY:default_key}
faceplusplus.api.secret=${FACEPLUSPLUS_API_SECRET:default_secret}
faceplusplus.api.return-attributes=gender,age,smiling,eyestatus,glass,headpose,facequality,blur
faceplusplus.api.return-landmark=0
```

#### 日志配置
```properties
logging.level.webcam=INFO
logging.level.org.springframework.web=INFO
```

### 环境配置

#### 开发环境 (application-dev.properties)
```properties
spring.profiles.active=dev
server.port=8080
logging.level.webcam=DEBUG
```

#### 生产环境 (application-prod.properties)
```properties
spring.profiles.active=prod
server.port=80
logging.level.webcam=INFO
logging.file.name=logs/webcam.log
```

## 🚢 部署指南

### 方式1: Spring Boot内嵌服务器

```bash
# 打包
mvn clean package

# 运行
java -jar target/webcam.war

# 或指定配置文件
java -jar target/webcam.war --spring.profiles.active=prod
```

### 方式2: 传统WAR部署

1. **打包WAR文件**
   ```bash
   mvn clean package
   ```

2. **部署到Tomcat**
   - 将 `target/webcam.war` 复制到 `$CATALINA_HOME/webapps/`
   - 启动Tomcat服务器
   - 访问: `http://localhost:8080/webcam/index.html`

3. **配置HTTPS**（生产环境必需）
   - 配置Tomcat SSL证书
   - 更新 `application.properties` 中的端口和URL

### Docker部署（可选）

创建 `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/webcam.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
```

构建和运行:
```bash
docker build -t webcam-app .
docker run -p 8080:8080 webcam-app
```

## 🔧 故障排除

### 常见问题

#### 1. 数据源配置错误
**错误**: `Failed to configure a DataSource`

**解决**: 已排除数据源自动配置，无需数据库配置。

#### 2. 静态资源404错误
**错误**: `No static resource index.html`

**解决**: 
- 确保运行了 `mvn clean compile` 编译项目
- 检查 `target/classes/META-INF/resources/` 目录是否存在文件
- 如果文件不存在，重新编译项目

#### 3. 摄像头无法访问
**错误**: 浏览器提示无法访问摄像头

**解决**:
- 确保使用HTTPS（生产环境）或localhost（开发环境）
- 检查浏览器权限设置
- 使用支持的浏览器（Chrome、Firefox、Edge等）

#### 4. Face++ API调用失败
**错误**: API返回错误

**解决**:
- 检查API密钥是否正确配置
- 验证API密钥是否有效
- 检查网络连接
- 查看应用日志获取详细错误信息

#### 5. 文件上传失败
**错误**: 无法保存上传的文件

**解决**:
- 确保 `upload/` 目录存在且有写入权限
- 检查文件大小是否超过限制（默认10MB）
- 查看应用日志

### 调试模式

启用调试日志:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

或在 `application.properties` 中设置:
```properties
logging.level.webcam=DEBUG
logging.level.org.springframework.web=DEBUG
```

## 🔄 从旧版本迁移

### 主要变更

| 组件 | 旧版本 | 新版本 |
|------|--------|--------|
| Java | 1.6 | 17 LTS |
| Web框架 | Servlet 2.5 | Spring Boot 3.2.0 |
| 摄像头访问 | Flash | HTML5 MediaDevices API |
| JSON处理 | json-lib 2.4 | Jackson |
| HTTP客户端 | HttpClient 4.5.2 | Spring RestTemplate |
| 日志 | System.out.println | SLF4J + Logback |
| Base64 | sun.misc.BASE64Decoder | java.util.Base64 |

### 迁移步骤

1. **环境准备**
   - 安装JDK 17
   - 更新Maven到3.6+

2. **配置更新**
   - 更新 `application.properties` 中的API密钥
   - 检查文件路径配置

3. **前端更新**
   - 使用现代浏览器（不再需要Flash插件）
   - 更新链接：`sec.html` → `capture.html`

4. **测试验证**
   - 测试摄像头访问
   - 测试图像上传
   - 测试API调用
   - 测试结果展示

## 🌐 浏览器兼容性

| 浏览器 | 最低版本 | 状态 |
|--------|---------|------|
| Chrome | 53+ | ✅ 完全支持 |
| Edge | 12+ | ✅ 完全支持 |
| Firefox | 36+ | ✅ 完全支持 |
| Safari | 11+ | ✅ 完全支持 |
| Opera | 40+ | ✅ 完全支持 |

**注意**: 
- HTML5 MediaDevices API需要HTTPS（生产环境）
- 本地开发可以使用HTTP（localhost）

## 🔒 安全注意事项

1. **API密钥安全**
   - 不要将包含API密钥的配置文件提交到版本控制
   - 使用环境变量或密钥管理服务
   - 定期轮换API密钥

2. **HTTPS要求**
   - 生产环境必须使用HTTPS
   - 配置有效的SSL证书
   - 启用HSTS

3. **文件上传安全**
   - 限制文件大小（默认10MB）
   - 验证文件类型
   - 扫描恶意文件
   - 定期清理上传目录

4. **输入验证**
   - 验证所有用户输入
   - 防止XSS攻击
   - 防止CSRF攻击

5. **日志安全**
   - 不要在日志中记录敏感信息
   - 定期轮转日志文件
   - 限制日志文件访问权限

## 📝 开发指南

### 开发模式

```bash
# 启动开发服务器（自动重载）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用Spring Boot DevTools（如果已配置）
mvn spring-boot:run
```

### 代码规范

- 遵循Java编码规范
- 使用有意义的变量和方法名
- 添加必要的注释和JavaDoc
- 保持代码简洁和可读性

### 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

### 贡献类型

- 🐛 Bug修复
- ✨ 新功能
- 📝 文档改进
- 🎨 UI/UX改进
- ⚡ 性能优化
- 🔒 安全改进

## 📄 许可证

[添加您的许可证信息]

## 📞 联系方式

- **项目维护者**: [您的姓名]
- **邮箱**: [您的邮箱]
- **Issues**: [GitHub Issues链接]

## 🙏 致谢

- [Face++](https://www.faceplusplus.com.cn/) - 提供人脸识别API服务
- [Spring Boot](https://spring.io/projects/spring-boot) - 优秀的Java应用框架
- 所有贡献者和用户

---

**最后更新**: 2025-11-29  
**版本**: 2.0.0-SNAPSHOT
