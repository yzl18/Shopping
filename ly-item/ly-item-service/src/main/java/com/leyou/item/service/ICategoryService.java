package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface ICategoryService {
    List<Category> queryCategoryListByPid(Long pid);

    List<Category> queryByIds(List<Long> ids);
}
