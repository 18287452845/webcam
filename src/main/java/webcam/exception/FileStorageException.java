package webcam.exception;

/**
 * 文件存储异常
 * 当文件保存失败、权限不足或磁盘空间不足时抛出此异常
 * 
 * @author Webcam Application
 * @version 2.0.0
 */
public class FileStorageException extends RuntimeException {

    /**
     * 使用错误消息构造异常
     * 
     * @param message 错误消息
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * 使用错误消息和原因构造异常
     * 
     * @param message 错误消息
     * @param cause   原因
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
