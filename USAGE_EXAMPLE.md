# 使用示例 (Usage Example)

## 1. 用户流程示例

### 场景：男性用户使用系统

```
用户操作                           系统响应
--------------------------------------------------------------------
1. 访问首页 index.html
                                  显示"我是男生"和"我是女生"按钮
                                  
2. 点击"我是男生"按钮
                                  跳转到 capture.html?gender=male
                                  启动摄像头，显示倒计时
                                  
3. 拍照或上传照片
                                  POST /webcam
                                  - 参数: image (Base64), gender (male)
                                  
4. 系统处理
                                  a) 保存图片到 upload/
                                  b) 调用 Face++ API 识别
                                  c) 返回识别结果
                                  
5. 显示结果
                                  - 用户照片：左侧
                                  - 男明星照片：右侧 (同性匹配)
                                  - 属性：性别、年龄、笑容、眼镜
                                  - 匹配描述
```

## 2. API 调用示例

### 前端 JavaScript (capture.js)

```javascript
// 用户选择性别
const gender = getUrlParameter('gender'); // 'male' 或 'female'
state.selectedGender = gender;

// 发送图片到服务器
async function sendImageToServer(imageData) {
    const formData = new URLSearchParams();
    formData.append('image', imageData);
    
    // 添加性别参数 (关键！)
    if (state.selectedGender) {
        formData.append('gender', state.selectedGender);
    }
    
    const response = await fetch('/webcam', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    });
    
    const result = await response.json();
    // result.msg 包含性别、年龄等信息
    // result.msg.userGender 包含用户选择的性别
}
```

### 后端处理流程

```java
// 1. WebcamController 接收请求
@PostMapping
public ResponseEntity<ApiResponse<Map<String, Object>>> processImage(
    @RequestParam("image") String imageData,
    @RequestParam(value = "gender", required = false) String gender) {
    
    // 保存图片
    Path filePath = imageStorageService.saveBase64Image(base64Data, fileName);
    
    // Face++ 识别
    Map<String, Object> faceAttributes = 
        faceRecognitionService.detectFaceAttributes(filePath);
    
    // 添加用户选择的性别
    if (gender != null) {
        faceAttributes.put("userGender", gender);
    }
    
    return ApiResponse.success(faceAttributes);
}

// 2. ResultController 处理结果显示
@GetMapping
public String showResult(@RequestParam("msg") String msg, Model model) {
    JsonNode jsonObject = objectMapper.readTree(msg);
    
    String detectedGender = jsonObject.get("gender").asText(); // "男性" 或 "女性"
    String userGender = jsonObject.get("userGender").asText();  // "male" 或 "female"
    
    // 获取匹配的明星照片 (同性匹配)
    String celebrityPhoto = selectCelebrityPhoto(userGender, detectedGender);
    
    model.addAttribute("ppei", celebrityPhoto);
    return "result";
}

// 3. CelebrityPhotoService 选择明星照片
@Override
public String getRandomCelebrityPhoto(String gender) {
    List<String> photoList = "female".equalsIgnoreCase(gender)
        ? celebrityProperties.getFemalePhotos()
        : celebrityProperties.getMalePhotos();
    
    int index = random.nextInt(photoList.size());
    return photoList.get(index);
}
```

## 3. 性别匹配逻辑

### 修复前（错误）
```
用户选择：我是男生 (male)
    ↓
匹配结果：女明星照片 (female/)  ❌ 错误！
```

### 修复后（正确）
```
用户选择：我是男生 (male)
    ↓
匹配结果：男明星照片 (male/)  ✅ 正确！

用户选择：我是女生 (female)
    ↓
匹配结果：女明星照片 (female/)  ✅ 正确！
```

## 4. 明星照片示例

### 男明星 (10位)
```
1. https://img1.baidu.com/it/u=2839653127,3970469241&... (吴京)
2. https://img2.baidu.com/it/u=3716951733,2758164488&... (胡歌)
3. https://img1.baidu.com/it/u=1960452892,2313764692&... (黄晓明)
...
10. https://img0.baidu.com/it/u=3988550846,2749428658&... (李易峰)
```

### 女明星 (10位)
```
1. https://img0.baidu.com/it/u=3775428652,2868156394&... (刘亦菲)
2. https://img2.baidu.com/it/u=4164373443,1565726825&... (杨幂)
3. https://img1.baidu.com/it/u=2867906828,3712538536&... (赵丽颖)
...
10. https://img0.baidu.com/it/u=1546721785,3842654827&... (周迅)
```

## 5. 降级方案

如果网络明星照片加载失败，系统会自动使用本地图片：

```java
// 如果 celebrityPhotoUrl 为 null 或空
if (celebrityPhotoUrl == null || celebrityPhotoUrl.isEmpty()) {
    int randomNum = random.nextInt(10) + 1;
    celebrityPhotoUrl = matchGender + "/" + randomNum + ".png";
    // 例如: "male/3.png" 或 "female/7.png"
    logger.warn("Failed to get celebrity photo, using fallback local image");
}
```

## 6. JSP 结果页面示例

```jsp
<!-- result.jsp -->
<div class="user-image left">
    <img src="${img}" alt="用户照片" width="205" height="205">
</div>

<div class="user-image right">
    <!-- ppei 现在是网络明星照片URL或本地图片路径 -->
    <img src="${ppei}" alt="匹配照片" width="205" height="205">
</div>

<div class="main-content">
    <p>性别：<span class="p11">${gender}</span></p>
    <p>年龄：<span class="p44">${age}</span></p>
    <p>笑容：<span class="p33">${smile}</span></p>
    <p>眼镜：<span class="p22">${eyestatus}</span></p>
    <p>描述：<span class="p55">${pdesc}</span></p>
</div>
```

## 7. 配置示例

### application.properties

```properties
# Face++ API 配置
faceplusplus.api.url=https://api-cn.faceplusplus.com/facepp/v3/detect
faceplusplus.api.key=${FACEPLUSPLUS_API_KEY:your_api_key}
faceplusplus.api.secret=${FACEPLUSPLUS_API_SECRET:your_api_secret}

# 图片上传配置
webcam.upload.path=upload/
webcam.upload.base-url=http://localhost:8080/upload/

# 明星照片配置（可选，默认使用代码中的URL列表）
# celebrity.male-photos[0]=https://custom-cdn.com/male1.jpg
# celebrity.female-photos[0]=https://custom-cdn.com/female1.jpg
```

## 8. 测试示例

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=CelebrityPhotoServiceTest
mvn test -Dtest=ResultControllerTest

# 运行应用
mvn spring-boot:run

# 访问应用
open http://localhost:8080/index.html
```

## 9. 完整请求/响应示例

### 请求
```http
POST /webcam HTTP/1.1
Content-Type: application/x-www-form-urlencoded

image=data:image/jpeg;base64,/9j/4AAQSkZJRg...&gender=male
```

### 响应
```json
{
  "result": "1",
  "msg": {
    "gender": "男性",
    "age": 25,
    "smile": "微笑",
    "eyestatus": "不带眼镜并且睁眼",
    "img": "http://localhost:8080/upload/uuid.jpeg",
    "userGender": "male",
    "faceToken": "abc123..."
  },
  "processingTime": 1234,
  "requestId": "uuid-request"
}
```

### 结果页面请求
```http
GET /result?msg=%7B%22gender%22%3A%22男性%22...%7D HTTP/1.1
```

### 模型属性
```java
model.addAttribute("img", "http://localhost:8080/upload/uuid.jpeg");
model.addAttribute("ppei", "https://img1.baidu.com/it/u=2839653127..."); // 男明星照片
model.addAttribute("gender", "男性");
model.addAttribute("age", "25");
model.addAttribute("smile", "微笑");
model.addAttribute("eyestatus", "不带眼镜并且睁眼");
model.addAttribute("pdesc", "...匹配描述...");
```

---

**提示**: 这个示例展示了完整的用户流程和数据流，从前端选择性别到最终显示匹配的明星照片。
