package com.leyou.item.service;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;

import java.util.List;

public interface ISpecificationService {

    List<SpecGroup> queryGroupByCid(Long cid);

    List<SpecParam> queryParamList(Long cid, Long gid, Boolean searching);

    List<SpecGroup> queryListById(Long cid);
}
