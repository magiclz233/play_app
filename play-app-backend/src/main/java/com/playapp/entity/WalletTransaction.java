package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 钱包变动流水明细
 */
@Data
@TableName(value = "wallet_transactions", autoResultMap = true)
public class WalletTransaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companionId;
    private Long orderId;

    /** 1-订单收入, 2-提现扣款, 3-平台扣罚, 4-平台奖励, 5-退款回滚 */
    private Integer transactionType;

    /** 关联来源类型: 1-订单, 2-提现, 3-平台操作 */
    private Integer sourceType;
    /** 关联来源ID */
    private Long sourceId;

    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
