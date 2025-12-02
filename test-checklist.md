# 项目测试清单

## ✅ 已完成的测试

### 1. 编译测试
- [x] Maven编译 (`mvn clean compile`) - **通过**
- [x] Maven打包 (`mvn package`) - **通过**
- [x] WAR文件生成 - **成功** (`target/webcam.war`)

### 2. 代码结构检查
- [x] Spring Boot主类存在 - `WebcamApplication.java`
- [x] 控制器类存在 - `WebcamController.java`, `ResultController.java`
- [x] 配置类完整 - 所有配置类已创建
- [x] 前端文件完整 - HTML、CSS、JSP文件存在
- [x] 配置文件完整 - application.properties系列文件存在

### 3. 依赖检查
- [x] Spring Boot 3.2.0
- [x] MyBatis 3.0.3
- [x] Jackson (JSON处理)
- [x] JSP支持
- [x] 日志框架 (SLF4J + Logback)

## ⏳ 需要手动测试的功能

### 启动测试
```bash
# 方式1: 使用Spring Boot Maven插件
mvn spring-boot:run

# 方式2: 部署WAR文件到Tomcat
# 将 target/webcam.war 复制到 Tomcat/webapps/ 目录
# 启动Tomcat服务器
```

**预期结果**: 
- 应用在 http://localhost:8080 启动
- 无启动错误
- 日志显示Spring Boot启动成功

### 前端页面测试

#### 1. 首页测试
- [ ] 访问 http://localhost:8080/index.html
- [ ] 页面正常显示
- [ ] 背景图片和视频正常加载
- [ ] 性别选择按钮可点击

#### 2. 拍照页面测试
- [ ] 点击性别按钮跳转到 capture.html
- [ ] 浏览器请求摄像头权限
- [ ] 允许权限后视频流正常显示
- [ ] 倒计时从30秒开始
- [ ] 倒计时结束后自动拍照
- [ ] 图像上传到服务器

#### 3. 结果页面测试
- [ ] 识别成功后跳转到结果页面
- [ ] 用户照片和匹配照片显示
- [ ] 识别信息正确显示（性别、年龄、笑容等）
- [ ] 文字动画效果正常
- [ ] 60秒后自动返回首页

### API端点测试

#### POST /webcam
**测试数据准备**:
```javascript
// 使用浏览器控制台测试
const canvas = document.createElement('canvas');
canvas.width = 320;
canvas.height = 240;
const ctx = canvas.getContext('2d');
ctx.fillStyle = '#FF0000';
ctx.fillRect(0, 0, 320, 240);
const imageData = canvas.toDataURL('image/jpeg');

// 发送请求
fetch('/webcam', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'image=' + encodeURIComponent(imageData)
})
.then(r => r.json())
.then(console.log);
```

**验证点**:
- [ ] 请求成功返回200状态码
- [ ] 返回JSON格式正确
- [ ] Face++ API调用成功
- [ ] 图像文件保存到upload目录
- [ ] 返回结果包含faceToken、gender、age等字段

#### GET /result
**测试URL**:
```
http://localhost:8080/result?msg=%7B%22img%22%3A%22...%22%2C%22gender%22%3A%22%E7%94%B7%E6%80%A7%22%7D
```

**验证点**:
- [ ] 页面正常渲染
- [ ] JSP表达式正确解析
- [ ] 所有数据正确显示
- [ ] 样式正常加载

### 错误处理测试

#### 摄像头权限拒绝
- [ ] 拒绝摄像头权限时显示友好错误提示
- [ ] 错误信息清晰易懂

#### 无摄像头设备
- [ ] 检测到无摄像头时显示相应提示
- [ ] 应用不会崩溃

#### API调用失败
- [ ] Face++ API调用失败时返回错误信息
- [ ] 错误信息记录到日志
- [ ] 用户看到友好的错误提示

#### 无效图像数据
- [ ] 上传无效Base64数据时返回错误
- [ ] 错误处理正确

### 浏览器兼容性测试

#### Chrome/Edge (推荐)
- [ ] 所有功能正常
- [ ] 摄像头访问正常
- [ ] 样式显示正确

#### Firefox
- [ ] 所有功能正常
- [ ] 摄像头访问正常

#### Safari (如果可用)
- [ ] 基本功能正常
- [ ] 摄像头访问正常

## 🔧 配置验证

### 必需配置检查
- [ ] Face++ API密钥已配置
- [ ] 文件上传路径有写入权限
- [ ] 日志配置正确

### 可选配置
- [ ] 开发/生产环境配置已设置
- [ ] 端口配置正确
- [ ] 字符编码配置正确

## 📝 测试记录

### 测试环境
- **操作系统**: Windows
- **JDK版本**: 17
- **Maven版本**: 3.x
- **浏览器**: [填写测试浏览器]

### 测试结果
- **编译状态**: ✅ 通过
- **打包状态**: ✅ 通过
- **启动状态**: ⏳ 待测试
- **功能测试**: ⏳ 待测试

### 发现的问题
1. [记录问题1]
2. [记录问题2]

### 修复建议
1. [修复建议1]
2. [修复建议2]

---

**最后更新**: 2025-11-29
**测试人员**: [填写姓名]

