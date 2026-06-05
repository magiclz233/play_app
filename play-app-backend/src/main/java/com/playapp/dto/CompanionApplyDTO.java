package com.playapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompanionApplyDTO {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "微信号不能为空")
    private String wechatCode;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    private Integer age;
    private Integer height;

    @NotBlank(message = "个人简介不能为空")
    private String summary;

    @NotEmpty(message = "请至少选择一个特长标签")
    private List<Integer> tagIds;

    @NotNull(message = "请设置每小时价格")
    @DecimalMin(value = "0.01", message = "价格不能小于0.01")
    private BigDecimal pricePerHour;

    // TODO: In a real scenario, we would also expect lists of photo URLs and a voice URL.
    // For MVP, we receive them directly in this DTO after they are uploaded via another interface.
    @NotEmpty(message = "请至少上传一张展示图片")
    private List<String> photoUrls;

    private String voiceUrl; // 语音介绍（H5不可用）
    
    private Integer voiceDuration;
}
