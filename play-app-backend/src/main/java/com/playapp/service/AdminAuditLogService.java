package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.entity.AdminAuditLog;

public interface AdminAuditLogService extends IService<AdminAuditLog> {

    void record(Long adminId, String action, String targetType, String targetId, String remark);
}
