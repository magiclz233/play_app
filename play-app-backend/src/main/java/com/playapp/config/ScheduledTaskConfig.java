package com.playapp.config;

import com.playapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 系统定时任务配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledTaskConfig {

    private final OrderService orderService;

    /**
     * 自动关闭超时未支付订单
     * 每 60 秒执行一次
     */
    @Scheduled(fixedDelay = 60000)
    public void autoCloseExpiredOrders() {
        try {
            orderService.autoCloseExpiredOrders();
        } catch (Exception e) {
            log.error("定时任务 autoCloseExpiredOrders 执行异常", e);
        }
    }
}
