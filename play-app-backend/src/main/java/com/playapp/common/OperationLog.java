package com.playapp.common;

import java.lang.annotation.*;

/**
 * 操作日志注解——标注在 Controller 方法上自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    /** 操作模块，如 ORDER, COMPANION, USER, WITHDRAWAL, RISK, ROLE, SYSTEM */
    String module();

    /** 操作动作，如 AUDIT, CREATE, UPDATE, DELETE, SETTLE, REFUND, STATUS_CHANGE */
    String action();

    /** 操作描述，支持 SpEL 表达式，如 "'审核通过助教 #id'" */
    String detail() default "";

    /** 是否记录请求参数，默认 true */
    boolean recordParams() default true;

    /** 是否记录响应结果，默认 false（失败时总是记录错误信息） */
    boolean recordResult() default false;
}
