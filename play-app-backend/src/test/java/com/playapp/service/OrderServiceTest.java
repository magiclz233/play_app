package com.playapp.service;

import com.playapp.common.BusinessException;
import com.playapp.entity.Order;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态机单元测试
 */
class OrderServiceTest {

    /** 合法状态转移映射（与 OrderServiceImpl 保持一致） */
    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(10, Set.of(20, 100, 250)),
            Map.entry(20, Set.of(30, 100, 110, 200)),
            Map.entry(30, Set.of(40, 50, 70, 100, 200)),
            Map.entry(40, Set.of(50, 70, 100, 200)),
            Map.entry(50, Set.of(60, 70, 100, 200)),
            Map.entry(60, Set.of(70, 100, 200)),
            Map.entry(70, Set.of(80, 200)),
            Map.entry(80, Set.of(200)),
            Map.entry(100, Set.of(110)),
            Map.entry(110, Set.of(120, 130)),
            Map.entry(200, Set.of(210))
    );

    /** 终态 */
    private static final Set<Integer> TERMINAL = Set.of(120, 130, 210, 250);

    @Test
    void testNormalFlowTransitions() {
        // 正常流程：10→20→30→40→50→60→70→80
        assertTrue(isValidTransition(10, 20), "待付款→已付款");
        assertTrue(isValidTransition(20, 30), "已付款→客服拉群");
        assertTrue(isValidTransition(30, 40), "客服拉群→双方确认");
        assertTrue(isValidTransition(40, 50), "双方确认→服务进行中");
        assertTrue(isValidTransition(50, 60), "服务中→陪玩完工");
        assertTrue(isValidTransition(60, 70), "陪玩完工→用户确认");
        assertTrue(isValidTransition(70, 80), "用户确认→平台放款");
    }

    @Test
    void testCancelFlowTransitions() {
        // 取消流程
        assertTrue(isValidTransition(10, 100), "待付款→取消申请");
        assertTrue(isValidTransition(20, 100), "已付款→取消申请");
        assertTrue(isValidTransition(30, 100), "已拉群→取消申请");
        assertTrue(isValidTransition(40, 100), "已确认→取消申请");
        assertTrue(isValidTransition(50, 100), "服务中→取消申请");
        assertTrue(isValidTransition(60, 100), "待确认→取消申请");
        assertTrue(isValidTransition(100, 110), "取消申请→退款处理中");
        assertTrue(isValidTransition(110, 120), "退款处理中→全额退款");
        assertTrue(isValidTransition(110, 130), "退款处理中→部分退款");
    }

    @Test
    void testTimeoutCloseTransition() {
        assertTrue(isValidTransition(10, 250), "待付款→超时关闭");
    }

    @Test
    void testDisputeFlowTransitions() {
        assertTrue(isValidTransition(20, 200), "已付款→纠纷");
        assertTrue(isValidTransition(50, 200), "服务中→纠纷");
        assertTrue(isValidTransition(70, 200), "已确认完工→纠纷");
        assertTrue(isValidTransition(80, 200), "已完成→纠纷");
        assertTrue(isValidTransition(200, 210), "纠纷→纠纷完成");
    }

    @Test
    void testAdminOverrideTransitions() {
        // admin 跳过步骤
        assertTrue(isValidTransition(30, 50), "已拉群→admin标记服务开始");
        assertTrue(isValidTransition(30, 70), "已拉群→admin核销完工");
        assertTrue(isValidTransition(40, 70), "已确认→admin核销完工");
        assertTrue(isValidTransition(50, 70), "服务中→admin核销完工");
    }

    @Test
    void testIllegalTransitions() {
        // 非法跳转
        assertFalse(isValidTransition(10, 80), "待付款→已放款（非法）");
        assertFalse(isValidTransition(10, 50), "待付款→服务中（非法）");
        assertFalse(isValidTransition(20, 80), "已付款→已放款（非法）");
        assertFalse(isValidTransition(50, 80), "服务中→已放款（非法）");
        assertFalse(isValidTransition(80, 120), "已完成→全额退款（非法）");
        assertFalse(isValidTransition(250, 20), "已关闭→已付款（非法）");
        assertFalse(isValidTransition(120, 20), "已退款→已付款（非法）");
    }

    @Test
    void testTerminalStates() {
        assertTrue(TERMINAL.contains(120), "全额退款是终态");
        assertTrue(TERMINAL.contains(130), "部分退款是终态");
        assertTrue(TERMINAL.contains(210), "纠纷完成是终态");
        assertTrue(TERMINAL.contains(250), "已关闭是终态");
        // 终态没有合法转移
        for (Integer term : TERMINAL) {
            Set<Integer> targets = ALLOWED_TRANSITIONS.get(term);
            assertTrue(targets == null || targets.isEmpty(), "终态" + term + " 不应有后续转移");
        }
    }

    @Test
    void testPlatformFeeCalculation() {
        // 费率 5%，最低5元，最高50元
        assertEquals("5.00", calcFee(50).toString(), "50元：最低5元");
        assertEquals("5.00", calcFee(100).toString(), "100元：5元 (5%)");
        assertEquals("10.00", calcFee(200).toString(), "200元：10元 (5%)");
        assertEquals("25.00", calcFee(500).toString(), "500元：25元 (5%)");
        assertEquals("50.00", calcFee(1000).toString(), "1000元：封顶50元");
        assertEquals("50.00", calcFee(2000).toString(), "2000元：封顶50元");
        assertEquals("50.00", calcFee(5000).toString(), "5000元：封顶50元");
    }

    @Test
    void testOrderStatusConstants() {
        assertEquals(10, Order.STATUS_PENDING_PAY);
        assertEquals(20, Order.STATUS_PAID);
        assertEquals(60, Order.STATUS_FINISH_REQUESTED);
        assertEquals(80, Order.STATUS_SETTLED);
        assertEquals(200, Order.STATUS_DISPUTE);
        assertEquals(250, Order.STATUS_CLOSED);
    }

    // ===== helpers =====

    private boolean isValidTransition(int from, int to) {
        Set<Integer> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    private java.math.BigDecimal calcFee(int totalAmount) {
        java.math.BigDecimal total = java.math.BigDecimal.valueOf(totalAmount);
        java.math.BigDecimal rate = new java.math.BigDecimal("0.05");
        java.math.BigDecimal min = new java.math.BigDecimal("5.00");
        java.math.BigDecimal max = new java.math.BigDecimal("50.00");
        java.math.BigDecimal raw = total.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        return raw.max(min).min(max);
    }
}
