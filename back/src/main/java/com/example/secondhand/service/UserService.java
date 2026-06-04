package com.example.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.secondhand.entity.User;

public interface UserService extends IService<User> {

    String login(String username, String password);

    String loginBySms(String phone, String code);

    void register(String username, String password, String nickname, String phone);

    User getUserInfo(String username);

    void updateProfile(User user);

    User getByPhone(String phone);

    void updatePassword(Long userId, String oldPassword, String newPassword);
}
