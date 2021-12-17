package com.leyou.order.service;

import com.leyou.auth.pojo.UserInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        //1、新增订单
        Order order = new Order();
        //1.1、订单编号,基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        //1.2、用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId());
        order.setBuyerNick(user.getUsername());
        order.setBuyerRate(false);//买家是否评价
        //1.3、收货人地址
        AddressDTO addr = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getDistrict());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverState(addr.getState());
        order.setReceiverZip(addr.getZipCode());
        //1.4、金额
        //key是skuId,value是数量num
        Map<Long, Integer> numMap = orderDTO.getCarts().stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum,(s, a) -> s + a));
        Set<Long> ids = numMap.keySet();
        //Set集合转为List集合，直接new List集合，将Set集合传进去即可

        List<Sku> skus = goodsClient.querySkusByIds(new ArrayList<>(ids));

        //准备orderDetail集合
        List<OrderDetail> details = new ArrayList<>();

        long totalPay = 0L;
        for (Sku sku : skus) {
            //计算商品总金额
            totalPay += sku.getPrice() * numMap.get(sku.getId());

            //封装orderDetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(numMap.get(sku.getId()));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            details.add(orderDetail);
        }
        //商品总金额
        order.setTotalPay(totalPay);
        //实付金额
        order.setActualPay(totalPay + order.getPostFee() - 0);
        //1.5、将order写入数据库
        int count = orderMapper.insertSelective(order);
        if (count != 1){
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //2、新增订单详情
        count = orderDetailMapper.insertList(details);
        if (count != details.size()){
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        //3、新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(order.getOrderId());
        orderStatus.setStatus(OrderStatusEnum.UN_PAY.value());
        count = orderStatusMapper.insertSelective(orderStatus);
        if (count != 1){
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        //4、减库存
        goodsClient.decreaseStock(orderDTO.getCarts());

        //TODO 在redis中清空购物车已提交的商品


        return orderId;
    }

    public Order queryOrderById(Long id) {
        //查询订单
        Order order = orderMapper.selectByPrimaryKey(id);
        if (order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        //查询订单详情
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(detail);
        if (CollectionUtils.isEmpty(details)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(details);

        //查询订单状态
        OrderStatus status = orderStatusMapper.selectByPrimaryKey(id);
        if (status == null){
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOUND);
        }
        order.setOrderStatus(status);
        return order;
    }

    public void updateStatus(Long orderId, int status) {
        int count = orderStatusMapper.updateStatus(orderId, status);
        System.out.println(count);
    }

    public String createPayUrl(Long id) {
        /*String payUrl = payHelper.createPayUrl(id);*/
       return "【商城提示】 目前暂未开通微信付款";
    }
}
