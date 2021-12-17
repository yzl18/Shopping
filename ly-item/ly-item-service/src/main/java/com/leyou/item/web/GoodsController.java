package com.leyou.item.web;

import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;

    //分页查询Spu
    @GetMapping("spu/page")
    public ResponseEntity<PageInfo<Spu>> querySpuByPage(
            @RequestParam(name = "page",defaultValue = "1") Integer page,
            @RequestParam(name = "rows",defaultValue = "5") Integer rows,
            @RequestParam(name = "saleable",required = false) Boolean saleable,
            @RequestParam(name = "key",required = false) String key
    ){
        return ResponseEntity.ok(goodsService.querySpuByPage(page,rows,saleable,key));
    }

    //商品新增
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu){
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //修改商品，根据spuId查询商品详情SpuDetail
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        return ResponseEntity.ok(goodsService.querySpuDetailBySpuId(spuId));
    }

    //修改商品，根据spuId查询下面的所有Sku
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id")Long spuId){
        return ResponseEntity.ok(goodsService.querySkusBySpuId(spuId));
    }

    //查询购物车商品价格是否改变，根据sku的id集合查询所有sku
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkusByIds(@RequestParam("ids")List<Long> ids){
        return ResponseEntity.ok(goodsService.querySkusByIds(ids));
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu){
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //根据spu的id查询spu
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    //减库存
    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts){
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
