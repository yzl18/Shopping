package com.leyou.order.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("notify")
public class NotifyController {

    @GetMapping("{id}")
    public String hello(@PathVariable("id")Long id){
        System.out.println("id: "+ id);
        return "id: "+ id;
    }
}
