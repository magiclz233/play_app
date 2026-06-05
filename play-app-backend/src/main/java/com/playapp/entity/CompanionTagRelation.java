package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 陪玩-标签关联表
 */
@Data
@TableName("companion_tag_relations")
public class CompanionTagRelation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long companionId;
    private Integer tagId;
}
