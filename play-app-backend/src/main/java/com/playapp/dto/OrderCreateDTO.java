package com.playapp.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "请填写服务地址")
    @Size(max = 255, message = "服务地址不能超过255字")
    private String address;

    @Size(max = 255, message = "详细地址不能超过255字")
    private String addressDetail;

    @NotBlank(message = "请填写微信号或手机号")
    @Size(max = 50, message = "联系方式不能超过50字")
    private String customerWechat;

    private String remark;
}
