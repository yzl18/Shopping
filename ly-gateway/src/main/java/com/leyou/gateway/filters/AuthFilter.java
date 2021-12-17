package com.leyou.gateway.filters;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;


import javax.servlet.http.HttpServletRequest;
/*加上@Component注解，就相当于在spring配置文件中配置了bean*/
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProp;

    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; //过滤器类型，前置过滤
    }

    @Override
    public int filterOrder() {
        //过滤器顺序，在官方(5)的前面
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    // 是否执行该过滤器，此处为true，说明需要过滤,执行run方法
    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取请求的url路径
        String path = request.getRequestURI();
        //判断是否拦截，拦截则true，放行则false,白名单放行
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
        //遍历白名单,是白名单的返回true
        for (String allowPath : filterProp.getAllowPaths()) {
            if (path.startsWith(allowPath)){
                return true;
            }
        }
        return false;
    }

    //过滤器逻辑
    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProp.getCookieName());
        //解析token
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProp.getPublicKey());
            //登录成功，校验权限
        } catch (Exception e) {
            //解析token失败，未登录,拦截
            context.setSendZuulResponse(false);
            //返回状态码,未授权
            context.setResponseStatusCode(403);
        }
        return null;
    }
}
