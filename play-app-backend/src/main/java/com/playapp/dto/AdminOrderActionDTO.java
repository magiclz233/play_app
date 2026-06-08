package com.playapp.dto;

import lombok.Data;

/**
 * 管理员推进订单状态时的可选备注信息。
 */
@Data
public class AdminOrderActionDTO {

    private String remark;

    private String actualAddress;

    private String finishRemark;

    private Integer finishType;
}
