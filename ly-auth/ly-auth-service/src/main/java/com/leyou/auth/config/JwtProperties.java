package com.leyou.auth.config;

import com.leyou.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {


    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private int expire;
    private String cookieName;

    private PublicKey publicKey;//公钥
    private PrivateKey privateKey;//私钥

    //对象一旦实例化后(初始化后)，就应该读取私钥和公钥
    //注解@PostConstruct，在构造函数执行后执行该注解的方法
    @PostConstruct
    public void init() throws Exception {
        //公钥或者私钥不存在,先生成，一般是测试直接先生成公钥私钥
        File pubPath = new File(pubKeyPath);
        File priPath = new File(priKeyPath);
        if (!pubPath.exists() || !priPath.exists()){
            //生成公钥私钥
            RsaUtils.generateKey(pubKeyPath,priKeyPath,secret);
        }

        //读取公钥和私钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

}
