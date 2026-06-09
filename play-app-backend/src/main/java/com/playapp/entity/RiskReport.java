package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "risk_reports", autoResultMap = true)
public class RiskReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reporterId;
    private Integer targetType;
    private Long targetId;
    private Long orderId;
    private String reason;
    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> evidenceUrls;

    /** 0-待处理, 1-处理中, 2-已处理, 3-驳回 */
    private Integer status;
    private Long handlerId;
    private String handleResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
