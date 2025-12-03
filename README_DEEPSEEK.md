# DeepSeek AI 夸奖功能 - 快速开始

## 功能简介

本功能在人脸识别后自动调用 DeepSeek AI 大模型，根据检测到的人脸属性（性别、年龄、笑容、眼镜）生成个性化的夸奖内容。

## 快速配置

### 1. 获取 API Key

访问 https://platform.deepseek.com/ 注册并获取 API Key

### 2. 配置文件

编辑 `src/main/resources/application.properties`：

```properties
deepseek.api.key=your_deepseek_api_key_here
```

或设置环境变量：

```bash
export DEEPSEEK_API_KEY=your_key_here
```

### 3. 运行应用

```bash
mvn spring-boot:run
```

## 效果展示

用户上传照片后，系统会：

1. 使用 Face++ API 检测人脸属性
2. 调用 DeepSeek AI 生成个性化夸奖（60-80字）
3. 在结果页面显示：
   - 性别、年龄、笑容、眼镜等属性
   - **AI夸奖**：根据属性生成的个性化夸奖内容

### 夸奖示例

- "你的笑容很有感染力，给人一种很舒服的感觉。25岁的年纪正值青春，眼神清澈明亮..."
- "你的气质真好，面部轮廓分明，五官协调，整体给人一种精神饱满的感觉..."

## 降级策略

如果 DeepSeek API 不可用（网络问题、配额用尽等），系统会自动使用预设的夸奖文本，确保用户体验不受影响。

## 技术细节

- **API 端点**: `https://api.deepseek.com/v1/chat/completions`
- **模型**: `deepseek-chat`
- **温度**: 0.7（控制创意性）
- **最大 Tokens**: 500
- **超时设置**: 连接10秒，读取30秒
- **错误处理**: 优雅降级，不影响主流程

## 成本

- 每次调用约 0.005-0.01 元人民币
- 适用于中小规模应用

## 完整文档

详细配置和开发文档请参考：[DEEPSEEK_INTEGRATION.md](DEEPSEEK_INTEGRATION.md)

---

**版本**: 2.0.0-SNAPSHOT  
**创建日期**: 2025-12-02
