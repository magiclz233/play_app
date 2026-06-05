package com.playapp.controller;

import com.playapp.common.Result;
import com.playapp.dto.WxLoginDTO;
import com.playapp.service.UserService;
import com.playapp.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wx")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 微信小程序登录接口
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody WxLoginDTO loginDTO) {
        LoginVO loginVO = userService.wxLogin(loginDTO.getCode());
        return Result.success("登录成功", loginVO);
    }

    /**
     * 模拟登录接口（无需微信环境）
     */
    @PostMapping("/mock-login")
    public Result<LoginVO> mockLogin(@RequestBody java.util.Map<String, String> params) {
        String phone = params.getOrDefault("phone", "13800000000");
        LoginVO loginVO = userService.mockLogin(phone);
        return Result.success("模拟登录成功", loginVO);
    }
}
