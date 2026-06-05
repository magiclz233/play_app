package com.playapp.controller;

import com.playapp.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/wx/pay")
@RequiredArgsConstructor
public class WxPayController {

    private final OrderService orderService;

    /**
     * 微信支付异步通知回调
     */
    @PostMapping("/notify")
    public String parseOrderNotifyResult(@RequestBody String xmlData) {
        log.info("收到微信支付异步通知: \n{}", xmlData);
        return orderService.handleWxPayNotify(xmlData);
    }
}
