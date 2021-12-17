package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.ISpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private ISpecificationService specificationService;

    //根据分类id查询该分类下的所有组
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        List<SpecGroup> groups=specificationService.queryGroupByCid(cid);
        return ResponseEntity.ok(groups);
    }

    //根据cid或gid或searching查询该组下的所有商品参数
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamList(
            @RequestParam(value = "cid",required = false) Long cid,
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "searching",required = false) Boolean searching
    ){
        List<SpecParam> params=specificationService.queryParamList(cid,gid,searching);
        return ResponseEntity.ok(params);
    }

    //根据分类查询规格组及组内参数
    @GetMapping("group")
    public ResponseEntity<List<SpecGroup>> queryListByCid(@RequestParam("cid") Long cid){
        return ResponseEntity.ok(specificationService.queryListById(cid));
    }
}