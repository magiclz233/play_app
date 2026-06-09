package com.playapp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompanionVO {
    private Long userId;
    private String nickname;
    private String coverUrl; // First album image
    private BigDecimal rating;
    private Integer totalRatingCount;
    private BigDecimal pricePerHour;
    private Integer workStatus; // 1-接单中, 2-忙碌中
    private String voiceUrl;
    private Integer voiceDuration;
    private List<String> tags;
    private String distance; // Mocked for MVP
}
