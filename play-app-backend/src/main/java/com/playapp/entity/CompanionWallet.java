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
 * 陪玩钱包表
 */
@Data
@TableName(value = "companion_wallets", autoResultMap = true)
public class CompanionWallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long companionId;

    private BigDecimal balance;
    private BigDecimal frozenAmount;
    private BigDecimal totalIncome;
    private BigDecimal totalWithdrawn;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
