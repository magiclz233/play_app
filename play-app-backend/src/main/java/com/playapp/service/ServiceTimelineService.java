package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.entity.ServiceEvidence;
import com.playapp.entity.ServiceTimelineEvent;

import java.util.List;
import java.util.Map;

public interface ServiceTimelineService extends IService<ServiceTimelineEvent> {

    /** 获取订单服务时间线 */
    List<ServiceTimelineEvent> getTimeline(Long orderId, Long userId);

    /** 添加时间线事件 */
    ServiceTimelineEvent addEvent(Long orderId, Long operatorId, Integer operatorRole,
                                   Integer eventType, String eventDesc, List<String> fileUrls);

    /** 获取订单凭证 */
    List<ServiceEvidence> getEvidence(Long orderId);

    /** 上传凭证 */
    ServiceEvidence addEvidence(Map<String, Object> params);
}
