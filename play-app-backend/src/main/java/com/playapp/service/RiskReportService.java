package com.playapp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.AdminRiskHandleDTO;
import com.playapp.dto.RiskReportDTO;
import com.playapp.entity.RiskReport;

public interface RiskReportService extends IService<RiskReport> {

    RiskReport submit(Long reporterId, RiskReportDTO dto);

    Page<RiskReport> listMine(Long reporterId, Integer current, Integer size);

    Page<RiskReport> listAdmin(Integer status, Integer current, Integer size);

    void adminHandle(Long adminId, Long id, AdminRiskHandleDTO dto);
}
