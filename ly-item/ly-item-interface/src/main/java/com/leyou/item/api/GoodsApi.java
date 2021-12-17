package com.leyou.item.api;

import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    @GetMapping("spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId")Long spuId);

    //修改商品，根据spuId查询下面的所有Sku
    @GetMapping("sku/list")
    List<Sku> querySkusBySpuId(@RequestParam("id")Long spuId);

    @GetMapping("spu/page")
    PageInfo<Spu> querySpuByPage(
            @RequestParam(name = "page",defaultValue = "1") Integer page,
            @RequestParam(name = "rows",defaultValue = "5") Integer rows,
            @RequestParam(name = "saleable",required = false) Boolean saleable,
            @RequestParam(name = "key",required = false) String key
    );

    //根据spu的id查询spu
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    //根据id批量查询sku
    @GetMapping("sku/list/ids")
    List<Sku> querySkusByIds(@RequestParam("ids")List<Long> ids);

    //减库存
    @PostMapping("stock/decrease")
    Void decreaseStock(@RequestBody List<CartDTO> carts);
}
