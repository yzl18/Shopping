package com.leyou.order.web;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import com.leyou.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    //创建订单
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderDTO orderDTO){

        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }

    //根据id查询订单
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.queryOrderById(id));
    }

    @GetMapping("url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id")Long id){
        return ResponseEntity.ok(orderService.createPayUrl(id));
    }
}
