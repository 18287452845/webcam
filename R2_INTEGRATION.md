# Cloudflare R2 Integration Guide

本文档描述了如何配置和使用 Cloudflare R2 对象存储来上传卡通头像并生成预签名 URL。

## 功能概述

系统在生成卡通头像后进行以下操作：

1. **本地存储**：卡通图片首先保存到本地文件系统（用于网页展示）
2. **R2 上传**：卡通图片上传到 Cloudflare R2（对象存储）
3. **对象键生成**：使用 UUID + 时间戳生成唯一的对象键，避免可猜测
4. **预签名 URL**：生成 600 秒有效期的 GET 预签名 URL
5. **二维码生成**：将预签名 URL 编码为二维码图片（Base64）

## 配置说明

### 环境变量配置（推荐）

对于生产环境，建议使用环境变量：

```bash
# Cloudflare R2 配置
export R2_ACCOUNT_ID=your-account-id
export R2_ACCESS_KEY_ID=your-access-key-id
export R2_ACCESS_KEY_SECRET=your-access-key-secret
export R2_BUCKET_NAME=your-bucket-name
export R2_REGION=auto
export R2_ENDPOINT=https://your-account-id.r2.cloudflarestorage.com
```

### 属性文件配置

在 `application.properties` 或 `application-prod.properties` 中配置：

```properties
# Cloudflare R2 配置
r2.account-id=your-account-id
r2.access-key-id=your-access-key-id
r2.access-key-secret=your-access-key-secret
r2.bucket-name=your-bucket-name
r2.region=auto
r2.endpoint=https://your-account-id.r2.cloudflarestorage.com
r2.presigned-url-expiration=600
```

### 获取 R2 凭证

1. 登录 Cloudflare Dashboard
2. 导航到 **R2 Storage** > **API Tokens**
3. 创建新的 API 令牌
4. 配置权限（至少需要 `object:read` 和 `object:write`）
5. 创建令牌并获取以下信息：
   - Access Key ID
   - Secret Access Key

6. 在 Cloudflare 控制面板获取：
   - Account ID
   - Bucket Name
   - Endpoint（格式：`https://{account-id}.r2.cloudflarestorage.com`）

## API 端点

### POST /api/cartoon/generate

从图片 URL 生成卡通头像

**请求：**
```bash
POST /api/cartoon/generate
Content-Type: application/x-www-form-urlencoded

imageUrl=http://example.com/image.jpg
```

**响应成功（200）：**
```json
{
  "success": true,
  "data": {
    "localUrl": "http://localhost:8080/upload/cartoon_uuid.jpeg",
    "r2ObjectKey": "cartoon/uuid-timestamp.jpeg",
    "presignedUrl": "https://bucket.r2.cloudflarestorage.com/cartoon/uuid-timestamp.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
    "qrCodeBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA...",
    "fileSize": 45678
  },
  "requestId": "uuid"
}
```

**响应错误（400/500）：**
```json
{
  "success": false,
  "error": "错误信息",
  "requestId": "uuid"
}
```

### POST /api/cartoon/generate-from-file

从本地文件路径生成卡通头像（开发/测试用）

**请求：**
```bash
POST /api/cartoon/generate-from-file
Content-Type: application/x-www-form-urlencoded

filePath=/path/to/image.jpg
```

## 响应数据结构

### CartoonImageResult

```java
{
  "localUrl": "http://localhost:8080/upload/cartoon_uuid.jpeg",     // 本地 URL
  "r2ObjectKey": "cartoon/uuid-timestamp.jpeg",                      // R2 对象键
  "presignedUrl": "https://...",                                     // 600秒有效预签名 URL
  "qrCodeBase64": "data:image/png;base64,iVBORw0KGgo...",          // 二维码 Base64
  "fileSize": 45678                                                  // 文件大小（字节）
}
```

## 对象键格式

为了避免可猜测的对象键，系统使用以下格式：

```
cartoon/{UUID}-{TIMESTAMP}.jpeg
```

示例：
```
cartoon/550e8400-e29b-41d4-a716-446655440000-1702382400000.jpeg
```

其中：
- `UUID`：全局唯一标识符（RFC 4122）
- `TIMESTAMP`：毫秒级时间戳

## 预签名 URL 特性

- **有效期**：600 秒（10 分钟）
- **HTTP 方法**：GET（仅允许读取）
- **包含认证**：预签名 URL 中包含所有必需的认证参数
- **无需凭证**：用户可以直接访问 URL，无需 AWS/R2 凭证

预签名 URL 示例：
```
https://bucket.r2.cloudflarestorage.com/cartoon/uuid-timestamp.jpeg?
  X-Amz-Algorithm=AWS4-HMAC-SHA256&
  X-Amz-Credential=...&
  X-Amz-Date=...&
  X-Amz-Expires=600&
  X-Amz-SignedHeaders=host&
  X-Amz-Signature=...
```

## 二维码生成

系统使用 Google ZXing 库生成二维码：

- **编码内容**：预签名 URL
- **格式**：PNG 图片
- **尺寸**：400x400 像素
- **返回格式**：Base64 编码的 Data URI

前端可以直接将二维码 Base64 显示为 HTML 图片：

```html
<img src="data:image/png;base64,iVBORw0KGgoAAAA..." />
```

或将其编码为 HTML 图像标签：

```html
<img id="qrCode" />
<script>
  const qrCodeBase64 = "data:image/png;base64,iVBORw0KGgo...";
  document.getElementById('qrCode').src = qrCodeBase64;
</script>
```

## 错误处理

系统采用优雅降级策略：

- 如果 R2 上传成功 → 返回预签名 URL 和二维码
- 如果 R2 上传失败但本地保存成功 → 仅返回本地 URL（继续进行）
- 如果本地保存失败 → 返回错误响应

日志中会记录所有操作：

```
INFO  - R2 presigned URL generated: expires in 600 seconds
WARN  - R2 upload failed, but continuing with local storage
WARN  - Failed to generate QR code, continuing without it
```

## 安全建议

1. **限制公开访问**：使用 R2 访问控制确保只有授权用户可以访问桶
2. **定期轮换凭证**：定期更新 API 令牌和密钥
3. **监控成本**：R2 提供免费额度，监控使用情况以避免超额费用
4. **HTTPS 传输**：始终通过 HTTPS 传输预签名 URL
5. **URL 过期**：预签名 URL 在 10 分钟后自动过期

## 集成到前端

### 方案 1：直接使用预签名 URL

```javascript
fetch('/api/cartoon/generate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: 'imageUrl=' + encodeURIComponent(imageUrl)
})
.then(res => res.json())
.then(data => {
  if (data.success) {
    // 使用预签名 URL 下载
    const link = document.createElement('a');
    link.href = data.data.presignedUrl;
    link.download = 'cartoon_image.jpeg';
    link.click();
  }
});
```

### 方案 2：显示二维码供用户扫描

```javascript
fetch('/api/cartoon/generate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  body: 'imageUrl=' + encodeURIComponent(imageUrl)
})
.then(res => res.json())
.then(data => {
  if (data.success && data.data.qrCodeBase64) {
    // 显示二维码
    document.getElementById('qrCode').src = data.data.qrCodeBase64;
  }
});
```

## 故障排除

### 问题：R2 配置缺失错误

**原因**：R2 配置未完整设置

**解决方案**：
1. 检查所有必需的配置项是否已设置
2. 验证环境变量是否正确传递
3. 检查 application.properties 文件中的配置

### 问题：预签名 URL 生成失败

**原因**：AWS SDK 初始化失败或 R2 连接问题

**解决方案**：
1. 验证 R2 凭证的正确性
2. 检查 endpoint 是否正确（应为 `https://{account-id}.r2.cloudflarestorage.com`）
3. 查看日志文件获取详细错误信息

### 问题：文件上传到 R2 失败，但本地保存成功

**原因**：R2 服务不可用或网络问题

**解决方案**：
1. 这是预期的行为，系统会自动降级到本地存储
2. 检查网络连接
3. 检查 R2 服务状态
4. 查看日志文件了解具体错误

## 监控和日志

系统在以下事件时记录日志：

- R2 文件上传：`INFO - File uploaded successfully to R2`
- 预签名 URL 生成：`INFO - Presigned URL generated successfully`
- 二维码生成：`INFO - QR code generated successfully`
- 错误：`ERROR - Error uploading file to R2`、`WARN - R2 upload failed`

使用以下命令启用调试日志：

```properties
logging.level.webcam.service.impl=DEBUG
logging.level.software.amazon.awssdk=DEBUG
```

## 性能考虑

- **R2 上传**：通常需要 100-500ms（取决于文件大小和网络）
- **预签名 URL 生成**：通常需要 10-50ms
- **二维码生成**：通常需要 50-200ms

总的来说，整个过程通常在 200-700ms 内完成。

## 成本估算

Cloudflare R2 定价（截至 2024 年）：

- **存储**：$0.015 per GB/month
- **操作**：$0.36 per million requests
- **免费额度**：10GB 存储空间 + 100 万次请求/月

对于典型的卡通头像应用：
- 每张卡通图：~50-100KB
- 1000 用户 = 50-100MB 存储 = $0.0008-0.0015/month
- 成本非常低廉

## 参考链接

- [Cloudflare R2 文档](https://developers.cloudflare.com/r2/)
- [AWS SDK for Java 文档](https://docs.aws.amazon.com/sdk-for-java/)
- [Google ZXing 库](https://github.com/zxing/zxing)
