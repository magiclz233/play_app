package com.playapp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.AdminPlayRequestDTO;
import com.playapp.dto.PlayRequestCreateDTO;
import com.playapp.entity.PlayRequest;

public interface PlayRequestService extends IService<PlayRequest> {

    PlayRequest publish(Long userId, PlayRequestCreateDTO dto);

    Page<PlayRequest> listPublic(Integer current, Integer size);

    Page<PlayRequest> listMine(Long userId, Integer current, Integer size);

    void adminUpdate(Long adminId, Long id, AdminPlayRequestDTO dto);
}
