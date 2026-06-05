package com.playapp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playapp.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
