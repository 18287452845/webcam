# Face++ API 密钥验证测试结果

**测试时间：** 2025-11-30  
**测试方法：** 多种方式验证Face++ API密钥有效性

---

## 测试结果总结

### ❌ API密钥验证失败

**测试结论：** Face++ API密钥**可能无效**或**存在配置问题**

**证据：**
1. 直接调用Face++ API返回 **HTTP 403 Forbidden**
2. 通过后端服务调用返回 **HTTP 503 Service Unavailable**
3. 错误信息：`人脸识别服务调用失败: 人脸检测失败`

---

## 详细测试记录

### 测试1: 直接调用Face++ API（Multipart方式）
- **方法：** 使用multipart/form-data上传图片文件
- **结果：** HTTP 403
- **错误响应：** 空响应或无法读取

### 测试2: 直接调用Face++ API（Base64方式）
- **方法：** 使用image_base64参数
- **结果：** HTTP 403
- **错误响应：** 空响应

### 测试3: 通过后端服务调用
- **方法：** 通过已配置的后端API `/webcam` 端点
- **结果：** HTTP 503
- **错误响应：** 
```json
{
  "result": "0",
  "errorCode": "FACE_API_ERROR",
  "errorDetail": "人脸识别服务调用失败: 人脸检测失败"
}
```

---

## 可能的原因

### 1. API密钥无效 ⚠️ 最可能
- API Key或API Secret配置错误
- 密钥已被删除或禁用
- 密钥类型不匹配（免费版/付费版）

### 2. API账户问题
- 账户余额不足
- 账户被暂停
- 账户权限不足

### 3. API配额限制
- 免费配额已用完
- 达到调用频率限制
- 需要升级账户

### 4. 网络/配置问题
- 网络连接问题
- API端点URL配置错误
- 请求格式问题

---

## 建议的解决方案

### 步骤1: 验证API密钥
1. 登录Face++控制台：https://console.faceplusplus.com/
2. 检查API Key和API Secret是否正确
3. 确认账户状态和余额

### 步骤2: 检查API配额
1. 查看API调用统计
2. 确认是否还有剩余配额
3. 检查是否有调用频率限制

### 步骤3: 测试新的API密钥
1. 如果密钥无效，生成新的API Key和Secret
2. 更新 `application.properties` 配置文件：
   ```properties
   faceplusplus.api.key=你的新API_KEY
   faceplusplus.api.secret=你的新API_SECRET
   ```
3. 重启后端服务

### 步骤4: 验证配置
1. 确认API端点URL正确：
   ```properties
   faceplusplus.api.url=https://api-cn.faceplusplus.com/facepp/v3/detect
   ```
2. 检查网络连接是否正常
3. 确认防火墙没有阻止访问

---

## 测试脚本

已创建以下测试脚本用于验证API密钥：

1. **test-faceplusplus-api.ps1** - 直接调用Face++ API（multipart方式）
2. **test-faceplusplus-via-backend.ps1** - 通过后端服务测试
3. **diagnose-faceplusplus.ps1** - 详细诊断脚本

### 使用方法：
```powershell
cd test-resources
.\diagnose-faceplusplus.ps1
```

---

## 下一步行动

1. ✅ **立即检查** Face++控制台的API密钥配置
2. ✅ **验证账户状态**（余额、配额、权限）
3. ⚠️ **如果密钥无效**，生成新密钥并更新配置
4. ⚠️ **如果配额用完**，考虑升级账户或等待配额重置
5. ✅ **重新运行测试**脚本验证修复

---

## 联系支持

如果问题持续存在，可以：
- 查看Face++官方文档：https://console.faceplusplus.com/documents
- 联系Face++技术支持
- 检查Face++服务状态页面

---

**测试执行人：** Auto Test System  
**报告生成时间：** 2025-11-30

