package webcam.service;

/**
 * 二维码生成服务接口
 * 用于生成二维码图片或获取二维码内容
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public interface QrCodeService {
    
    /**
     * 生成二维码并返回Base64编码的图片
     * 
     * @param content 二维码内容（通常是URL）
     * @param width 二维码宽度（像素）
     * @param height 二维码高度（像素）
     * @return Base64编码的PNG图片数据（包含data:image/png;base64,前缀）
     * @throws Exception 当二维码生成失败时
     */
    String generateQrCodeBase64(String content, int width, int height);
    
    /**
     * 生成二维码并返回Base64编码的图片（使用默认尺寸）
     * 
     * @param content 二维码内容（通常是URL）
     * @return Base64编码的PNG图片数据（包含data:image/png;base64,前缀）
     * @throws Exception 当二维码生成失败时
     */
    String generateQrCodeBase64(String content);
    
    /**
     * 生成二维码并保存到文件
     * 
     * @param content 二维码内容
     * @param filePath 文件保存路径
     * @param width 二维码宽度（像素）
     * @param height 二维码高度（像素）
     * @throws Exception 当二维码生成或保存失败时
     */
    void generateQrCodeFile(String content, String filePath, int width, int height);
}
