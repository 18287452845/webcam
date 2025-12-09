# 前后端联调测试资源

本目录包含前后端联调测试的所有资源和脚本。

## 文件说明

- `test-scripts.ps1` - PowerShell测试脚本（Windows）
- `test-scripts.sh` - Bash测试脚本（Linux/Mac）
- `api-test-results.json` - API测试结果（JSON格式，由PowerShell脚本生成）
- `api-test-results.txt` - API测试结果（文本格式，由Bash脚本生成）

## 使用方法

### Windows环境

1. 确保后端服务已启动（运行在 http://localhost:8080）
2. 打开PowerShell
3. 运行测试脚本：

```powershell
cd test-resources
.\test-scripts.ps1
```

### Linux/Mac环境

1. 确保后端服务已启动（运行在 http://localhost:8080）
2. 给脚本添加执行权限：

```bash
chmod +x test-resources/test-scripts.sh
```

3. 运行测试脚本：

```bash
./test-resources/test-scripts.sh
```

## 测试用例覆盖

### API接口测试（POST /webcam）
- TC-API-001: 发送有效Base64图片（带前缀）
- TC-API-002: 发送纯Base64字符串（无前缀）
- TC-API-003: 空图片参数
- TC-API-004: 无效Base64数据
- TC-API-005: 无人脸图片

### 结果页面API测试（GET /result）
- TC-RESULT-001: 有效JSON参数
- TC-RESULT-002: 空参数
- TC-RESULT-003: 无效JSON

## 注意事项

1. 测试前确保后端服务已启动
2. 某些测试用例可能依赖阿里云百炼API的实际响应（如人脸检测）
3. 测试结果会保存到相应的结果文件中
4. 如果测试失败，请检查：
   - 后端服务是否正常运行
   - 端口号是否正确（默认8080）
   - 网络连接是否正常
   - 阿里云百炼API配置是否正确

