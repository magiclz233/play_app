package com.playapp.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

/**
 * 交易订单（核心主表）
 *
 * 状态机:
 *   10-待付款 → 20-已付款/待拉群 → 30-已拉群/服务中 → 40-双方确认/待服务
 *   → 50-服务进行中 → 60-陪玩申请完工 → 70-用户已确认完工 → 80-平台已放款/完成
 *
 *   异常链路:
 *   任意状态 → 100-用户申请取消 → 110-退款处理中 → 120-全额退款/130-部分退款
 *   任意状态 → 200-纠纷申诉中 → 210-纠纷处理完成
 *   10(超时未付) → 250-自动关闭
 */
@Data
@TableName(value = "orders", autoResultMap = true)
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;
    private Long userId;
    private Long companionId;
    private Integer categoryId;

    // ----- 服务与定价 -----
    private BigDecimal hours;
    private BigDecimal pricePerHour;
    private BigDecimal totalAmount;
    private BigDecimal companionAmount;
    private BigDecimal platformFee;

    // ----- 预约信息 -----
    private LocalDate reserveDate;
    private LocalTime reserveTimeStart;
    private LocalTime reserveTimeEnd;
    private String address;
    private String addressDetail;

    // ----- 客户沟通 -----
    private String customerWechat;
    private String customerRemark;

    // ----- 订单状态 -----
    private Integer status;

    /** 三方群状态: 0-未拉群, 1-已拉群, 2-已解散 */
    private Integer wechatGroupStatus;

    // ----- 支付信息 -----
    private String transactionId;
    private LocalDateTime payTime;

    // ----- 取消与退款 -----
    private String cancelReason;
    private Integer cancelType;
    private BigDecimal refundAmount;

    // ----- 完成信息 -----
    private LocalDateTime finishTime;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extra;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 订单状态常量 =====
    public static final int STATUS_PENDING_PAY        = 10;
    public static final int STATUS_PAID               = 20;
    public static final int STATUS_GROUP_CREATED      = 30;
    public static final int STATUS_CONFIRMED          = 40;
    public static final int STATUS_IN_SERVICE         = 50;
    public static final int STATUS_FINISH_REQUESTED   = 60;
    public static final int STATUS_USER_CONFIRMED     = 70;
    public static final int STATUS_SETTLED            = 80;
    public static final int STATUS_CANCEL_REQUESTED   = 100;
    public static final int STATUS_REFUNDING          = 110;
    public static final int STATUS_FULL_REFUNDED      = 120;
    public static final int STATUS_PARTIAL_REFUNDED   = 130;
    public static final int STATUS_DISPUTE            = 200;
    public static final int STATUS_DISPUTE_RESOLVED   = 210;
    public static final int STATUS_CLOSED             = 250;
}
