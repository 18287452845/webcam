package webcam.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 统一API响应格式
 * 用于标准化所有API的响应结构
 * 
 * @author Webcam Application
 * @version 2.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应状态码
     * "1" 表示成功，"0" 表示失败
     */
    private String result;

    /**
     * 响应数据/消息
     */
    private T msg;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 处理时间（毫秒）
     */
    private Long processingTime;

    /**
     * 错误代码（仅错误时）
     */
    private String errorCode;

    /**
     * 错误详情（仅错误时）
     */
    private String errorDetail;

    /**
     * 请求ID（用于追踪）
     */
    private String requestId;

    /**
     * 默认构造函数
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 成功响应构造函数
     * 
     * @param data 响应数据
     */
    public ApiResponse(T data) {
        this();
        this.result = "1";
        this.msg = data;
    }

    /**
     * 成功响应构造函数（带处理时间）
     * 
     * @param data 响应数据
     * @param startTime 开始时间
     */
    public ApiResponse(T data, LocalDateTime startTime) {
        this(data);
        if (startTime != null) {
            this.processingTime = ChronoUnit.MILLIS.between(startTime, this.timestamp);
        }
    }

    /**
     * 错误响应构造函数
     * 
     * @param errorCode 错误代码
     * @param errorDetail 错误详情
     */
    public ApiResponse(String errorCode, String errorDetail) {
        this();
        this.result = "0";
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.msg = null;
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }

    /**
     * 创建成功响应（带处理时间）
     * 
     * @param data 响应数据
     * @param startTime 开始时间
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> success(T data, LocalDateTime startTime) {
        return new ApiResponse<>(data, startTime);
    }

    /**
     * 创建错误响应
     * 
     * @param errorCode 错误代码
     * @param errorDetail 错误详情
     * @return ApiResponse实例
     */
    public static <T> ApiResponse<T> error(String errorCode, String errorDetail) {
        return new ApiResponse<>(errorCode, errorDetail);
    }

    /**
     * 创建错误响应（兼容旧格式）
     * 
     * @param errorMessage 错误消息
     * @return ApiResponse实例
     */
    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String errorMessage) {
        ApiResponse<T> response = new ApiResponse<>();
        response.result = "0";
        response.msg = (T) errorMessage;
        return response;
    }

    // Getters and Setters

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public T getMsg() {
        return msg;
    }

    public void setMsg(T msg) {
        this.msg = msg;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

