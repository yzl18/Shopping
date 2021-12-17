package com.leyou.email.mq;



import com.leyou.email.utils.MailUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class EmailListener {

    @Autowired
    private MailUtils mailUtils;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "email.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.email.exchange",type = ExchangeTypes.TOPIC),
            key = "email.verify.code"
    ))
    public void listenUserRegister(Map<String,String> map){
        String email = map.get("email");
        if (StringUtils.isBlank(email)){
            return;
        }
        //处理消息,发送验证码
        mailUtils.sendMail(email, map.get("code"));

    }
}
