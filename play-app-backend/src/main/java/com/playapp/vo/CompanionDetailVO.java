package com.playapp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompanionDetailVO {
    private Long userId;
    private String nickname;
    private String realName; // usually hidden from public, but keeping for reference, maybe hide later
    private Integer gender;
    private Integer age;
    private Integer height;
    private String summary;
    private String voiceUrl;
    private Integer voiceDuration;
    
    private BigDecimal pricePerHour;
    private BigDecimal rating;
    private Integer totalRatingCount;
    private Integer orderCount;
    private Integer workStatus;
    
    private List<String> photoUrls;
    private List<String> tags;
}
