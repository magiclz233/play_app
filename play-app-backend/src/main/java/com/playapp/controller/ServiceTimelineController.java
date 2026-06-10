package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.entity.Order;
import com.playapp.entity.ServiceEvidence;
import com.playapp.entity.ServiceTimelineEvent;
import com.playapp.mapper.OrderMapper;
import com.playapp.service.ServiceTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ServiceTimelineController {

    private final ServiceTimelineService timelineService;
    private final OrderMapper orderMapper;

    /** 获取订单服务时间线 */
    @GetMapping("/{orderNo}/timeline")
    public Result<List<ServiceTimelineEvent>> getTimeline(@AuthenticationPrincipal Long userId,
                                                          @PathVariable String orderNo) {
        Order order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.fail("订单不存在");
        return Result.success(timelineService.getTimeline(order.getOrderId(), userId));
    }

    /** 添加时间线事件 */
    @PostMapping("/{orderNo}/timeline")
    public Result<ServiceTimelineEvent> addEvent(@AuthenticationPrincipal Long userId,
                                                  @PathVariable String orderNo,
                                                  @RequestBody Map<String, Object> body) {
        Order order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.fail("订单不存在");

        Integer eventType = (Integer) body.get("eventType");
        String eventDesc = (String) body.get("eventDesc");
        @SuppressWarnings("unchecked")
        List<String> fileUrls = (List<String>) body.get("fileUrls");
        Integer operatorRole = body.get("operatorRole") != null ? (Integer) body.get("operatorRole") : 1;

        return Result.success(timelineService.addEvent(order.getOrderId(), userId, operatorRole, eventType, eventDesc, fileUrls));
    }

    /** 获取订单凭证 */
    @GetMapping("/{orderNo}/evidence")
    public Result<List<ServiceEvidence>> getEvidence(@PathVariable String orderNo) {
        Order order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.fail("订单不存在");
        return Result.success(timelineService.getEvidence(order.getOrderId()));
    }

    /** 上传凭证 */
    @PostMapping("/{orderNo}/evidence")
    public Result<ServiceEvidence> addEvidence(@AuthenticationPrincipal Long userId,
                                                @PathVariable String orderNo,
                                                @RequestBody Map<String, Object> body) {
        Order order = orderMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo));
        if (order == null) return Result.fail("订单不存在");
        body.put("orderId", order.getOrderId());
        body.put("uploaderId", userId);
        return Result.success(timelineService.addEvidence(body));
    }
}
