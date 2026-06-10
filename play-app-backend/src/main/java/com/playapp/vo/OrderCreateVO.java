package com.playapp.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单创建结果视图对象
 */
@Data
@AllArgsConstructor
public class OrderCreateVO {
    private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal companionAmount;
}
