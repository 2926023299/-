package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;

import java.util.List;

public interface VisitorsApi {
    /**
     * 保存访客数据
     * @param visitors
     */
    void save(Visitors visitors);

    /**
     * 查询访客列表
     * @param userId
     * @param date
     * @return
     */
    List<Visitors> queryVisitorsList(Long userId, Long date);
}
