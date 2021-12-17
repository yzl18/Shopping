package com.leyou;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void TestSend(){
        Map<String,String> map = new HashMap<>();
        map.put("email","yangzelin_18@163.com");
        map.put("code","5555");
        amqpTemplate.convertAndSend("ly.email.exchange","email.verify.code",map);
    }
}
