package io.github.cctyl.constants;

import lombok.Data;


public enum ErrorCode {

    ALREAD_THUMBUP(65007,"已踩过"),

    ;

    private int code;
    private String message;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
