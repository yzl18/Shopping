package com.leyou.order.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService service;

    @Test
    public void updateStatus() {
        service.updateStatus(1446779442247499776L,1);
    }


    @Test
    public void createPayUrl() {

        String payUrl = service.createPayUrl(1446779442247499776L);
        System.out.println(payUrl);
    }

}