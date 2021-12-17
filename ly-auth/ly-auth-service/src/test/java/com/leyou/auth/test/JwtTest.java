package com.leyou.auth.test;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "E:\\IdeaProjects\\leyou\\rsa\\rsa.pub";

    private static final String priKeyPath = "E:\\IdeaProjects\\leyou\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test //测试时需将@Before方法注释掉
    public void testRsa() throws Exception {
        //secret:盐，登录校验密钥
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "ly@Login(Auth}*^31)&yun6%f3q2");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTYzMTYyOTY4Mn0.Y2ggVX6bLDgi-uU3FoIqD7lTLu-hZPRyUwzBJYeBHyXyqxbn4lvVRJ1Ntyl7h_lLva6LQKnAsd35YPhNz4ORKPY1yEa6fdxL3gqzC1e4D0_hEy5LLIfHeSvzsL6Y7TkG17PR5EojSw1lnE41ItmT-bADAExTXwtNFn917JGYRYQ";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}