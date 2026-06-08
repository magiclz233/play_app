package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefundApplyDTO {

    @NotBlank(message = "退款原因不能为空")
    @Size(max = 200, message = "退款原因不能超过200字")
    private String reason;
}
