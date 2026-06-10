package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.CompanionApplyDTO;
import com.playapp.entity.CompanionProfile;

public interface CompanionProfileService extends IService<CompanionProfile> {

    /**
     * 提交助教入驻申请
     * @param userId 当前用户ID
     * @param applyDTO 申请表单数据
     */
    void submitApplication(Long userId, CompanionApplyDTO applyDTO);

    /**
     * 管理员审核助教申请
     * @param companionId 助教用户ID
     * @param isPass 是否通过 (true:通过, false:驳回)
     * @param rejectReason 驳回原因 (当isPass为false时必填)
     */
    void auditApplication(Long companionId, boolean isPass, String rejectReason);

    /**
     * 分页查询公开助教列表
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.playapp.vo.CompanionVO> getCompanionPage(Integer current, Integer size, Integer categoryId, String keyword, Integer gender, String sortBy);

    /**
     * 获取助教公开详细信息
     */
    com.playapp.vo.CompanionDetailVO getCompanionDetail(Long id);
}
