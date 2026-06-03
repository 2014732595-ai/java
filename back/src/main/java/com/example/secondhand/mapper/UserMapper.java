package com.example.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.secondhand.entity.User;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);
}
