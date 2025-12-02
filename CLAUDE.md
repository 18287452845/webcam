# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Modernized Spring Boot 3.2.0 WAR application implementing webcam-based face recognition ("NXNS云匹配认别系统"). Uses HTML5 MediaDevices API for camera access and Face++ API for face detection and attribute analysis.

**Version**: 2.0.0-SNAPSHOT
**Java Version**: 17 LTS
**Build Tool**: Maven 3.6+

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────┐
│  Frontend (src/main/webapp/)         │
│  - index.html, capture.html         │
│  - result.jsp                       │
│  - HTML5 MediaDevices API           │
└──────────────┬──────────────────────┘
               │ HTTP
┌──────────────▼──────────────────────┐
│  Controller Layer                    │
│  - WebcamController (/webcam)       │
│  - ResultController (/result)       │
│  - GlobalExceptionHandler           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Service Layer                       │
│  - FaceRecognitionService           │
│  - ImageStorageService              │
│  - MapUtil (utility)                │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Configuration Layer                 │
│  - FacePlusPlusProperties           │
│  - UploadProperties                 │
│  - RestTemplateConfig               │
│  - WebConfig                        │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  External APIs                       │
│  - Face++ API (face detection)      │
│  - Image storage (local filesystem) │
└─────────────────────────────────────┘
```

### Key Components

**Controller Layer** (`src/main/java/webcam/controller/`)
- `WebcamController.java:25-108` - REST endpoint for image processing
- `ResultController.java` - Result page rendering
- `ApiResponse.java` - Standardized API response wrapper
- `GlobalExceptionHandler.java` - Centralized exception handling

**Service Layer** (`src/main/java/webcam/service/`)
- `FaceRecognitionService.java` - Interface for face recognition
- `FaceRecognitionServiceImpl.java:27-213` - Face++ API integration
- `ImageStorageService.java` - Interface for image storage
- `ImageStorageServiceImpl.java:23-105` - Base64 image handling and storage

**Configuration** (`src/main/java/webcam/config/`)
- `FacePlusPlusProperties.java:11-60` - Face++ API configuration
- `UploadProperties.java` - Image upload configuration
- `RestTemplateConfig.java` - HTTP client configuration
- `WebConfig.java` - Web MVC configuration

**Frontend** (`src/main/webapp/`)
- `index.html` - Entry point with gender selection
- `capture.html:1-67` - Camera capture with HTML5 MediaDevices API
- `WEB-INF/views/result.jsp:1-105` - Results display with animations
- Static resources: `css/`, `js/`, `images/`, `saomiao.gif`

## Common Development Commands

### Building
```bash
# Clean and compile
mvn clean compile

# Build WAR file
mvn clean package
# Output: target/webcam.war

# Build with tests
mvn clean verify

# Skip tests during build
mvn clean package -DskipTests
```

### Running
```bash
# Run Spring Boot application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Run on specific port
mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dserver.port=8080

# Debug mode
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### Testing
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=WebcamControllerTest

# Run with coverage report
mvn test jacoco:report

# View coverage report (generated in target/site/jacoco/)
open target/site/jacoco/index.html

# Run tests in specific package
mvn test -Dtest="webcam.service.*"

# Run tests with verbose output
mvn test -X

# Run integration tests only (if configured)
mvn test -Dtest="*IntegrationTest"
```

### Code Quality
```bash
# Format code (if configured with spotless/format plugin)
mvn spotless:check
mvn spotless:apply

# Check for dependencies updates
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates

# Analyze dependencies
mvn dependency:tree
mvn dependency:analyze
```

## Configuration

### Environment Variables
```bash
# Face++ API credentials (override application.properties)
export FACEPLUSPLUS_API_KEY=your_api_key
export FACEPLUSPLUS_API_SECRET=your_api_secret

# Upload path
export WEBCAM_UPLOAD_PATH=/custom/upload/path

# Server port
export SERVER_PORT=8080
```

### Application Profiles

**Development** (`application-dev.properties`)
- Debug logging enabled
- Local upload path: `upload/`
- Base URL: `http://localhost:8080/upload/`

**Production** (`application-prod.properties`)
- Info level logging
- Configurable upload path
- Production-ready settings

## File Structure

```
src/
├── main/
│   ├── java/webcam/
│   │   ├── WebcamApplication.java:15-26          # Main entry point
│   │   ├── controller/                            # REST controllers
│   │   │   ├── WebcamController.java              # Image processing endpoint
│   │   │   ├── ResultController.java              # Result rendering
│   │   │   └── ApiResponse.java                   # Response wrapper
│   │   ├── service/                               # Business logic
│   │   │   ├── FaceRecognitionService.java        # Interface
│   │   │   ├── FaceRecognitionServiceImpl.java    # Face++ integration
│   │   │   ├── ImageStorageService.java           # Interface
│   │   │   └── ImageStorageServiceImpl.java       # File operations
│   │   ├── config/                                # Configuration
│   │   │   ├── FacePlusPlusProperties.java        # API properties
│   │   │   ├── UploadProperties.java              # Upload config
│   │   │   ├── RestTemplateConfig.java            # HTTP client
│   │   │   └── WebConfig.java                     # MVC config
│   │   ├── exception/                             # Exception handling
│   │   │   ├── GlobalExceptionHandler.java        # Centralized handler
│   │   │   ├── FacePlusPlusApiException.java      # API errors
│   │   │   ├── FileStorageException.java          # Storage errors
│   │   │   └── ImageProcessingException.java      # Processing errors
│   │   └── MapUtil.java                           # Utility for face attributes
│   ├── resources/
│   │   ├── application.properties                 # Main configuration
│   │   ├── application-dev.properties             # Dev overrides
│   │   └── application-prod.properties            # Prod overrides
│   └── webapp/
│       ├── index.html                             # Entry page
│       ├── capture.html                           # Camera capture
│       ├── WEB-INF/views/result.jsp               # Results view
│       ├── css/                                   # Stylesheets
│       ├── js/capture.js                          # Camera logic
│       └── images/                                # Static images
└── test/
    └── java/webcam/
        ├── controller/WebcamControllerTest.java   # Controller tests
        ├── service/ImageStorageServiceTest.java   # Service tests
        └── util/MapUtilTest.java                  # Utility tests
```

## Development Workflow

### 1. Initial Setup
```bash
# Clone and setup
git clone <repo>
cd webcam

# Configure API keys
cp src/main/resources/application.properties src/main/resources/application-dev.properties
# Edit application-dev.properties with your Face++ API credentials

# Build and run
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Making Changes
- **Backend changes**: Modify Java files in `src/main/java/webcam/`
- **Frontend changes**: Modify HTML/JS/CSS in `src/main/webapp/`
- **Configuration**: Edit `application*.properties` files
- **Rebuild required**: Run `mvn clean compile` after changes to copy webapp resources

### 3. Testing Changes
```bash
# Run tests
mvn test

# Test specific feature
mvn test -Dtest=WebcamControllerTest#testProcessImage_Success

# Generate coverage report
mvn clean test jacoco:report
```

### 4. Debugging
```bash
# Enable debug logging
# Edit application.properties:
logging.level.webcam=DEBUG
logging.level.org.springframework.web=DEBUG

# Run with debugger
mvn spring-boot:run

# Attach debugger on port 5005 (IntelliJ/Eclipse)
```

## Image Processing Flow

1. **Frontend** (`capture.html:63`)
   - User grants camera permission (MediaDevices API)
   - 30-second countdown with animation
   - Canvas captures frame as base64 JPEG
   - POST to `/webcam` endpoint

2. **Controller** (`WebcamController.java:53-108`)
   - Validates image data
   - Extracts base64 payload
   - Generates unique filename (UUID)
   - Delegates to services

3. **Storage Service** (`ImageStorageServiceImpl.java:36-65`)
   - Creates upload directory if needed
   - Decodes base64 to binary
   - Saves to `upload/{uuid}.jpeg`
   - Returns file path

4. **Recognition Service** (`FaceRecognitionServiceImpl.java:46-133`)
   - Calls Face++ API via RestTemplate
   - Sends multipart request with image_file
   - Receives JSON response
   - Parses face attributes (gender, age, smile, eyestatus)

5. **Response** (`WebcamController.java:80-101`)
   - Returns ApiResponse with face attributes
   - Adds image URL to response
   - Includes request ID and processing time
   - Sets cache-control headers

6. **Result Display** (`result.jsp:23-35`)
   - JSP renders face attributes with animations
   - Displays user image and match description
   - Auto-refreshes after 60 seconds

## Key Technologies

- **Spring Boot 3.2.0** - Application framework, auto-configuration
- **Java 17** - Language (records, var, pattern matching)
- **Jackson** - JSON serialization (jackson-databind)
- **RestTemplate** - HTTP client for Face++ API
- **SLF4J + Logback** - Logging framework
- **JUnit 5** - Testing framework with Mockito
- **JaCoCo** - Code coverage analysis
- **HTML5 MediaDevices API** - Camera access (modern replacement for Flash)
- **JSP + JSTL** - Server-side view rendering

## Known Issues & Solutions

1. **Static resources 404**
   - **Cause**: Webapp files not copied to build output
   - **Solution**: Run `mvn clean compile` (maven-resources-plugin handles this)

2. **DataSource configuration error**
   - **Cause**: Spring Boot tries to configure database
   - **Solution**: Already fixed via `@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)`

3. **Camera access denied**
   - **Cause**: HTTPS required for production, permission denied
   - **Solution**: Use localhost for dev, grant camera permission, use HTTPS in production

4. **Face++ API errors**
   - **Cause**: Invalid API keys, quota exceeded, or network issues
   - **Solution**: Check logs, verify API keys in `application.properties`

5. **Image upload fails**
   - **Cause**: Upload directory not writable, file too large
   - **Solution**: Check `webcam.upload.path` configuration, verify permissions

## API Reference

### POST /webcam
Processes uploaded image through Face++ API.

**Request**:
- Content-Type: `application/x-www-form-urlencoded`
- Parameter: `image` (base64 encoded JPEG, with or without data:image prefix)

**Response**:
```json
{
  "result": "1",
  "msg": {
    "gender": "男性",
    "age": 25,
    "smile": "微笑",
    "eyestatus": "不带眼镜并且睁眼",
    "img": "http://localhost:8080/upload/{uuid}.jpeg"
  },
  "processingTime": 1234,
  "requestId": "uuid"
}
```

**Error Response**:
```json
{
  "result": "0",
  "errorCode": "ERROR_CODE",
  "errorDetail": "Human readable error message"
}
```

### GET /result
Renders result page (JSP view).

**Query Parameters**: Attributes from face detection passed as request attributes

## Troubleshooting

### Build Errors
```bash
# Clean build
mvn clean install -U

# Clear Maven cache
mvn dependency:purge-local-repository

# Rebuild dependencies
mvn clean install -DskipTests
```

### Test Failures
```bash
# Run single test with verbose output
mvn test -Dtest=ClassName#methodName -X

# Skip failing tests
mvn test -DfailIfNoTests=false

# Check test reports
cat target/surefire-reports/*.txt
```

### Runtime Issues
```bash
# Check logs
tail -f logs/application.log

# Verify configuration
curl http://localhost:8080/actuator/info  # if actuator enabled

# Test API endpoint directly
curl -X POST http://localhost:8080/webcam \
  -d "image=data:image/jpeg;base64,/9j/4AAQSkZJRg..."
```

## Migration Notes

**Legacy Version → Modern Version**
- Java 1.6 → Java 17 (use modern features like `var`, records)
- Servlets → Spring Boot (use @RestController, dependency injection)
- Flash → HTML5 MediaDevices API (modern browser APIs)
- jQuery AJAX → Fetch API (native JavaScript)
- json-lib → Jackson (Spring Boot default)
- Apache HttpClient 4.x → RestTemplate (Spring's HTTP client)
- System.out.println → SLF4J (use structured logging)

**File Mappings**:
- `sec.html` → `capture.html` (HTML5 version)
- `WebcamServlet.java` → `WebcamController.java`
- `ResultServlet.java` → `ResultController.java`
- `third.jsp` → `WEB-INF/views/result.jsp`

---

**Last Updated**: 2025-12-02
**Version**: 2.0.0-SNAPSHOT
