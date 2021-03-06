package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;

@RegisterMapper //注册通用mapper
public interface BaseMapper<T> extends Mapper<T> ,IdListMapper<T,Long> , InsertListMapper<T> {
}
