package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.dto.WithdrawDTO;
import com.playapp.entity.CompanionWallet;
import com.playapp.entity.Order;
import com.playapp.entity.WalletTransaction;
import com.playapp.entity.WithdrawalRecord;
import com.playapp.mapper.CompanionWalletMapper;
import com.playapp.mapper.OrderMapper;
import com.playapp.mapper.WalletTransactionMapper;
import com.playapp.mapper.WithdrawalRecordMapper;
import com.playapp.service.CompanionWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanionWalletServiceImpl extends ServiceImpl<CompanionWalletMapper, CompanionWallet> implements CompanionWalletService {

    private final WalletTransactionMapper walletTransactionMapper;
    private final WithdrawalRecordMapper withdrawalRecordMapper;
    private final OrderMapper orderMapper;

    @Override
    public CompanionWallet getWalletByCompanionId(Long companionId) {
        CompanionWallet wallet = this.getOne(new LambdaQueryWrapper<CompanionWallet>().eq(CompanionWallet::getCompanionId, companionId));
        if (wallet == null) {
            wallet = new CompanionWallet();
            wallet.setCompanionId(companionId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setFrozenAmount(BigDecimal.ZERO);
            wallet.setTotalIncome(BigDecimal.ZERO);
            this.save(wallet);
        }
        return wallet;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBalance(Long companionId, BigDecimal amount, Long orderId, String orderNo) {
        CompanionWallet wallet = getWalletByCompanionId(companionId);

        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setTotalIncome(wallet.getTotalIncome().add(amount));
        this.updateById(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setCompanionId(companionId);
        tx.setAmount(amount);
        tx.setTransactionType(1); // 1-收入
        tx.setSourceType(1); // 1-订单收益
        tx.setSourceId(orderId);
        tx.setDescription("订单收益: " + orderNo);
        walletTransactionMapper.insert(tx);

        log.info("助教 {} 增加订单收益 {}, 订单号: {}", companionId, amount, orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyWithdraw(Long companionId, WithdrawDTO dto) {
        CompanionWallet wallet = getWalletByCompanionId(companionId);

        if (wallet.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "余额不足");
        }

        wallet.setBalance(wallet.getBalance().subtract(dto.getAmount()));
        wallet.setFrozenAmount(wallet.getFrozenAmount().add(dto.getAmount()));
        this.updateById(wallet);

        WithdrawalRecord record = new WithdrawalRecord();
        record.setCompanionId(companionId);
        record.setAmount(dto.getAmount());
        record.setActualAmount(dto.getAmount());
        record.setStatus(0); // 待审核
        // 提现方式和账号存入extra
        Map<String, Object> recordExtra = new HashMap<>();
        recordExtra.put("withdrawMethod", 1); // 1-微信零钱
        recordExtra.put("withdrawAccount", dto.getRealName());
        record.setExtra(recordExtra);
        withdrawalRecordMapper.insert(record);

        WalletTransaction tx = new WalletTransaction();
        tx.setCompanionId(companionId);
        tx.setAmount(dto.getAmount().negate());
        tx.setTransactionType(2); // 2-支出
        tx.setSourceType(2); // 2-提现
        tx.setSourceId(record.getId());
        tx.setDescription("提现申请冻结");
        walletTransactionMapper.insert(tx);

        log.info("助教 {} 发起提现申请，金额: {}", companionId, dto.getAmount());
    }

    @Override
    public List<WalletTransaction> getTransactions(Long companionId) {
        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WalletTransaction::getCompanionId, companionId)
               .orderByDesc(WalletTransaction::getCreateTime);
        return walletTransactionMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getDashboardStats(Long companionId) {
        Map<String, Object> stats = new HashMap<>();

        CompanionWallet wallet = getWalletByCompanionId(companionId);
        stats.put("balance", wallet.getBalance());
        stats.put("totalIncome", wallet.getTotalIncome());

        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getCompanionId, companionId)
                    .ge(Order::getStatus, 20);
        long orderCount = orderMapper.selectCount(orderWrapper);
        stats.put("totalOrderCount", orderCount);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditWithdrawal(Long recordId, Integer status, String remark) {
        WithdrawalRecord record = withdrawalRecordMapper.selectById(recordId);
        if (record == null || record.getStatus() != 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "提现记录不存在或已处理");
        }

        CompanionWallet wallet = getWalletByCompanionId(record.getCompanionId());

        if (status == 1) {
            // 通过
            record.setStatus(1);
            withdrawalRecordMapper.updateById(record);

            wallet.setFrozenAmount(wallet.getFrozenAmount().subtract(record.getAmount()));
            this.updateById(wallet);

        } else if (status == 2) {
            // 拒绝
            record.setStatus(4); // 4-审核驳回
            record.setRejectReason(remark);
            withdrawalRecordMapper.updateById(record);

            wallet.setFrozenAmount(wallet.getFrozenAmount().subtract(record.getAmount()));
            wallet.setBalance(wallet.getBalance().add(record.getAmount()));
            this.updateById(wallet);

            WalletTransaction tx = new WalletTransaction();
            tx.setCompanionId(record.getCompanionId());
            tx.setAmount(record.getAmount());
            tx.setTransactionType(1); // 1-收入(退回)
            tx.setSourceType(3); // 3-提现退回
            tx.setSourceId(record.getId());
            tx.setDescription("提现被拒退回");
            walletTransactionMapper.insert(tx);
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的审核状态");
        }
    }
}
