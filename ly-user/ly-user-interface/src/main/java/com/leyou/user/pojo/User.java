package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Table(name = "tb_user")
@Data
public class User {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;

    @NotEmpty(message = "用户名不能为空")
    @Length(min = 4,max = 32,message = "用户名长度必须在4~32位")
    private String username;// 用户名

    @Length(min = 4,max = 32,message = "密码长度必须在4~32位")
    @JsonIgnore//当该实体类返回json数据时，忽略该字段,不会将该字段返回
    private String password;// 密码

    @Pattern(regexp = "^1(3[0-9]|4[01456879]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}$",message ="手机号不正确" )
    private String phone;// 电话

    @Email
    private String email;// 邮箱

    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值
}