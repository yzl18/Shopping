package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WXPayConfiguration {

    @Bean
    public PayConfig payConfig(){
        return new PayConfig();
    }

    @Bean
    public WXPay wxPay() throws Exception {
        return new WXPay(payConfig(),"http://test.leyou.com/wxpay/notify",true,false);
    }
}
