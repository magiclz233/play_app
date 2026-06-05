package com.playapp.common;

import lombok.Getter;

/**
 * 业务异常（由 Service 层抛出，由全局异常处理器捕获）
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.SERVER_ERROR;
    }

    /** 快捷工厂方法 */
    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.NOT_FOUND, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ErrorCode.FORBIDDEN, message);
    }

    public static BusinessException orderStatusIllegal() {
        return new BusinessException(ErrorCode.ORDER_STATUS_ILLEGAL, "当前订单状态不允许此操作");
    }
}
