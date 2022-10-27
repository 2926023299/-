package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Visitors;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VisitorsApiImpl implements VisitorsApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存访客数据
     * @param visitors
     */
    @Override
    public void save(Visitors visitors) {
        //对于同一个用户，同一天的访客数据，只保存一条
        Query query = Query.query(Criteria.where("userId").is(visitors.getUserId())
                .and("visitorUserId").is(visitors.getVisitorUserId())
                .and("visitDate").is(visitors.getVisitDate()));

        if(!mongoTemplate.exists(query, Visitors.class)){
            mongoTemplate.save(visitors);
        }
    }

    /**
     * 查询访客列表
     * @param userId
     * @param date
     * @return
     */
    @Override
    public List<Visitors> queryVisitorsList(Long userId, Long date) {
        Criteria criteria = Criteria.where("userId").is(userId);

        if(date != null){
            criteria.and("date").gt(date);
        }
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("date")));

        return mongoTemplate.find(query, Visitors.class);
    }
}
