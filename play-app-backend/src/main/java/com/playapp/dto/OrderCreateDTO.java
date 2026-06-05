package com.playapp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateDTO {

    @NotNull(message = "请选择要预约的助教")
    private Long companionId;

    @NotNull(message = "请选择服务类别")
    private Integer serviceId; // Mock service category

    @NotNull(message = "请选择服务时长")
    @Min(value = 1, message = "最少预约1小时")
    private Integer hours;

    @NotNull(message = "请选择预约开始时间")
    @Future(message = "预约时间必须在当前时间之后")
    private LocalDateTime appointmentTime;

    private String remark;
}
