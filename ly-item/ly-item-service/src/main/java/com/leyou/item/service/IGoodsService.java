package com.leyou.item.service;

import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import java.util.List;

public interface IGoodsService {
    PageInfo<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key);

    void saveGoods(Spu spu);

    SpuDetail querySpuDetailBySpuId(Long spuId);

    List<Sku> querySkusBySpuId(Long spuId);

    void updateGoods(Spu spu);

    Spu querySpuById(Long id);

    List<Sku> querySkusByIds(List<Long> ids);

    void decreaseStock(List<CartDTO> carts);
}

