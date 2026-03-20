package com.yupi.aicodehelper.controller;

import com.yupi.aicodehelper.entity.User;
import com.yupi.aicodehelper.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserRepository userRepository;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户已存在");
        }

        return String.valueOf(userRepository.save(user).getId());
    }

    @PostMapping("/login")
    public Long login(@RequestBody User user) {

        User dbUser = userRepository.findByUsername(user.getUsername());

        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("账号或密码错误");
        }

        return dbUser.getId(); // 👉 先返回 userId（后面升级JWT）
    }
}