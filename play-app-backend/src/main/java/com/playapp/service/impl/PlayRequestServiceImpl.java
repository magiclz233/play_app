package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.AdminPlayRequestDTO;
import com.playapp.dto.PlayRequestCreateDTO;
import com.playapp.entity.PlayRequest;
import com.playapp.entity.User;
import com.playapp.mapper.PlayRequestMapper;
import com.playapp.service.AdminAuditLogService;
import com.playapp.service.PlayRequestService;
import com.playapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PlayRequestServiceImpl extends ServiceImpl<PlayRequestMapper, PlayRequest> implements PlayRequestService {

    private final UserService userService;
    private final AdminAuditLogService auditLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PlayRequest publish(Long userId, PlayRequestCreateDTO dto) {
        User user = userService.getById(userId);
        PlayRequest request = new PlayRequest();
        request.setUserId(userId);
        request.setNickname(user == null ? "用户" + userId : user.getNickname());
        request.setAvatarUrl(user == null ? null : user.getAvatarUrl());
        request.setDescription(dto.getDescription());
        request.setReserveTime(dto.getReserveTime());
        request.setAddress(dto.getAddress());
        request.setBudget(dto.getBudget() == null ? BigDecimal.ZERO : dto.getBudget());
        request.setStatus(0);
        request.setContactStatus(0);
        this.save(request);
        return request;
    }

    @Override
    public Page<PlayRequest> listPublic(Integer current, Integer size) {
        LambdaQueryWrapper<PlayRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(PlayRequest::getStatus, 0, 1)
                .orderByDesc(PlayRequest::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    public Page<PlayRequest> listMine(Long userId, Integer current, Integer size) {
        LambdaQueryWrapper<PlayRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlayRequest::getUserId, userId)
                .orderByDesc(PlayRequest::getCreateTime);
        return this.page(new Page<>(current, size), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminUpdate(Long adminId, Long id, AdminPlayRequestDTO dto) {
        PlayRequest request = this.getById(id);
        if (request == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "需求不存在");
        }
        if (dto.getStatus() != null) {
            request.setStatus(dto.getStatus());
        }
        if (dto.getContactStatus() != null) {
            request.setContactStatus(dto.getContactStatus());
        }
        if (dto.getAdminRemark() != null) {
            request.setAdminRemark(dto.getAdminRemark());
        }
        request.setAssignedAdminId(adminId);
        this.updateById(request);
        auditLogService.record(adminId, "PLAY_REQUEST_UPDATE", "play_request", String.valueOf(id), dto.getAdminRemark());
    }
}
