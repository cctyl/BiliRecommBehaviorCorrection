package io.github.cctyl.exception;


/**
 * 未登陆异常
 */
public class LogOutException extends RuntimeException {

    public LogOutException() {
        super("未登录/登录失效");
    }
}
