package com.qpf.mall.service.impl;

import com.qpf.mall.bean.UserAddress;
import com.qpf.mall.service.UserService;

import java.util.Arrays;
import java.util.List;

public class UserServicerImpl implements UserService {
    @Override
    public List<UserAddress> getUserAdress(String userId) {

        UserAddress userAddress1 = new UserAddress(1, userId, "GZ", "qpf", "13800000001", true);
        UserAddress userAddress2 = new UserAddress(2, userId, "ZJ", "sdl", "13800000002", false);

        return Arrays.asList(userAddress1, userAddress2);
    }
}
