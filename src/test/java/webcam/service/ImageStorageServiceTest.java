package webcam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import webcam.config.UploadProperties;
import webcam.exception.FileStorageException;
import webcam.exception.ImageProcessingException;

import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ImageStorageService单元测试
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
class ImageStorageServiceTest {

    private ImageStorageService imageStorageService;
    private UploadProperties uploadProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        uploadProperties = new UploadProperties();
        uploadProperties.setPath(tempDir.toString());
        uploadProperties.setBaseUrl("http://localhost:8080/upload/");

        imageStorageService = new ImageStorageServiceImpl(uploadProperties);
    }

    @Test
    void testExtractBase64Data_WithPrefix() {
        String imageData = "data:image/jpeg;base64,SGVsbG8gV29ybGQ=";
        String result = imageStorageService.extractBase64Data(imageData);

        assertEquals("SGVsbG8gV29ybGQ=", result);
    }

    @Test
    void testExtractBase64Data_WithoutPrefix() {
        String imageData = "SGVsbG8gV29ybGQ=";
        String result = imageStorageService.extractBase64Data(imageData);

        assertEquals("SGVsbG8gV29ybGQ=", result);
    }

    @Test
    void testExtractBase64Data_Null() {
        String result = imageStorageService.extractBase64Data(null);

        assertNull(result);
    }

    @Test
    void testGetImageUrl() {
        String fileName = "test.jpeg";
        String result = imageStorageService.getImageUrl(fileName);

        assertEquals("http://localhost:8080/upload/test.jpeg", result);
    }

    @Test
    void testValidateImageData_Valid() {
        String validData = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(new byte[200]);

        assertDoesNotThrow(() -> imageStorageService.validateImageData(validData));
    }

    @Test
    void testValidateImageData_Null() {
        assertThrows(IllegalArgumentException.class,
                () -> imageStorageService.validateImageData(null));
    }

    @Test
    void testValidateImageData_Empty() {
        assertThrows(IllegalArgumentException.class,
                () -> imageStorageService.validateImageData(""));
    }

    @Test
    void testValidateImageData_TooSmall() {
        String tooSmall = "data:image/jpeg;base64,abc";

        assertThrows(IllegalArgumentException.class,
                () -> imageStorageService.validateImageData(tooSmall));
    }

    @Test
    void testSaveBase64Image_Success() {
        String base64Data = Base64.getEncoder().encodeToString("Hello World".getBytes());
        String fileName = "test.jpeg";

        Path result = imageStorageService.saveBase64Image(base64Data, fileName);

        assertNotNull(result);
        assertTrue(result.toFile().exists());
        assertEquals(fileName, result.getFileName().toString());
    }

    @Test
    void testSaveBase64Image_InvalidBase64() {
        String invalidBase64 = "This is not valid base64!!!";
        String fileName = "test.jpeg";

        assertThrows(ImageProcessingException.class,
                () -> imageStorageService.saveBase64Image(invalidBase64, fileName));
    }
}
