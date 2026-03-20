package com.yupi.aicodehelper.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "user") // 建议加上（避免关键字问题）
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private LocalDateTime createTime;
}