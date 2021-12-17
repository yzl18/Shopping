package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.IBrandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;


@Service
public class BrandServiceImpl implements IBrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageInfo<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页
        PageHelper.startPage(page,rows);

        //过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            //增加标准，即增加查询条件
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter",key.toUpperCase());
        }
        //排序，sortBy:排序字段
        if (StringUtils.isNotBlank(sortBy)){
            String orderByClause=sortBy + (desc ? " DESC" : " ASC");
            //增加order by 子句
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> list = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        PageInfo<Brand> info = new PageInfo<>(list);


        return info;
    }

    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        int count = brandMapper.insert(brand);
        //判断是否新增成功
        if (count != 1){
            //新增失败
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增品牌与分类的中间表数据
        for (Long cid : cids) {
            count= brandMapper.insertCategoryBrand(cid, brand.getId());
            if (count != 1){
                //新增失败
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    @Override
    public Brand queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    @Override
    public List<Brand> queryBrandsByCid(Long cid) {
        List<Brand> list = brandMapper.selectBrandByCid(cid);
        if (CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return list;
    }

    @Override
    public List<Brand> queryBrandByIds(List<Long> ids) {
        List<Brand> brands = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
