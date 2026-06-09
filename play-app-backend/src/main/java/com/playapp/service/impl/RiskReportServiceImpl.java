package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.AdminRiskHandleDTO;
import com.playapp.dto.RiskReportDTO;
import com.playapp.entity.RiskReport;
import com.playapp.mapper.RiskReportMapper;
import com.playapp.service.AdminAuditLogService;
import com.playapp.service.RiskReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskReportServiceImpl extends ServiceImpl<RiskReportMapper, RiskReport> implements RiskReportService {

    private final AdminAuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RiskReport submit(Long reporterId, RiskReportDTO dto) {
        RiskReport report = new RiskReport();
        report.setReporterId(reporterId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setOrderId(dto.getOrderId());
        report.setReason(dto.getReason());
        report.setDescription(dto.getDescription());
        report.setEvidenceUrls(dto.getEvidenceUrls() == null ? List.of() : dto.getEvidenceUrls());
        report.setStatus(0);
        this.save(report);
        return report;
    }

    @Override
    public Page<RiskReport> listMine(Long reporterId, Integer current, Integer size) {
        LambdaQueryWrapper<RiskReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskReport::getReporterId, reporterId)
                .orderByDesc(RiskReport::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    public Page<RiskReport> listAdmin(Integer status, Integer current, Integer size) {
        LambdaQueryWrapper<RiskReport> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(RiskReport::getStatus, status);
        }
        wrapper.orderByDesc(RiskReport::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminHandle(Long adminId, Long id, AdminRiskHandleDTO dto) {
        RiskReport report = this.getById(id);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "举报记录不存在");
        }
        report.setStatus(dto.getStatus());
        report.setHandlerId(adminId);
        report.setHandleResult(dto.getHandleResult());
        this.updateById(report);
        auditLogService.record(adminId, "RISK_REPORT_HANDLE", "risk_report", String.valueOf(id), dto.getHandleResult());
    }
}
