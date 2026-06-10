package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class DisputeCreateDTO {
    @NotBlank
    private String orderNo;
    /** 原因类型: service_quality, attitude, false_info, other */
    private String reasonType;
    @NotBlank @Size(max=500)
    private String description;
    /** 凭证图片URL列表 */
    private List<String> evidenceUrls;
}
