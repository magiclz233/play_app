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
 * 提现申请记录
 */
@Data
@TableName(value = "withdrawal_records", autoResultMap = true)
public class WithdrawalRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companionId;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal actualAmount;

    /** 状态: 0-待审核, 1-审核通过/打款中, 2-打款成功, 3-打款失败, 4-审核驳回 */
    private Integer status;
    private String wxPaymentNo;
    private String rejectReason;
    private Long auditorId;
    private LocalDateTime auditTime;
    private LocalDateTime paidTime;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
