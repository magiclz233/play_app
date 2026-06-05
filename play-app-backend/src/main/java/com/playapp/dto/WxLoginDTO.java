package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginDTO {
    
    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
