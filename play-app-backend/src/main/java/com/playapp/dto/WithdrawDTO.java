package com.playapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawDTO {

    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "10.00", message = "单次提现金额不能低于10元")
    private BigDecimal amount;

    // 真实场景通常需要选择提现渠道（微信零钱、银行卡等），MVP 默认提现到绑定的微信号
    @NotBlank(message = "真实姓名不能为空（用于微信实名校验）")
    private String realName;
}
