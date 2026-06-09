package com.playapp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.entity.AdminAuditLog;
import com.playapp.mapper.AdminAuditLogMapper;
import com.playapp.service.AdminAuditLogService;
import org.springframework.stereotype.Service;

@Service
public class AdminAuditLogServiceImpl extends ServiceImpl<AdminAuditLogMapper, AdminAuditLog> implements AdminAuditLogService {

    @Override
    public void record(Long adminId, String action, String targetType, String targetId, String remark) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminId(adminId == null ? 0L : adminId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setRemark(remark);
        this.save(log);
    }
}
