package com.leyou.auth.web;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties prop;

    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletResponse response, HttpServletRequest request){
        //登录
        String token = authService.login(username, password);
        //将token写入cookie
        /*Cookie cookie = new Cookie(prop.getCookieName(),token);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);*/
        CookieUtils.setCookie(request,response,prop.getCookieName(),token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //校验用户登录状态
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(@CookieValue("LY_TOKEN") String token,
                                           HttpServletResponse response, HttpServletRequest request){
       /*   可以不用写，下面统一抛出异常
       if (StringUtils.isBlank(token)){
            //如果没有token，证明未登录，返回403
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
*/
        //解析token
        try {
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());

            //用户在活跃，刷新token，以免半小时过期,即重新生成token,并写入Cookie
            String newToken = JwtUtils.generateToken(info, prop.getPrivateKey(), prop.getExpire());
            CookieUtils.setCookie(request,response,prop.getCookieName(),newToken);

            //已登录，返回用户信息
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            //token已过期，或者被篡改了
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }

    }
}
