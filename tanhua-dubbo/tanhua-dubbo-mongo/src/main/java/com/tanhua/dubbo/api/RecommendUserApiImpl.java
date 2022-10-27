package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.mongo.UserLike;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class RecommendUserApiImpl implements RecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public RecommendUser queryWithMaxScore(Long toUserId) {
        //根据touserid查询，根据评分来score排序,

        //构建Criteria
        Criteria criteria = Criteria.where("toUserId").is(toUserId);

        //构建Query对象
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score"))).limit(1);

        return mongoTemplate.findOne(query, RecommendUser.class);
    }

    @Override
    public PageResult queryRecommendList(Integer page, Integer pageSize, Long userId) {
        //构建Criteria对象
        Criteria criteria = Criteria.where("toUserId").is(userId);

        //构建query对象
        Query query = Query.query(criteria);

        //查询总数
        long count = mongoTemplate.count(query, RecommendUser.class);

        query.with(Sort.by(Sort.Order.desc("score"))).limit((page - 1) * pageSize).skip(pageSize);
        List<RecommendUser> list = mongoTemplate.find(query, RecommendUser.class);

        return new PageResult(page, pageSize, (int) count, list);
    }

    /**
     * @param userId   用户id
     * @param toUserId 被推荐人id
     * @return
     */
    @Override
    public RecommendUser queryByUserId(String userId, Long toUserId) {
        //构建Criteria对象
        Criteria criteria = Criteria.where("userId").is(userId).and("toUserId").is(toUserId);

        //构建Query对象
        Query query = Query.query(criteria);

        RecommendUser one = mongoTemplate.findOne(query, RecommendUser.class);
        if (one == null) {
            one = new RecommendUser();
            one.setUserId(Long.valueOf(userId));
            one.setToUserId(toUserId);
            //构建缘分值
            one.setScore(95d);
        }

        return one;
    }

    @Override
    public List<RecommendUser> queryCardsList(Long userId) {
        //查询喜欢或者不喜欢的用户
        List<UserLike> userLikes = mongoTemplate.find(Query.query(Criteria.where("toUserId").is(userId)), UserLike.class);
        List<Long> userLikeIds = CollUtil.getFieldValues(userLikes, "likeUserId", Long.class);

        //构建查询推荐用户的条件
        Criteria criteria = Criteria.where("toUserId").is(userId).and("userId").nin(userLikeIds);

        //使用统计函数，随机获取推荐的用户列表
        TypedAggregation<RecommendUser> recommendUserTypedAggregation = TypedAggregation.newAggregation(
                RecommendUser.class,
                Aggregation.match(criteria),
                Aggregation.sample(10)
        );

        return mongoTemplate.aggregate(recommendUserTypedAggregation, RecommendUser.class).getMappedResults();
    }
}
