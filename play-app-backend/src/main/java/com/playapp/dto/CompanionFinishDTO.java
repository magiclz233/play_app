package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 陪玩发起完工请求体
 */
@Data
public class CompanionFinishDTO {
    @NotBlank(message = "完工备注不能为空")
    @Size(max = 500, message = "完工备注长度不能超过500")
    private String finishRemark;

    /** 完工类型: 1-正常完工, 2-提前结束, 3-超时结束 */
    private Integer finishType;

    /** 实际服务时长(分钟) */
    private Integer actualDuration;
}
