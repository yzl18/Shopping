package com.leyou.order.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {


    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;//公钥

    //对象一旦实例化后(初始化后)，就应该读取私钥和公钥
    //注解@PostConstruct，在构造函数执行后执行该注解的方法
    @PostConstruct
    public void init() throws Exception {

        //读取公钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}
