package com.playapp.dto;

import lombok.Data;

@Data
public class AdminPlayRequestDTO {
    private Integer status;
    private Integer contactStatus;
    private String adminRemark;
}
