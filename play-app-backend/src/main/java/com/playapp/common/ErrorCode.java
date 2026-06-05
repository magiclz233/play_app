package com.playapp.common;

/**
 * 业务错误码常量
 */
public interface ErrorCode {

    // ===== 通用 =====
    int SUCCESS = 200;
    int PARAM_ERROR = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int SERVER_ERROR = 500;

    // ===== 用户模块 10xx =====
    int USER_NOT_FOUND = 1001;
    int USER_DISABLED = 1002;
    int PHONE_ALREADY_BOUND = 1003;

    // ===== 认证模块 11xx =====
    int TOKEN_EXPIRED = 1101;
    int TOKEN_INVALID = 1102;
    int WX_LOGIN_FAILED = 1103;

    // ===== 陪玩模块 20xx =====
    int COMPANION_NOT_FOUND = 2001;
    int COMPANION_ALREADY_APPLIED = 2002;
    int COMPANION_NOT_APPROVED = 2003;
    int COMPANION_BUSY = 2004;

    // ===== 订单模块 30xx =====
    int ORDER_NOT_FOUND = 3001;
    int ORDER_STATUS_ILLEGAL = 3002;
    int ORDER_CANNOT_CANCEL = 3003;
    int ORDER_ALREADY_PAID = 3004;

    // ===== 支付模块 31xx =====
    int PAY_NOTIFY_INVALID = 3101;
    int PAY_FAILED = 3102;

    // ===== 钱包模块 40xx =====
    int WALLET_INSUFFICIENT = 4001;
    int WITHDRAW_PENDING_EXISTS = 4002;

    // ===== 评价模块 50xx =====
    int REVIEW_ALREADY_EXISTS = 5001;
    int REVIEW_NOT_ALLOWED = 5002;
}
