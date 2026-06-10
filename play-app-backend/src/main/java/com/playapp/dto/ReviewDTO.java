package com.playapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewDTO {

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "最低1分")
    @Max(value = 5, message = "最高5分")
    private Integer rating;

    private String content;

    /** 是否匿名评价 */
    private Boolean isAnonymous;

    // Optional images, separated by comma or array
    private String images;
}
