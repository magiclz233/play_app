package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 陪玩详细资料
 */
@Data
@TableName(value = "companion_profiles", autoResultMap = true)
public class CompanionProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    private Long userId;

    private String realName;

    /** 对外展示昵称 */
    private String nickname;

    /** 助教号码（客户可通过此号码搜索） */
    private String companionCode;

    /** 身份证号（加密存储） */
    private String idCardNo;

    private String wechatCode;
    private String voiceUrl;
    private Integer voiceDuration;

    /** 1-男, 2-女 */
    private Integer gender;
    private Integer age;
    private Integer height;
    private String summary;
    private String serviceArea;

    /** 接单状态: 1-接单中, 2-忙碌中, 3-休息中 */
    private Integer workStatus;

    private BigDecimal rating;
    private Integer totalRatingCount;
    private Integer totalRatingScore;
    private Integer orderCount;

    /** 审核状态: 0-待审核, 1-审核通过, 2-已驳回, 3-已冻结 */
    private Integer auditStatus;
    private String rejectReason;
    private Boolean isRecommend;
    private Integer sortWeight;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
