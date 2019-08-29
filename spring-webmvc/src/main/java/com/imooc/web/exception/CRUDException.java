package com.imooc.web.exception;

/**
 * @description:
 * @author: sunyingji
 * @create: 2019-08-29 17:33
 **/
public class CRUDException extends BaseException {

    public CRUDException() {
        super();
    }

    public CRUDException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CRUDException(String message, Throwable cause) {
        super(message, cause);
    }

    public CRUDException(String message) {
        super(message);
    }

    public CRUDException(Throwable cause) {
        super(cause);
    }

    public CRUDException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                         String code) {
        super(message, cause, enableSuppression, writableStackTrace, code);
    }

    public CRUDException(String message, Throwable cause, String code) {
        super(message, cause, code);
    }

    public CRUDException(String message, String code) {
        super(message, code);
    }

    public CRUDException(Throwable cause, String code) {
        super(cause, code);
    }
}
