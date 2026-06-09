package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RiskReportDTO {

    @NotNull(message = "举报目标类型不能为空")
    private Integer targetType;

    private Long targetId;
    private Long orderId;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 200, message = "举报原因不能超过200字")
    private String reason;

    @Size(max = 1000, message = "详细说明不能超过1000字")
    private String description;

    private List<String> evidenceUrls;
}
