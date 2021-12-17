package com.leyou.user.web;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    //注册时校验用户名、邮箱、电话是否已经存在
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkData(@PathVariable("data") String data,@PathVariable("type") Integer type){
        return ResponseEntity.ok(userService.checkData(data,type));
    }

    //发送邮箱验证码
    @PostMapping("/send")
    public ResponseEntity<Void> sendCode(@RequestParam("email") String email){
        userService.sendCode(email);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //注册用户,注解@Valid校验用户信息，具体校验规则定义在User类中;BindingResult是框架Hibernate Validator中的方法，定义校验结果信息
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, BindingResult result, @RequestParam("code") String code){
        if (result.hasErrors()){
            throw new RuntimeException(result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).collect(Collectors.joining("|")));
        }
        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //根据用户名和密码查询用户
    @GetMapping("query")
    public ResponseEntity<User> queryUserByUsernameAndPassword(
            @RequestParam("username") String username,@RequestParam("password") String password){
        return ResponseEntity.ok(userService.queryUserByUsernameAndPassword(username,password));
    }
}
