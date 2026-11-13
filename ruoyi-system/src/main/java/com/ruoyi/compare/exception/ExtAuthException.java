package com.ruoyi.compare.exception;

public class ExtAuthException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final int code;

    public ExtAuthException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
