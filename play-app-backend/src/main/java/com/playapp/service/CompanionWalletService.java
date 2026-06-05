package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.dto.WithdrawDTO;
import com.playapp.entity.CompanionWallet;
import com.playapp.entity.WalletTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CompanionWalletService extends IService<CompanionWallet> {

    /**
     * 获取助教钱包信息（包含余额和总收入）
     */
    CompanionWallet getWalletByCompanionId(Long companionId);

    /**
     * 增加账户余额（订单完工后调用）
     */
    void addBalance(Long companionId, BigDecimal amount, Long orderId, String orderNo);

    /**
     * 发起提现申请
     */
    void applyWithdraw(Long companionId, WithdrawDTO dto);

    /**
     * 获取资金流水
     */
    List<WalletTransaction> getTransactions(Long companionId);
    
    /**
     * 获取工作台统计数据
     */
    Map<String, Object> getDashboardStats(Long companionId);

    /**
     * 审核提现申请 (Admin)
     */
    void auditWithdrawal(Long recordId, Integer status, String remark);
}
