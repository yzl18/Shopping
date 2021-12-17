package com.leyou.item.web;


import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.ISpecificationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpecificationControllerTest {

    @Autowired
    private ISpecificationService service;

    @Test
    public void queryParamList() {
        List<SpecParam> params = service.queryParamList(76L, null, true);
        for (SpecParam param : params) {
            System.out.println(param);
        }
    }
}