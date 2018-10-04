package com.qpf.mall.service.impl;

import com.qpf.mall.bean.UserAddress;
import com.qpf.mall.service.OrderService;
import com.qpf.mall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private UserService userService;

    @Override
    public String initOrder(String userId) {
        System.out.println("创建订单: ==========================");
        System.out.println("用户地址: =============");
        List<UserAddress> userAddresses = userService.getUserAdress(userId);
        for (UserAddress userAddress : userAddresses) {
            System.out.println("=========: " + userAddress);
        }
        System.out.println("用户地址: =============");

        return "用户地址: " + userAddresses;
    }
}
