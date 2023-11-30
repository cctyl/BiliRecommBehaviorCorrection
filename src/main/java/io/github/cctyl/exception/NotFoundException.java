package io.github.cctyl.exception;


/**
 * 未登陆异常
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("404 啥都木有");
    }
}
