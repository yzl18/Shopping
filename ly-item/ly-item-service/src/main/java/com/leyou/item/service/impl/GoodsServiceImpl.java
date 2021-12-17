package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import com.leyou.item.service.IBrandService;
import com.leyou.item.service.ICategoryService;
import com.leyou.item.service.IGoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements IGoodsService {
    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper detailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IBrandService brandService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageInfo<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);

        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索字段过滤 ,判断条件使用orLike还是andLike,只需看它与其他条件是or还and关系
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //上下架过滤
        if (saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }

        //默认排序
        example.setOrderByClause("last_update_time DESC");

        //查询
        List<Spu> spus = spuMapper.selectByExample(example);

        //判断
        if (CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //解析
        //解析分类和品牌的名称
        loadCategoryAndBrandName(spus);
        //解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        return info;
    }
    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }

    //新增商品
    @Override
    @Transactional
    public void saveGoods(Spu spu) {
        //新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);

        int count = spuMapper.insert(spu);
        if (count !=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增SpuDetail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());

        count = detailMapper.insert(detail);
        if (count !=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //新增Sku和库存
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());

    }
    public void saveSkuAndStock(Spu spu){
        int count;
        //定义库存集合
        List<Stock> stockList = new ArrayList<>();
        //新增Sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());

            count = skuMapper.insert(sku);
            if (count !=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }
            //新增stock库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());

            stockList.add(stock);

            //循环新增
            /*count = stockMapper.insert(stock);
            if (count !=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }*/

        }
        //批量新增 ,返回修改的数量
        count = stockMapper.insertList(stockList);
        if (count != stockList.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail detail = detailMapper.selectByPrimaryKey(spuId);
        if (detail == null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return detail;
    }

    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        //查询库存
        /*for (Sku s : skus) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if (stock == null){
                throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        }*/
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        loadStockInSku(ids, skus);
        return skus;
    }

    @Override
    @Transactional
    public void updateGoods(Spu spu) {
        //商品id不能为空
        if (spu.getId() == null){
            throw new LyException(ExceptionEnum.GOODS_ID_CANNOT_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> skuIds = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(skuIds);
        }

        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        count = detailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveSkuAndStock(spu);

        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());

    }

    @Override
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //查询sku
        spu.setSkus(querySkusBySpuId(id));

        //查询detail
        spu.setSpuDetail(querySpuDetailBySpuId(id));

        return spu;
    }

    @Override
    public List<Sku> querySkusByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        loadStockInSku(ids, skus);
        return skus;
    }

    private void loadStockInSku(List<Long> ids, List<Sku> skus) {
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        //将stocks集合转换为key是spuId,value是stock库存的Map集合
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s->s.setStock(map.get(s.getId())));
    }

    @Transactional
    @Override
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count !=1){
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }


}
