package com.playapp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.playapp.entity.User;
import com.playapp.vo.LoginVO;

public interface UserService extends IService<User> {
    
    /**
     * 微信小程序登录
     * @param code 微信登录 code
     * @return 登录结果包含 token 和用户信息
     */
    LoginVO wxLogin(String code);

    /**
     * 模拟登录 (仅限本地开发测试使用)
     * @param phone 模拟手机号
     */
    LoginVO mockLogin(String phone);
}
