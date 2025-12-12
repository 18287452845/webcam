package webcam.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import webcam.service.QrCodeService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成服务实现
 * 使用Google ZXing库生成二维码
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
@Service
public class QrCodeServiceImpl implements QrCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(QrCodeServiceImpl.class);
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    
    @Override
    public String generateQrCodeBase64(String content, int width, int height) {
        try {
            logger.debug("Generating QR code: content length={}, width={}, height={}", 
                    content.length(), width, height);
            
            // 创建编码提示
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1); // 最小边距
            
            // 生成二维码矩阵
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content, BarcodeFormat.QR_CODE, width, height, hints);
            
            // 转换为PNG图片
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            
            logger.info("QR code generated successfully, size={} bytes", imageBytes.length);
            
            return "data:image/png;base64," + base64Image;
            
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code", e);
            throw new RuntimeException("生成二维码失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String generateQrCodeBase64(String content) {
        return generateQrCodeBase64(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    
    @Override
    public void generateQrCodeFile(String content, String filePath, int width, int height) {
        try {
            logger.debug("Generating QR code file: path={}, width={}, height={}", 
                    filePath, width, height);
            
            // 创建编码提示
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            // 生成二维码矩阵
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content, BarcodeFormat.QR_CODE, width, height, hints);
            
            // 保存为PNG文件
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
            
            logger.info("QR code file generated successfully: {}", filePath);
            
        } catch (WriterException | IOException e) {
            logger.error("Error generating QR code file: {}", filePath, e);
            throw new RuntimeException("生成二维码文件失败: " + e.getMessage(), e);
        }
    }
}
