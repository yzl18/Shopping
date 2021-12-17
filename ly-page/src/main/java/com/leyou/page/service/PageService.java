package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //查询skus
        List<Sku> skus = spu.getSkus();
        //查询detail
        SpuDetail spuDetail = spu.getSpuDetail();
        //查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询categories
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询specs
        List<SpecGroup> groups = specificationClient.queryListByCid(spu.getCid3());

        // 查询特殊的规格参数
        List<SpecParam> params = specificationClient.queryParamList(spu.getCid3(),null,null);
        Map<Long, String> paramMap = new HashMap<>();
        for (SpecParam param : params) {
            paramMap.put(param.getId(), param.getName());
        }

        model.put("spu",spu);
        model.put("skus",skus);
        model.put("spuDetail",spuDetail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("groups",groups);
        model.put("paramMap",paramMap);
        return model;
    }

    public void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest = new File("F:\\upload", spuId + ".html");
        // exists:测试此抽象路径名表示的文件或目录是否存在。
        if (dest.exists()){
            dest.delete();
        }

        try (PrintWriter write = new PrintWriter(dest,"Utf-8")){

            //生成html
            templateEngine.process("item",context,write);
        } catch (Exception e) {
            log.error("[静态页服务] 生产静态页异常！",e);
        }
    }

    public void deleteHtml(Long spuId) {
        File dest = new File("F:\\upload", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
