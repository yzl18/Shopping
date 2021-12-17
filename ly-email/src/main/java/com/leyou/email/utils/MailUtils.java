package com.leyou.email.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 发邮件工具类
 */
@Slf4j
@Component
public class MailUtils {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX= "emial: ";
    private static final long SMS_MIN_INTERVAL_MILLIS= 60000;

//    private static final String USER = "yangzelin_18@163.com"; // 发件人称号，同邮箱地址
//    private static final String PASSWORD = "ZHQNWXQPKXWUPXFA"; // 如果是qq邮箱(163网易邮箱)可以使户端授权码
    private static final String USER = "1289136644@qq.com";
    private static final String PASSWORD = "ynyitcrjdpplfjif";

    /**
     *
     * @param email 收件人邮箱
     * @param code 验证码
     *
     */
    /* 发送验证信息的邮件 */
    public boolean sendMail(String email, String code){
        String key = KEY_PREFIX+email;
        //读取时间
        String lastTime = redisTemplate.opsForValue().get(key);
        //lastTime非空则表示发送过邮件
        if (StringUtils.isNotBlank(lastTime)){
            //一分钟内发送过邮件,则阻止继续发送，以免阻塞
            Long last = Long.valueOf(lastTime);
            if (System.currentTimeMillis()-last<SMS_MIN_INTERVAL_MILLIS){
                log.info("[邮箱服务] 发送验证码频率过高，被拦截，邮箱:{}",email);
                return false;
            }

        }
        try {
            final Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            //如果发件人是163邮箱,则smtp.163.com;如果是qq邮箱，则smtp.qq.com
            props.put("mail.smtp.host", "smtp.qq.com");

            // 发件人的账号
            props.put("mail.user", USER);
            //发件人的密码
            props.put("mail.password", PASSWORD);

            // 构建授权信息，用于进行SMTP进行身份验证
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    // 用户名、密码
                    String userName = props.getProperty("mail.user");
                    String password = props.getProperty("mail.password");
                    return new PasswordAuthentication(userName, password);
                }
            };
            // 使用环境属性和授权信息，创建邮件会话
            Session mailSession = Session.getInstance(props, authenticator);
            // 创建邮件消息
            MimeMessage message = new MimeMessage(mailSession);
            // 设置发件人
            String username = props.getProperty("mail.user");
            InternetAddress form = new InternetAddress(username);
            message.setFrom(form);

            // 设置收件人
            InternetAddress toAddress = new InternetAddress(email);
            message.setRecipient(Message.RecipientType.TO, toAddress);

            // 设置邮件标题
            String title = "注册激活";
            message.setSubject(title);

            // 设置邮件的内容体
            String text = "【井大电子商城】 尊敬的用户，您正在注册会员动态密码为："+code+"，感谢您的支持，验证码5分钟内有效！";
            message.setContent(text, "text/html;charset=UTF-8");
            // 发送邮件
            Transport.send(message);
            log.info("[邮箱服务] 发送验证码, 邮箱:{}",email);
            //发送短信成功后存入redis,指定生存时间为1分钟
            redisTemplate.opsForValue().set(key,String.valueOf(System.currentTimeMillis()),1, TimeUnit.MINUTES);
            return true;
        }catch (Exception e){
            log.error("[邮箱服务] 发送验证码异常, 邮箱:{}",email,e);
            return false;
        }
    }

    /*public static void main(String[] args) throws Exception { // 做测试用
        //MailUtils.sendMail("yangzelin_18@163.com","54321");
        System.out.println("发送成功");
    }*/



}
