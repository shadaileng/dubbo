package com.qpf.mall;

import com.qpf.mall.service.PriceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BootServiceConsumerPriceApplicationTests {
    @Autowired
    private PriceService priceService;

    @Test
    public void contextLoads() throws IOException {
        String t1002 = priceService.priceTicket("t1002");

        System.out.println(t1002);

//        System.in.read();
    }

}
