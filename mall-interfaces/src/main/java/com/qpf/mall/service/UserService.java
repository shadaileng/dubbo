package com.qpf.mall.service;

import com.qpf.mall.bean.UserAddress;

import java.util.List;

public interface UserService {
    List<UserAddress> getUserAdress(String userId);
}
