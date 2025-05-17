package com.Panda.tiktokDevKit.exception;

public class TiktokApiException extends RuntimeException {
    private int errorCode;
    private String errorMessage;
    private String errorCodeString;

    // constructor with error code and message
    public TiktokApiException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
    
    // constructor with only message
    public TiktokApiException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    // constructor with message and cause
    public TiktokApiException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorMessage = errorMessage;
    }

    // constructor with error code, message, and cause
    public TiktokApiException(int errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}