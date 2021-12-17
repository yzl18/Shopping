package com.leyou.item.web;

import com.github.pagehelper.PageInfo;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.IBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private IBrandService brandService;

    //分页查询品牌
    //page:当前页码，rows:每页显示的数量，sortBy: 需要排序的数据库字段，desc:是否降序排列，key:搜索框模糊查询
    @GetMapping("/page")
    public ResponseEntity<PageInfo<Brand>> queryBrandByPage(
            @RequestParam(name = "page",defaultValue = "1") Integer page,
            @RequestParam(name = "rows",defaultValue = "5") Integer rows,
            @RequestParam(name = "sortBy",required = false) String sortBy,
            @RequestParam(name = "desc",defaultValue = "false") Boolean desc,
            @RequestParam(name = "key",required = false) String key
    ){
        PageInfo<Brand> info =brandService.queryBrandByPage(page,rows,sortBy,desc,key);

        return ResponseEntity.ok(info);
    }

    //新增品牌 ,返回体没内容用Void
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids")List<Long> cids){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //根据分类id查询品牌
    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandsByCid(@PathVariable("cid")Long cid){

        return ResponseEntity.ok(brandService.queryBrandsByCid(cid));
    }

    //根据品牌id查询品牌
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.queryById(id));
    }

    @GetMapping("list")
    ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(brandService.queryBrandByIds(ids));
    }
}
