package webcam.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import webcam.service.impl.QrCodeServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 二维码生成服务测试
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@DisplayName("QrCodeService Tests")
class QrCodeServiceTest {
    
    private QrCodeService qrCodeService;
    
    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeServiceImpl();
    }
    
    @Test
    @DisplayName("Should generate valid QR code from URL")
    void testGenerateQrCodeBase64() {
        String testUrl = "https://example.com/image.jpg";
        
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(testUrl);
        
        assertNotNull(qrCodeBase64);
        assertTrue(qrCodeBase64.startsWith("data:image/png;base64,"));
        assertTrue(qrCodeBase64.length() > 100);
    }
    
    @Test
    @DisplayName("Should generate QR code with custom dimensions")
    void testGenerateQrCodeBase64WithDimensions() {
        String testUrl = "https://example.com/image.jpg";
        int width = 400;
        int height = 400;
        
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(testUrl, width, height);
        
        assertNotNull(qrCodeBase64);
        assertTrue(qrCodeBase64.startsWith("data:image/png;base64,"));
    }
    
    @Test
    @DisplayName("Should generate QR code for presigned URL")
    void testGenerateQrCodeForPresignedUrl() {
        String presignedUrl = "https://bucket.r2.cloudflarestorage.com/cartoon/uuid-timestamp.jpeg?" +
                "X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=test&X-Amz-Date=20231201T120000Z";
        
        String qrCodeBase64 = qrCodeService.generateQrCodeBase64(presignedUrl);
        
        assertNotNull(qrCodeBase64);
        assertTrue(qrCodeBase64.startsWith("data:image/png;base64,"));
    }
}
