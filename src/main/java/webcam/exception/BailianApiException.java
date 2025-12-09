package webcam.exception;

/**
 * 阿里云百炼API异常
 * 当调用阿里云百炼API失败或返回错误响应时抛出此异常
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public class BailianApiException extends RuntimeException {

    /**
     * 使用错误消息构造异常
     * 
     * @param message 错误消息
     */
    public BailianApiException(String message) {
        super(message);
    }

    /**
     * 使用错误消息和原因构造异常
     * 
     * @param message 错误消息
     * @param cause   原因
     */
    public BailianApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

