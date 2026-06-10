package com.playapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.playapp.common.BusinessException;
import com.playapp.common.ErrorCode;
import com.playapp.entity.Order;
import com.playapp.entity.ServiceEvidence;
import com.playapp.entity.ServiceTimelineEvent;
import com.playapp.mapper.OrderMapper;
import com.playapp.mapper.ServiceEvidenceMapper;
import com.playapp.mapper.ServiceTimelineEventMapper;
import com.playapp.service.ServiceTimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceTimelineServiceImpl extends ServiceImpl<ServiceTimelineEventMapper, ServiceTimelineEvent>
        implements ServiceTimelineService {

    private final OrderMapper orderMapper;
    private final ServiceEvidenceMapper evidenceMapper;

    @Override
    public List<ServiceTimelineEvent> getTimeline(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || (!order.getUserId().equals(userId) && !order.getCompanionId().equals(userId))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看");
        }
        return this.list(new LambdaQueryWrapper<ServiceTimelineEvent>()
                .eq(ServiceTimelineEvent::getOrderId, orderId)
                .orderByAsc(ServiceTimelineEvent::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceTimelineEvent addEvent(Long orderId, Long operatorId, Integer operatorRole,
                                          Integer eventType, String eventDesc, List<String> fileUrls) {
        ServiceTimelineEvent event = new ServiceTimelineEvent();
        event.setOrderId(orderId);
        event.setEventType(eventType);
        event.setEventDesc(eventDesc);
        event.setOperatorId(operatorId);
        event.setOperatorRole(operatorRole);
        this.save(event);

        // 如果有图片，保存为凭证
        if (fileUrls != null && !fileUrls.isEmpty()) {
            for (String url : fileUrls) {
                ServiceEvidence evidence = new ServiceEvidence();
                evidence.setOrderId(orderId);
                evidence.setTimelineEventId(event.getId());
                evidence.setFileType(1);
                evidence.setFileUrl(url);
                evidence.setUploaderId(operatorId);
                evidence.setUploaderRole(operatorRole);
                evidence.setDescription(eventDesc);
                evidenceMapper.insert(evidence);
            }
        }
        log.info("时间线事件: orderId={}, eventType={}, operator={}", orderId, eventType, operatorId);
        return event;
    }

    @Override
    public List<ServiceEvidence> getEvidence(Long orderId) {
        return evidenceMapper.selectList(new LambdaQueryWrapper<ServiceEvidence>()
                .eq(ServiceEvidence::getOrderId, orderId)
                .orderByDesc(ServiceEvidence::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceEvidence addEvidence(Map<String, Object> params) {
        ServiceEvidence evidence = new ServiceEvidence();
        evidence.setOrderId(Long.valueOf(params.get("orderId").toString()));
        evidence.setTimelineEventId(params.get("timelineEventId") != null
                ? Long.valueOf(params.get("timelineEventId").toString()) : null);
        evidence.setFileType(params.get("fileType") != null ? (Integer) params.get("fileType") : 1);
        evidence.setFileUrl(params.get("fileUrl").toString());
        evidence.setThumbnailUrl(params.get("thumbnailUrl") != null ? params.get("thumbnailUrl").toString() : null);
        evidence.setUploaderId(Long.valueOf(params.get("uploaderId").toString()));
        evidence.setUploaderRole((Integer) params.get("uploaderRole"));
        evidence.setDescription(params.get("description") != null ? params.get("description").toString() : null);
        evidenceMapper.insert(evidence);
        return evidence;
    }
}
