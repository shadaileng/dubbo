package com.qpf.mall.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qpf.mall.service.TicketService;

@Service
public class TicketServiceImpl implements TicketService {
    @Override
    public String getTicket(String ticketId) {
        return "Ticket - " + ticketId;
    }
}
