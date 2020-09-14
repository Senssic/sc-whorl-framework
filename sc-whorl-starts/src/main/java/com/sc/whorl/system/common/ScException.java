package com.sc.whorl.system.common;

public class ScException extends RuntimeException {
    private Integer resultCode;

    public ScException(Integer code, String message) {
        super(message);
        this.resultCode = code;
    }

    public ScException(String message) {
        super(message);
        this.resultCode = RT.INTERNAL_SERVER_ERROR;
    }

    public ScException(Integer code, Throwable cause) {
        super(cause);
        this.resultCode = code;
    }

    public ScException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = code;
    }

    public Integer getResultCode() {
        return this.resultCode;
    }
}
