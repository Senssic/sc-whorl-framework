package sc.whorl.system.common;

import java.io.Serializable;

public class RT<T> implements Serializable {
    public static final Integer SUCCESS = 0;
    public static final Integer INTERNAL_SERVER_ERROR = -99;
    public static final Integer PARAM_VALID_ERROR = -1;
    private Integer resultCode;
    private T result;

    private RT(Integer resultCode) {
        this.resultCode = resultCode;
    }

    private RT(Integer resultCode, T message) {
        this.resultCode = resultCode;
        this.result = message;
    }

    public static RT success() {
        return new RT(SUCCESS, "调用成功!");
    }

    public static RT error() {
        return new RT(INTERNAL_SERVER_ERROR, "调用失败!");
    }

    public static RT error(String message) {
        return new RT(INTERNAL_SERVER_ERROR, message);
    }

    public static RT error(Integer resultCode, String message) {
        return new RT(resultCode, message);
    }

    public Integer getResultCode() {
        return this.resultCode;
    }

    public RT setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public T getResult() {
        return this.result;
    }

    public RT setResult(T result) {
        this.result = result;
        return this;
    }
}