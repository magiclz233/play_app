package com.playapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlayRequestCreateDTO {

    @NotBlank(message = "需求描述不能为空")
    @Size(max = 500, message = "需求描述不能超过500字")
    private String description;

    @Size(max = 100, message = "预约时间不能超过100字")
    private String reserveTime;

    @Size(max = 200, message = "地址不能超过200字")
    private String address;

    @DecimalMin(value = "0.00", message = "预算不能为负数")
    private BigDecimal budget;
}
