package com.leyou.search.repository;

import com.github.pagehelper.PageInfo;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SpecificationClient specificationClient;
    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page=1;
        int rows=100;
        int totalPage=0;
        do {
            //查询spu信息
            PageInfo<Spu> info = goodsClient.querySpuByPage(page, rows, true, null);
            //构建出goods
            List<Spu> spuList = info.getList();
            if (CollectionUtils.isEmpty(spuList)){
                break;
            }
            List<Goods> goodList = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            repository.saveAll(goodList);
            //翻页
            page++;
            totalPage=info.getPages();
        } while (page<=totalPage);
    }


}