package com.imooc.web.exception;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:30
 **/
public class BaseException extends Exception {


    /**
     * 错误码
     */
    protected String errorCode;

    /**
     * 获取errorCode字段数据
     * @return Returns the errorCode.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 设置errorCode字段数据
     * @param errorCode The errorCode to set.
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 创建一个新的实例 BaseException
     */
    public BaseException() {
        super();
    }

    /**
     * 创建一个新的实例 BaseException
     * @param message 异常消息
     * @param cause 导致异常的异常
     * @param enableSuppression 是否抑制
     * @param writableStackTrace 是否写入stackTrace
     */
    public BaseException(String message, Throwable cause, boolean enableSuppression,
                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * 创建一个新的实例 BaseException
     * @param message 异常消息
     * @param cause 导致异常的异常
     */
    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建一个新的实例 BaseException
     * @param message 异常消息
     */
    public BaseException(String message) {
        super(message);
    }

    /**
     * 创建一个新的实例 BaseException
     * @param cause 导致异常的异常
     */
    public BaseException(Throwable cause) {
        super(cause);
    }

    /**
     * 创建一个新的实例 BaseException
     * @param message 异常消息
     * @param cause 导致异常的异常
     * @param enableSuppression 是否抑制
     * @param writableStackTrace 是否写入stackTrace
     * @param code 错误码
     */
    public BaseException(String message, Throwable cause, boolean enableSuppression,
                         boolean writableStackTrace, String code) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = code;
    }

    /**
     * 创建一个新的实例 BaseException
     * @param message 异常消息
     * @param cause 导致异常的异常
     * @param code 错误码
     */
    public BaseException(String message, Throwable cause, String code) {
        super(message, cause);
        this.errorCode = code;
    }

    /**
     * 创建一个新的实例 BaseException
     * @param message 异常消息
     * @param code 错误码
     */
    public BaseException(String message, String code) {
        super(message);
        this.errorCode = code;
    }

    /**
     * 创建一个新的实例 BaseException
     * @param cause 导致异常的异常
     * @param code 错误码
     */
    public BaseException(Throwable cause, String code) {
        super(cause);
        this.errorCode = code;
    }

}
