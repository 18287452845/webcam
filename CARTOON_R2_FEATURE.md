# 卡通头像 R2 上传和预签名 URL 功能

## 功能描述

本功能在卡通头像生成后，自动将其上传到 Cloudflare R2 对象存储，并生成：
1. 预签名 URL（600秒有效期，用于临时下载）
2. 二维码（编码预签名 URL，用于分享）

## 工作流程

```
用户图片 URL
    ↓
[卡通图片生成] - 调用阿里云 Bailian API
    ↓
生成的卡通图片（临时 URL）
    ↓
[下载到本地]
    ↓
本地保存 ✓
    ↓
[上传到 R2]
    ├─ 成功
    │   ↓
    │   [生成预签名 URL] - 600秒有效
    │   ↓
    │   [生成二维码]
    │   ↓
    │   返回完整结果 ✓
    └─ 失败 (日志记录，继续)
        ↓
        仅返回本地 URL ✓ (graceful degradation)
```

## 实现细节

### 1. 对象键生成

避免可猜测的对象键，使用 UUID + 时间戳组合：

```java
// 示例
cartoon/550e8400-e29b-41d4-a716-446655440000-1702382400000.jpeg
          ├─ UUID (全局唯一)
          └─ 毫秒级时间戳 (防止碰撞)
```

### 2. 预签名 URL

使用 AWS SDK v4 签名生成的预签名 URL，包含所有必需的认证参数：

```
https://bucket.r2.cloudflarestorage.com/cartoon/uuid-timestamp.jpeg?
  X-Amz-Algorithm=AWS4-HMAC-SHA256&
  X-Amz-Credential=...&
  X-Amz-Date=...&
  X-Amz-Expires=600&
  X-Amz-SignedHeaders=host&
  X-Amz-Signature=...
```

特点：
- 自动过期（600秒后无效）
- 仅允许 GET 请求
- 无需凭证即可访问

### 3. 二维码

使用 Google ZXing 库生成：
- 格式：PNG 图片
- 尺寸：400x400 像素
- 内容：完整的预签名 URL
- 返回：Base64 编码的 Data URI

```
data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA...
```

### 4. 错误处理

采用优雅降级（Graceful Degradation）：

```
R2 上传成功
  → 返回 presignedUrl 和 qrCodeBase64
  
R2 上传失败，本地保存成功
  → 仅返回 localUrl
  → 日志记录警告
  
本地保存失败
  → 返回错误响应
  → 日志记录错误
```

## API 使用示例

### 请求

```bash
curl -X POST http://localhost:8080/api/cartoon/generate \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "imageUrl=http://example.com/user-photo.jpg"
```

### 成功响应

```json
{
  "success": true,
  "data": {
    "localUrl": "http://localhost:8080/upload/cartoon_abc123.jpeg",
    "r2ObjectKey": "cartoon/550e8400-e29b-41d4-a716-446655440000-1702382400000.jpeg",
    "presignedUrl": "https://mybucket.r2.cloudflarestorage.com/cartoon/550e8400-e29b-41d4-a716-446655440000-1702382400000.jpeg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...",
    "qrCodeBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAZAAAAAZCAIAAACQc3/tAAAFuElEQVR4nO3dQW...",
    "fileSize": 45678
  },
  "requestId": "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6"
}
```

### 部分失败响应（R2 上传失败，但本地保存成功）

```json
{
  "success": true,
  "data": {
    "localUrl": "http://localhost:8080/upload/cartoon_abc123.jpeg",
    "r2ObjectKey": null,
    "presignedUrl": null,
    "qrCodeBase64": null,
    "fileSize": 45678
  },
  "requestId": "..."
}
```

## 集成到卡通图片生成流程

### CartoonImageServiceImpl 的修改

原来：
```java
String cartoonImageUrl = cartoonImageService.generateCartoonImageFromUrl(imageUrl);
```

现在：
```java
CartoonImageResult result = cartoonImageService.generateCartoonImageFromUrl(imageUrl);
// result 包含：
// - localUrl: 本地访问 URL
// - r2ObjectKey: R2 中的对象键
// - presignedUrl: 预签名 URL（600秒有效）
// - qrCodeBase64: 二维码 Base64
// - fileSize: 文件大小
```

### 关键代码片段

```java
// 在 CartoonImageServiceImpl 中
private CartoonImageResult downloadAndSaveCartoonImage(String imageUrl) {
    // 1. 下载卡通图片
    byte[] imageBytes = downloadImage(imageUrl);
    
    // 2. 保存到本地
    String fileName = "cartoon_" + UUID.randomUUID() + ".jpeg";
    Path savedPath = imageStorageService.saveBase64Image(base64Data, fileName);
    String localUrl = imageStorageService.getImageUrl(fileName);
    
    // 3. 初始化结果对象
    CartoonImageResult result = new CartoonImageResult();
    result.setLocalUrl(localUrl);
    result.setFileSize(imageBytes.length);
    
    // 4. 尝试上传到 R2（非阻塞失败）
    try {
        String r2ObjectKey = generateR2ObjectKey(); // UUID-timestamp
        R2UploadService.R2ObjectInfo r2Info = r2UploadService.uploadFromBase64(
            base64Data, r2ObjectKey, "image/jpeg");
        result.setR2ObjectKey(r2ObjectKey);
        
        // 5. 生成预签名 URL（600 秒）
        String presignedUrl = r2UploadService.getPresignedUrl(
            r2ObjectKey, 600);
        result.setPresignedUrl(presignedUrl);
        
        // 6. 生成二维码
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(
            presignedUrl, 400, 400);
        result.setQrCodeBase64(qrCodeBase64);
        
    } catch (FileStorageException e) {
        logger.warn("R2 upload failed, continuing with local storage", e);
        // 继续返回本地 URL，不中断流程
    }
    
    return result;
}

// 生成安全的对象键（UUID + 时间戳）
private String generateR2ObjectKey() {
    String uuid = UUID.randomUUID().toString();
    long timestamp = Instant.now().toEpochMilli();
    return String.format("cartoon/%s-%d.jpeg", uuid, timestamp);
}
```

## 配置要求

### 环境变量

```bash
export R2_ACCOUNT_ID=xxxxxxxxxxxx
export R2_ACCESS_KEY_ID=xxxxxxxxxxxx
export R2_ACCESS_KEY_SECRET=xxxxxxxxxxxx
export R2_BUCKET_NAME=my-cartoon-bucket
export R2_ENDPOINT=https://xxxxxxxxxxxx.r2.cloudflarestorage.com
```

### 或在 application.properties 中

```properties
r2.account-id=xxxxxxxxxxxx
r2.access-key-id=xxxxxxxxxxxx
r2.access-key-secret=xxxxxxxxxxxx
r2.bucket-name=my-cartoon-bucket
r2.region=auto
r2.endpoint=https://xxxxxxxxxxxx.r2.cloudflarestorage.com
r2.presigned-url-expiration=600
```

## 性能指标

| 操作 | 典型耗时 | 备注 |
|------|---------|------|
| 下载卡通图片 | 500-2000ms | 取决于阿里云 API 响应 |
| 保存到本地 | 10-50ms | 文件 I/O |
| 上传到 R2 | 100-500ms | 网络延迟 |
| 生成预签名 URL | 10-50ms | 签名计算 |
| 生成二维码 | 50-200ms | ZXing 编码 |
| **总计** | **~700-3000ms** | 受决于网络状况 |

## 安全考虑

1. **对象键唯一性**：使用 UUID + 毫秒时间戳，碰撞概率极低
2. **预签名 URL 过期**：600 秒后自动失效，减少泄露风险
3. **只读访问**：预签名 URL 仅支持 GET 请求
4. **凭证安全**：
   - 不在代码中硬编码凭证
   - 使用环境变量传递
   - 定期轮换 API 令牌

## 故障排除

### 问题：R2 上传失败，但本地图片成功保存

这是**正常行为**，系统采用优雅降级设计。

解决方案：
1. 检查 R2 凭证配置
2. 验证网络连接
3. 查看日志了解具体错误
4. 用户仍然可以访问本地 URL

### 问题：预签名 URL 返回 403 Forbidden

原因可能：
1. URL 已过期（600 秒）
2. 对象在 R2 中不存在
3. 签名计算错误

解决方案：
1. 立即重新请求
2. 检查 R2 配置
3. 查看 R2 访问日志

### 问题：二维码无法生成

原因：ZXing 库问题或 URL 过长

解决方案：
1. 确保 ZXing 依赖正确安装
2. 预签名 URL 可能过长，尝试使用短链
3. 启用调试日志

## 前端集成示例

### 方案 1：直接下载

```javascript
async function downloadCartoonImage(userImageUrl) {
  const response = await fetch('/api/cartoon/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: 'imageUrl=' + encodeURIComponent(userImageUrl)
  });
  
  const data = await response.json();
  
  if (data.success && data.data.presignedUrl) {
    // 使用 R2 预签名 URL 下载
    const link = document.createElement('a');
    link.href = data.data.presignedUrl;
    link.download = 'cartoon_image.jpeg';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
}
```

### 方案 2：显示二维码

```javascript
async function showQrCode(userImageUrl) {
  const response = await fetch('/api/cartoon/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: 'imageUrl=' + encodeURIComponent(userImageUrl)
  });
  
  const data = await response.json();
  
  if (data.success && data.data.qrCodeBase64) {
    // 显示二维码
    const qrImg = document.createElement('img');
    qrImg.src = data.data.qrCodeBase64;
    qrImg.alt = 'Download QR Code';
    document.getElementById('qr-container').appendChild(qrImg);
  }
}
```

### 方案 3：同时显示本地和 R2 URL

```javascript
async function displayCartoonResult(userImageUrl) {
  const response = await fetch('/api/cartoon/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: 'imageUrl=' + encodeURIComponent(userImageUrl)
  });
  
  const data = await response.json();
  
  if (data.success) {
    // 显示本地图片
    document.getElementById('cartoon-image').src = data.data.localUrl;
    
    // 如果 R2 可用，显示分享信息
    if (data.data.presignedUrl) {
      document.getElementById('share-url').textContent = data.data.presignedUrl;
      document.getElementById('qr-code').src = data.data.qrCodeBase64;
      document.getElementById('share-info').style.display = 'block';
    }
  }
}
```

## 成本分析

假设月用户：1000 人，平均文件大小：50 KB

- **存储成本**：50 GB × $0.015 = $0.75/月
- **操作成本**：1000 次上传 × $0.36/100万 = $0.00036/月
- **总成本**：约 $0.75/月（远低于免费额度）

## 相关文件

- `CartoonImageService.java`：卡通图片生成服务接口
- `CartoonImageServiceImpl.java`：实现（包含 R2 上传逻辑）
- `R2UploadService.java`：R2 上传服务接口
- `R2UploadServiceImpl.java`：R2 上传服务实现
- `QrCodeService.java`：二维码生成服务接口
- `QrCodeServiceImpl.java`：二维码生成服务实现
- `CartoonImageResult.java`：结果数据类
- `CartoonImageController.java`：REST 端点
- `R2_INTEGRATION.md`：完整的 R2 集成指南
