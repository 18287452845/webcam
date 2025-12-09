# Mock测试指南

本文档描述如何执行Mock测试，模拟阿里云百炼API失败场景。

## 测试目的

验证当阿里云百炼API调用失败时，系统的错误处理机制是否正常工作。

## 测试方法

由于当前系统直接调用真实的阿里云百炼API，Mock测试可以通过以下方式进行：

### 方法1: 临时修改配置使用无效API密钥

**测试步骤：**

1. **备份当前配置**
   - 备份 `src/main/resources/application.properties`

2. **修改API密钥**
   - 将 `bailian.api.api-key` 改为无效值（如 `invalid_key`）

3. **重启服务**
   - 停止当前服务
   - 重新启动服务

4. **执行测试**
   - 使用API测试脚本发送有效图片
   - 或通过前端上传图片

5. **验证结果**
   - 验证系统返回错误响应
   - 验证错误码为 `FACE_API_ERROR`
   - 验证HTTP状态码为 503 (Service Unavailable)
   - 验证错误信息清晰明确

6. **恢复配置**
   - 恢复原始的API密钥配置
   - 重启服务

**预期结果：**
- HTTP状态码：503
- 错误码：`FACE_API_ERROR`
- 错误信息包含："人脸识别服务调用失败"
- 前端正确显示错误提示

**实际结果：** [ ] PASS [ ] FAIL

---

### 方法2: 模拟网络故障

**测试步骤：**

1. **断开网络连接**（或使用防火墙阻止对阿里云百炼API的访问）

2. **执行测试**
   - 通过API或前端上传图片

3. **验证结果**
   - 验证系统正确处理网络错误
   - 验证错误信息提示清晰

4. **恢复网络连接**

**预期结果：**
- 系统能够捕获网络错误
- 返回适当的错误响应
- 错误信息对用户友好

**实际结果：** [ ] PASS [ ] FAIL

---

### 方法3: 使用Postman或curl模拟API响应

**测试步骤：**

1. **使用Postman创建Mock服务器**
   - 创建一个Mock端点模拟阿里云百炼API
   - 配置返回错误响应

2. **修改应用配置**
   - 临时修改 `bailian.api.endpoint` 指向Mock服务器

3. **执行测试**
   - 通过API或前端上传图片

4. **验证结果**
   - 验证系统正确处理API错误响应

**预期结果：**
- 系统能够处理阿里云百炼API返回的各种错误状态
- 错误处理逻辑正确

**实际结果：** [ ] PASS [ ] FAIL

---

## 测试用例

### TC-MOCK-001: 阿里云百炼API认证失败

**测试场景：** API密钥无效

**预期响应：**
```json
{
  "result": "0",
  "errorCode": "FACE_API_ERROR",
  "errorDetail": "人脸识别服务调用失败: API认证失败",
  "requestId": "...",
  "timestamp": "..."
}
```

**HTTP状态码：** 503

---

### TC-MOCK-002: 阿里云百炼API网络超时

**测试场景：** API响应超时

**预期响应：**
```json
{
  "result": "0",
  "errorCode": "FACE_API_ERROR",
  "errorDetail": "人脸识别服务调用失败: 请求超时",
  "requestId": "...",
  "timestamp": "..."
}
```

**HTTP状态码：** 503

---

### TC-MOCK-003: 阿里云百炼API返回错误

**测试场景：** API返回业务错误

**预期响应：**
- 系统应该能够解析阿里云百炼API的错误响应
- 返回适当的错误信息

**HTTP状态码：** 503

---

## Mock 阿里云百炼API错误响应示例

### 认证失败响应
```json
{
  "error_message": "AUTHORIZATION_ERROR",
  "time_used": 0
}
```

### 请求频率超限
```json
{
  "error_message": "CONCURRENCY_LIMIT_EXCEEDED",
  "time_used": 0
}
```

### 图片格式错误
```json
{
  "error_message": "INVALID_IMAGE",
  "time_used": 0
}
```

## 测试结果记录

| 测试用例ID | 测试场景 | 状态 | 实际响应 | 备注 |
|-----------|---------|------|---------|------|
| TC-MOCK-001 | API认证失败 | | | |
| TC-MOCK-002 | API网络超时 | | | |
| TC-MOCK-003 | API返回错误 | | | |

**测试执行人：** _____________  
**测试日期：** _____________  
**测试方法：** _____________  

## 注意事项

1. 执行Mock测试后，务必恢复正常的API配置
2. 记录所有错误响应的详细信息
3. 验证前端能够正确显示错误信息
4. 确保错误不会导致系统崩溃

## 参考代码

查看错误处理实现：
- `src/main/java/webcam/exception/GlobalExceptionHandler.java`
- `src/main/java/webcam/exception/BailianApiException.java`
- `src/main/java/webcam/service/FaceRecognitionServiceImpl.java`

