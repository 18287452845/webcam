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
- 🔍 **AI人脸识别** - 集成Face++ API进行人脸检测和属性分析
- 📊 **智能属性分析** - 自动识别性别、年龄、笑容、眼镜状态等
- 🎭 **智能明星匹配** - 根据Face++检测的性别自动匹配同性中国明星照片（从网络CDN获取）
- 🤖 **AI智能夸奖** - 集成DeepSeek AI大模型，生成个性化夸奖内容
- 🎨 **动态结果展示** - 打字机动画效果展示识别结果和AI夸奖
- ⚡ **全自动化流程** - 无需手动选择性别，Face++自动检测并匹配

## ✨ 主要特性

- ✅ **完全现代化** - 从Java 1.6 + Flash升级到Java 17 + HTML5
- ✅ **Spring Boot架构** - 使用Spring Boot 3.2.0，简化配置和部署
- ✅ **RESTful API** - 清晰的API设计，易于集成和扩展
- ✅ **配置外部化** - API密钥等敏感信息通过配置文件管理
- ✅ **完善的错误处理** - 友好的错误提示和日志记录
- 🤖 **AI智能性别检测** - Face++ API自动检测性别，无需用户手动选择
- 🎭 **智能同性匹配** - 根据AI检测的性别自动匹配同性中国明星照片（男性→男明星，女性→女明星）
- 🤖 **DeepSeek AI夸奖** - 调用DeepSeek大模型生成60-80字个性化夸奖
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
- **DeepSeek AI** - 个性化夸奖内容生成

## 🚀 快速开始

### 前置要求

- **JDK 17** 或更高版本
- **Maven 3.6+**
- **现代浏览器**（Chrome 53+, Firefox 36+, Edge 12+, Safari 11+）
- **Face++ API密钥**（[申请地址](https://www.faceplusplus.com.cn/)）
- **DeepSeek API密钥**（可选，[申请地址](https://platform.deepseek.com/)）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd webcam
   ```

2. **配置API密钥**

   编辑 `src/main/resources/application.properties`：
   ```properties
   faceplusplus.api.key=your_faceplusplus_api_key
   faceplusplus.api.secret=your_faceplusplus_api_secret
   deepseek.api.key=your_deepseek_api_key
   ```
   
   或使用环境变量：
   ```bash
   export FACEPLUSPLUS_API_KEY=your_faceplusplus_api_key
   export FACEPLUSPLUS_API_SECRET=your_faceplusplus_api_secret
   export DEEPSEEK_API_KEY=your_deepseek_api_key
   ```

   > DeepSeek API Key 用于生成AI夸奖，未配置时系统会自动回退到内置夸奖文案。

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

## 👤 用户使用流程

本系统提供**完全自动化**的人脸识别体验，用户无需任何额外操作：

```
1. 打开应用 (index.html)
   ↓
2. 授权摄像头访问权限
   ↓
3. 30秒倒计时自动拍照（也可手动点击"立即拍照"按钮）
   ↓
4. 系统自动上传照片到后端
   ↓
5. Face++ API 自动检测人脸属性（性别、年龄、笑容等）
   ↓
6. 系统根据检测的性别自动匹配同性明星照片
   ├─ 男性 → 匹配男明星照片
   └─ 女性 → 匹配女明星照片
   ↓
7. DeepSeek AI 生成个性化夸奖文案
   ↓
8. 结果页面展示（带打字机动画效果）
   - 用户照片
   - 检测到的属性（性别、年龄等）
   - 匹配的明星照片
   - AI生成的夸奖文案
```

**核心优势**：
- ✅ 完全自动化，从拍照到结果展示一气呵成
- ✅ 无需手动选择性别，AI智能检测更准确
- ✅ 多重降级机制，确保服务稳定可靠

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
│   │   │   │   ├── DeepSeekProperties.java      # DeepSeek AI配置属性
│   │   │   │   ├── CelebrityProperties.java     # 明星照片配置属性
│   │   │   │   └── UploadProperties.java        # 文件上传配置属性
│   │   │   ├── service/                         # 服务层
│   │   │   │   ├── FaceRecognitionService.java  # 人脸识别服务
│   │   │   │   ├── ImageStorageService.java     # 图像存储服务
│   │   │   │   ├── CelebrityPhotoService.java   # 明星照片服务
│   │   │   │   └── DeepSeekPraiseService.java   # AI夸奖服务
│   │   │   └── MapUtil.java                     # 工具类（数据映射）
│   │   ├── resources/
│   │   │   ├── application.properties           # 主配置文件
│   │   │   ├── application-dev.properties       # 开发环境配置
│   │   │   └── application-prod.properties      # 生产环境配置
│   │   └── webapp/                              # Web资源目录
│   │       ├── index.html                       # 首页（摄像头直接捕获）
│   │       ├── capture.html                     # 旧版重定向页面（兼容性）
│   │       ├── css/
│   │       │   └── main.css                     # 主样式文件
│   │       ├── js/
│   │       │   └── capture.js                   # 摄像头捕获逻辑
│   │       ├── images/                          # 图片资源
│   │       ├── male/                            # 男性匹配图片（降级方案）
│   │       ├── female/                          # 女性匹配图片（降级方案）
│   │       └── WEB-INF/views/
│   │           └── result.jsp                   # 结果展示页面
│   └── test/                                    # 测试代码
├── target/                                      # 编译输出目录
├── upload/                                      # 上传文件目录（运行时创建）
├── pom.xml                                      # Maven配置文件
├── README.md                                    # 项目说明文档
└── .gitignore                                   # Git忽略配置
```

## 🌟 智能体验增强

### AI智能性别检测与同性匹配
本系统采用**完全自动化**的性别检测和匹配流程，无需用户手动选择性别：

1. **自动性别检测**：用户拍照后，Face++ API 自动分析并检测性别
2. **智能同性匹配**：系统根据检测到的性别自动匹配同性明星照片
   - 检测为男性 → 随机匹配10位男明星之一
   - 检测为女性 → 随机匹配10位女明星之一
3. **CDN加速**：`CelebrityPhotoService` 从高速CDN获取明星照片，加载速度快
4. **智能降级**：如果网络图片不可用，自动使用本地 `male/*.png` 或 `female/*.png` 静态资源

**技术优势**：
- ⚡ 简化用户操作流程，提升用户体验
- 🎯 AI检测更准确，避免用户输入错误
- 🔄 全自动化处理，从拍照到匹配一气呵成

### DeepSeek AI智能夸奖
`DeepSeekPraiseService` 提供个性化AI夸奖功能：

- **智能分析**：根据 Face++ 返回的性别、年龄、笑容、眼镜等属性构建提示词
- **AI生成**：调用 DeepSeek Chat API 生成 60-80 字的个性化夸奖内容
- **容错机制**：所有请求都经过超时重试与异常捕获，失败时自动使用预置夸奖文本
- **可选功能**：未配置 `DEEPSEEK_API_KEY` 时系统会直接使用预置夸奖内容，不影响主流程
- **详细文档**：想了解提示词格式、异常处理与测试用例，可阅读 [DEEPSEEK_INTEGRATION.md](DEEPSEEK_INTEGRATION.md)

## 📡 API文档

### POST /webcam

处理图像上传和Face++ API调用。

**请求格式**: `application/x-www-form-urlencoded`

**请求参数**:
- `image` (String, 必需): Base64编码的图像数据，支持 `data:image/jpeg;base64,` 前缀（由前端 `js/capture.js` 自动生成）

> ⚠️ **无需传入性别**：系统会根据Face++检测结果自动判定性别并完成明星匹配。

**响应格式**: `application/json`

**成功响应** (HTTP 200):
```json
{
  "result": "1",
  "msg": {
    "img": "http://localhost:8080/upload/uuid.jpeg",
    "gender": "男性",
    "age": 25,
    "smile": "微笑",
    "eyestatus": "不带眼镜并且睁眼",
    "celebrityPhoto": "https://cdn.example.com/male/liu-de-hua.jpg",
    "praise": "你的笑容很有感染力，给人一种很舒服的感觉。25岁的年纪正值青春，眼神清澈明亮，整体气质看起来精神饱满、充满活力！"
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

#### DeepSeek AI配置
```properties
deepseek.api.url=https://api.deepseek.com/v1/chat/completions
deepseek.api.key=${DEEPSEEK_API_KEY:}
deepseek.api.model=deepseek-chat
deepseek.api.temperature=0.7
deepseek.api.max-tokens=500
```
> 未配置 `deepseek.api.key` 时系统会自动降级为预置夸奖文案。

#### 明星照片配置（可选）
```properties
celebrity.male-photos[0]=https://img1.baidu.com/it/u=2839653127,3970469241&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500
celebrity.female-photos[0]=https://img0.baidu.com/it/u=3775428652,2868156394&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500
```
> 不配置时会使用代码内置的 10 张男性 + 10 张女性明星照片。

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

#### 6. DeepSeek AI夸奖未生成
**错误**: 结果页面没有显示个性化夸奖

**解决**:
- 检查 `deepseek.api.key` 是否配置
- 未配置时会自动使用预置夸奖文案，不影响功能使用
- 如果希望使用AI夸奖，请到 https://platform.deepseek.com/ 申请API Key

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
   - 更新链接：`sec.html` → `index.html`（现在是直接摄像头捕获页面）
   - 移除手动性别选择功能，使用AI自动检测

4. **功能变更**
   - 性别检测由用户手动选择改为Face++自动检测
   - 明星匹配由用户性别参数改为AI检测性别
   - 新增DeepSeek AI夸奖功能（可选）

5. **测试验证**
   - 测试摄像头访问
   - 测试图像上传
   - 测试Face++自动性别检测
   - 测试同性明星匹配
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

## 📚 更多文档

- [CONTRIBUTING.md](CONTRIBUTING.md) - 贡献流程和代码规范
- [CHANGELOG.md](CHANGELOG.md) - 历史版本与主要改动
- [CLAUDE.md](CLAUDE.md) - 面向AI助手的项目说明
- [DEEPSEEK_INTEGRATION.md](DEEPSEEK_INTEGRATION.md) - DeepSeek AI功能的深入解析

## 📄 许可证

本项目基于 [MIT License](LICENSE) 发布，自由用于学习、修改与分发。

## 📞 联系方式

- **项目维护者**: [您的姓名]
- **邮箱**: [您的邮箱]
- **Issues**: [GitHub Issues链接]

## 🙏 致谢

- [Face++](https://www.faceplusplus.com.cn/) - 提供人脸识别API服务
- [DeepSeek](https://www.deepseek.com/) - 提供AI大模型服务
- [Spring Boot](https://spring.io/projects/spring-boot) - 优秀的Java应用框架
- 所有贡献者和用户

---

**最后更新**: 2025-12-03  
**版本**: 2.0.2-SNAPSHOT

## 🔄 最新变更

### v2.0.2 (2025-12-03) - AI自动化升级
- ✨ **移除手动性别选择**：改用Face++ API自动检测性别
- 🎯 **智能同性匹配**：根据AI检测的性别自动匹配同性明星照片
- ⚡ **简化用户流程**：从拍照到结果展示完全自动化
- 📝 **文档更新**：完善README，统一项目文档

更多历史版本请查看 [CHANGELOG.md](CHANGELOG.md)
