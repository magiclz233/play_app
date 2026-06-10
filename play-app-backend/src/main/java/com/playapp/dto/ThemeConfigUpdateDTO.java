package com.playapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 品牌主题配置更新 DTO
 */
@Data
public class ThemeConfigUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 品牌主色 HEX，如 "#FF3B5C" */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "品牌主色必须为有效的 HEX 颜色值")
    private String primaryColor;

    /** 强调色 HEX，如 "#FF9F1C" */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "强调色必须为有效的 HEX 颜色值")
    private String accentColor;

    /** 渐变终点色 HEX，如 "#FF7B54" */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "渐变终点色必须为有效的 HEX 颜色值")
    private String gradientEndColor;

    /** 圆角策略: "rounded" | "smooth" | "pill" */
    @Pattern(regexp = "^(rounded|smooth|pill)$", message = "圆角策略必须是 rounded/smooth/pill 之一")
    private String radiusScale;

    /** 间距策略: "compact" | "comfortable" | "spacious" */
    @Pattern(regexp = "^(compact|comfortable|spacious)$", message = "间距策略必须是 compact/comfortable/spacious 之一")
    private String spacingScale;

    /** 字体缩放: 1 | 1.15 | 1.3 */
    private Double fontScale;
}
