# DeepSeek AI 夸奖功能集成说明

## 功能概述

本项目已集成 DeepSeek AI 大语言模型，在人脸识别完成后会自动调用 DeepSeek API 生成个性化的夸奖内容。

## 配置方式

### 1. 获取 DeepSeek API Key

访问 [DeepSeek 官网](https://platform.deepseek.com/) 注册账号并获取 API Key。

### 2. 配置 API Key

在 `src/main/resources/application.properties` 或环境变量中配置：

```properties
# DeepSeek Praise API Configuration
deepseek.api.url=https://api.deepseek.com/v1/chat/completions
deepseek.api.key=your_deepseek_api_key_here
deepseek.api.model=deepseek-chat
deepseek.api.temperature=0.7
deepseek.api.max-tokens=500
```

或通过环境变量：

```bash
export DEEPSEEK_API_KEY=your_deepseek_api_key_here
```

### 3. 配置参数说明

- `deepseek.api.url`: DeepSeek API 端点地址（默认无需修改）
- `deepseek.api.key`: 你的 DeepSeek API Key（**必须配置**）
- `deepseek.api.model`: 使用的模型（默认：deepseek-chat）
- `deepseek.api.temperature`: 生成温度，控制创意性（0-1，推荐0.7）
- `deepseek.api.max-tokens`: 最大生成 token 数（推荐500）

## 工作原理

### 1. 人脸识别流程

```
用户上传照片 → Face++ 识别 → DeepSeek 生成夸奖 → 展示结果
```

### 2. 夸奖生成逻辑

DeepSeek 服务会根据以下人脸属性生成个性化夸奖：

- 性别（男性/女性）
- 年龄
- 笑容状态（微笑/笑得灿烂/不笑）
- 眼镜佩戴情况

生成的夸奖内容：
- 长度：60-80 字
- 风格：真诚、温暖、自然
- 语言：亲切的"你"而非"您"

### 3. 降级策略

如果 DeepSeek API 调用失败（网络问题、API 限额等），系统会自动使用预设的夸奖文本作为降级方案，确保用户体验不受影响。

预设夸奖文本包括：
- "你的气质真好，笑容很有感染力..."
- "你看起来真的很棒！面部轮廓分明..."
- "你的气质很出众，看起来是个自信又友善的人..."
- 等 5 条备选文本

## 代码结构

### 新增文件

```
src/main/java/webcam/
├── config/
│   └── DeepSeekProperties.java          # DeepSeek API 配置
├── service/
│   ├── DeepSeekPraiseService.java       # 夸奖服务接口
│   └── impl/
│       └── DeepSeekPraiseServiceImpl.java # 夸奖服务实现

src/test/java/webcam/
└── service/
    └── DeepSeekPraiseServiceTest.java   # 夸奖服务单元测试
```

### 修改文件

```
src/main/java/webcam/
├── controller/
│   ├── WebcamController.java            # 集成 DeepSeek 服务调用
│   └── ResultController.java            # 添加夸奖字段处理

src/main/webapp/WEB-INF/views/
└── result.jsp                           # 添加夸奖内容展示

src/main/resources/
└── application.properties               # 添加 DeepSeek 配置
```

## API 调用示例

### 请求格式

```json
POST https://api.deepseek.com/v1/chat/completions
Authorization: Bearer YOUR_API_KEY
Content-Type: application/json

{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "user",
      "content": "请根据以下人脸属性，生成一段真诚、温暖的夸奖话语（60-80字）：\n性别：男性\n年龄：25岁\n笑容：微笑\n眼镜：不带眼镜并且睁眼\n\n要求：\n1. 语言亲切、自然，避免过度夸张\n2. 结合具体属性进行夸奖\n3. 60-80字左右\n4. 不要使用「您」，使用「你」即可\n5. 直接输出夸奖内容，不要有任何前缀或解释"
    }
  ],
  "temperature": 0.7,
  "max_tokens": 500
}
```

### 响应示例

```json
{
  "choices": [
    {
      "message": {
        "content": "你的笑容真好看，温暖又自然，给人一种很舒服的感觉。25岁的年纪正值青春，眼神清澈明亮，整体气质看起来精神饱满、充满活力。相信你在生活中一定是个很受欢迎的人，继续保持这份自信和阳光！"
      }
    }
  ]
}
```

## 测试

### 运行单元测试

```bash
# 运行所有测试
mvn test

# 仅运行 DeepSeek 服务测试
mvn test -Dtest=DeepSeekPraiseServiceTest
```

### 测试覆盖场景

- ✅ 正常 API 调用成功
- ✅ 空属性返回降级文本
- ✅ Null 属性返回降级文本
- ✅ API 错误返回降级文本
- ✅ 无效响应返回降级文本

## 监控与日志

### 日志级别

```properties
# 开启 DEBUG 日志查看详细信息
logging.level.webcam.service.impl.DeepSeekPraiseServiceImpl=DEBUG
```

### 日志示例

```
2025-12-02 10:30:15 - Generated prompt: 请根据以下人脸属性，生成一段真诚、温暖的夸奖话语...
2025-12-02 10:30:16 - Successfully generated praise: 你的笑容真好看...
```

### 异常处理

所有 DeepSeek API 调用异常都会被捕获并记录：

```
2025-12-02 10:30:15 - Error generating praise with DeepSeek API
2025-12-02 10:30:15 - Failed to generate praise [RequestId: xxx]
```

## 性能考虑

- **超时配置**: RestTemplate 连接超时 10 秒，读取超时 30 秒
- **异步处理**: DeepSeek 调用失败不会影响主流程
- **降级方案**: 确保即使 API 不可用也能提供用户体验

## 成本控制

### DeepSeek 定价（参考）

- 输入: ~0.001 元/千 tokens
- 输出: ~0.002 元/千 tokens
- 预估每次调用: ~0.005-0.01 元

### 建议

1. 为生产环境设置 API 调用配额
2. 监控 API 使用量
3. 考虑缓存相似人脸属性的夸奖结果

## 故障排查

### 问题：API 密钥未配置

**现象**: 日志显示 "调用DeepSeek API失败: 401 Unauthorized"

**解决**: 检查 `application.properties` 中的 `deepseek.api.key` 配置

### 问题：API 调用超时

**现象**: 日志显示 "Error calling DeepSeek API: SocketTimeoutException"

**解决**: 
1. 检查网络连接
2. 调整 RestTemplate 超时配置
3. 确认 DeepSeek API 服务状态

### 问题：API 配额耗尽

**现象**: 日志显示 "调用DeepSeek API失败: 429 Too Many Requests"

**解决**: 
1. 检查 DeepSeek 账户余额
2. 升级 API 套餐
3. 实现请求限流

## 扩展功能建议

### 1. 缓存优化

为相似的人脸属性缓存夸奖结果：

```java
@Cacheable(value = "praise", key = "#faceAttributes.toString()")
public String generatePraise(Map<String, Object> faceAttributes) {
    // ...
}
```

### 2. 个性化配置

允许用户选择夸奖风格（正式/幽默/文艺等）

### 3. 多语言支持

根据用户偏好生成不同语言的夸奖内容

### 4. A/B 测试

对比 DeepSeek 生成的夸奖 vs 预设夸奖的用户反馈

## 参考资源

- [DeepSeek API 文档](https://platform.deepseek.com/docs)
- [DeepSeek 模型介绍](https://www.deepseek.com/)
- [Spring RestTemplate 文档](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/RestTemplate.html)

---

**版本**: 2.0.0-SNAPSHOT  
**最后更新**: 2025-12-02
