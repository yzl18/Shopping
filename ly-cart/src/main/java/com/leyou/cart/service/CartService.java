package com.leyou.cart.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String KEY_PREFIX="cart:uid:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();

        //购物车结构Map<String,Map<String,String>>,第一个String:用户ID，第二个：商品ID，第三个：商品信息
        //key,第一个String
        String key = KEY_PREFIX + user.getId();
        //hashKey,第二个String
        String hashKey=cart.getSkuId().toString();

        //绑定key
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        //判断当前购物车商品是否存在
        if (operation.hasKey(hashKey)) {
            //存在，修改商品
            //从redis取出商品数据
            String json_cart = operation.get(hashKey).toString();
            Cart cacheCart = JsonUtils.toBean(json_cart, Cart.class);
            cacheCart.setNum(cacheCart.getNum() + cart.getNum());
            //写回redis
            operation.put(hashKey,JsonUtils.toString(cacheCart));

        }else {
            //否，添加商品
            operation.put(hashKey,JsonUtils.toString(cart));
        }



    }

    public List<Cart> queryCartList() {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //购物车结构Map<String,Map<String,String>>,第一个String:用户ID，第二个：商品ID，第三个：商品信息
        //key,第一个String
        String key = KEY_PREFIX + user.getId();
        //判断是否购物车为空
        if (!redisTemplate.hasKey(key)){
            //为空，返回404
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }

        //绑定key
        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        //获取购物车所有商品
        List<Cart> carts = operation.values().stream().map(o -> JsonUtils.toBean(o.toString(), Cart.class))
                .collect(Collectors.toList());

        return carts;
    }

    public void updateCartNum(Long skuId, Integer num) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key,第一个String
        String key = KEY_PREFIX + user.getId();
        //获取操作
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);

        if (!operations.hasKey(skuId.toString())){
            //key不存在，返回404
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        //查询购物车该商品的信息
        Cart cart = JsonUtils.toBean(operations.get(skuId.toString()).toString(), Cart.class);
        cart.setNum(num);

        //写回redis
        operations.put(skuId.toString(),JsonUtils.toString(cart));
    }

    public void deleteCart(Long skuId) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        //key,第一个String
        String key = KEY_PREFIX + user.getId();
        //删除
        redisTemplate.opsForHash().delete(key,skuId.toString());
    }
}
