package com.Panda.tiktokDevKit.exception;


public class AuthenticationException extends RuntimeException {
    
    private final String errorCode;
    

    public AuthenticationException(String message) {
        super(message);
        this.errorCode = null;
    }
    
  
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }
    
   
    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
   
    public AuthenticationException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
//