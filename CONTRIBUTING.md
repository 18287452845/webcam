# 贡献指南

感谢您对Webcam人脸识别系统项目的关注！我们欢迎所有形式的贡献。

## 🤝 如何贡献

### 报告Bug

如果您发现了bug，请：

1. 检查[Issues](链接)中是否已有相关报告
2. 如果没有，创建新的Issue
3. 提供以下信息：
   - 详细的bug描述
   - 复现步骤
   - 预期行为
   - 实际行为
   - 环境信息（OS、Java版本、浏览器等）
   - 错误日志（如果有）

### 提出功能建议

如果您有功能改进建议：

1. 检查是否已有相关Issue
2. 创建新的Feature Request Issue
3. 详细描述：
   - 功能需求
   - 使用场景
   - 预期效果
   - 可能的实现方案

### 提交代码

1. **Fork项目**
2. **创建特性分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **编写代码**
   - 遵循代码规范
   - 添加必要的注释
   - 编写测试用例
4. **提交更改**
   ```bash
   git commit -m "Add: 描述你的更改"
   ```
5. **推送到分支**
   ```bash
   git push origin feature/your-feature-name
   ```
6. **创建Pull Request**

## 📝 代码规范

### Java代码规范

- 遵循Google Java Style Guide或Oracle Java Code Conventions
- 使用有意义的变量和方法名
- 添加必要的JavaDoc注释
- 保持方法简洁（建议不超过50行）
- 避免深层嵌套（建议不超过3层）

### 提交信息规范

使用以下格式：

```
<type>: <subject>

<body>

<footer>
```

**Type类型**:
- `feat`: 新功能
- `fix`: Bug修复
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

**示例**:
```
feat: 添加用户认证功能

- 集成Spring Security
- 添加登录/注册页面
- 实现JWT Token认证

Closes #123
```

## 🧪 测试要求

- 新功能必须包含测试用例
- 修复bug必须包含回归测试
- 测试覆盖率不应降低
- 所有测试必须通过

## 📚 文档要求

- 更新相关文档（README、API文档等）
- 添加代码注释
- 更新CHANGELOG（如果有）

## ✅ Pull Request检查清单

在提交PR前，请确认：

- [ ] 代码遵循项目规范
- [ ] 所有测试通过
- [ ] 添加了必要的测试用例
- [ ] 更新了相关文档
- [ ] 提交信息清晰明确
- [ ] 没有引入新的警告或错误
- [ ] 代码已通过代码审查

## 🎯 优先级功能

如果您想贡献但不知道从何开始，可以考虑：

1. 修复[Good First Issues](链接)
2. 改进文档
3. 添加测试用例
4. 优化性能
5. 改进用户体验

## 📞 联系方式

如有问题，可以通过以下方式联系：

- 创建Issue
- 发送邮件
- 参与讨论

再次感谢您的贡献！🎉

