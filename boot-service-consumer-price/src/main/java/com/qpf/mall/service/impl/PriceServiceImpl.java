package com.qpf.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qpf.mall.service.PriceService;
import com.qpf.mall.service.TicketService;
import org.springframework.stereotype.Service;

@Service
public class PriceServiceImpl implements PriceService {

    @Reference
    private TicketService ticketService;

    @Override
    public String priceTicket(String ticketId) {
        return "price of ticket: " + ticketService.getTicket(ticketId);
    }
}
