package com.playapp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRiskHandleDTO {

    @NotNull(message = "处理状态不能为空")
    private Integer status;

    private String handleResult;
}
