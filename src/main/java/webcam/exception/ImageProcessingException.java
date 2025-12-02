package webcam.exception;

/**
 * 图像处理异常
 * 当图像解码、格式验证或处理失败时抛出此异常
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public class ImageProcessingException extends RuntimeException {

    /**
     * 使用错误消息构造异常
     * 
     * @param message 错误消息
     */
    public ImageProcessingException(String message) {
        super(message);
    }

    /**
     * 使用错误消息和原因构造异常
     * 
     * @param message 错误消息
     * @param cause   原因
     */
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
