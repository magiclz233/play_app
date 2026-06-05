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
 * 支付流水表
 */
@Data
@TableName(value = "payment_records", autoResultMap = true)
public class PaymentRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String paymentNo;
    private String wxTransactionId;

    /** 支付类型: 1-用户付款, 2-平台退款(全额), 3-平台退款(部分), 4-平台放款给陪玩 */
    private Integer paymentType;
    private BigDecimal amount;

    /** 状态: 0-待处理, 1-成功, 2-失败, 3-已关闭 */
    private Integer status;
    private LocalDateTime paidTime;
    private String notifyData;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
