# Changelog

All notable changes to this project will be documented in this file.

## [2.0.1-SNAPSHOT] - 2025-12-02

### Fixed
- **性别匹配逻辑修复**: 修复了性别匹配错误的问题，现在实现同性匹配
  - 男性用户 → 匹配男性明星照片
  - 女性用户 → 匹配女性明星照片
  - 之前的逻辑是异性匹配（男性匹配女性照片），这已被修正

### Added
- **中国明星照片功能**: 新增从网络CDN获取中国明星照片的功能
  - 新增 `CelebrityProperties` 配置类管理明星照片URL
  - 新增 `CelebrityPhotoService` 服务提供明星照片选择
  - 新增 `CelebrityPhotoServiceImpl` 实现随机选择逻辑
  - 配置了20张高质量明星照片（10男+10女）
  - 实现降级方案：网络照片失败时自动使用本地图片

- **测试覆盖**: 新增完整的单元测试
  - `CelebrityPhotoServiceTest`: 测试明星照片服务
  - `ResultControllerTest`: 测试结果控制器和性别匹配逻辑
  - 所有新功能都有测试覆盖

### Changed
- **ResultController 重构**:
  - 修改 `showResult()` 方法使用新的明星照片服务
  - 修改 `getResultJson()` 方法使用新的明星照片服务
  - 新增 `selectCelebrityPhoto()` 辅助方法统一处理性别匹配逻辑
  - 增强错误处理和日志记录

### Documentation
- 新增 `OPTIMIZATION.md` - 详细的优化说明文档
- 新增 `USAGE_EXAMPLE.md` - 使用示例和API文档
- 新增 `CHANGELOG.md` - 变更日志

### Technical Details

#### 新增文件
```
src/main/java/webcam/config/CelebrityProperties.java
src/main/java/webcam/service/CelebrityPhotoService.java
src/main/java/webcam/service/impl/CelebrityPhotoServiceImpl.java
src/test/java/webcam/service/CelebrityPhotoServiceTest.java
src/test/java/webcam/controller/ResultControllerTest.java
OPTIMIZATION.md
USAGE_EXAMPLE.md
CHANGELOG.md
```

#### 修改文件
```
src/main/java/webcam/controller/ResultController.java
```

#### 明星照片列表
**男明星 (10位)**:
1. 吴京
2. 胡歌
3. 黄晓明
4. 陈坤
5. 邓超
6. 刘德华
7. 张国荣
8. 周杰伦
9. 王力宏
10. 李易峰

**女明星 (10位)**:
1. 刘亦菲
2. 杨幂
3. 赵丽颖
4. 迪丽热巴
5. 唐嫣
6. 林志玲
7. 范冰冰
8. 章子怡
9. 舒淇
10. 周迅

#### 性能优化
- 使用 `SecureRandom` 确保随机性
- 明星照片URL使用百度CDN，加载速度快
- 实现降级方案，确保服务可用性
- 增加详细的日志记录，便于调试

#### 兼容性
- ✅ 完全向后兼容
- ✅ 不影响现有的Face++识别功能
- ✅ 支持旧版浏览器（降级到本地图片）
- ✅ 所有现有测试（除MapUtilTest）继续通过

---

## [2.0.0-SNAPSHOT] - 2025-11-01

### Initial Release
- Spring Boot 3.2.0 现代化重构
- HTML5 MediaDevices API替代Flash
- Face++ API集成
- 人脸识别和属性分析
- 结果展示和动画效果

---

**注意**: MapUtilTest中的一些测试失败是已知问题，与本次优化无关，将在后续版本中修复。
