package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DisputeResolveDTO {
    @NotBlank
    private String orderNo;
    /** resolution: dismiss(驳回), refund_full(全额退款), refund_partial(部分退款) */
    @NotBlank
    private String resolution;
    private BigDecimal refundAmount;
    private String remark;
}
