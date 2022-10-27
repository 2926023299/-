package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.UserLike;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class UserLikeApiImpl implements UserLikeApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Boolean saveOrUpdate(Long userId, Long likeUserId, boolean isLike) {
        try {
            Query query = new Query(Criteria.where("userId").is(userId).and("likeUserId").is(likeUserId));
            UserLike userLike = mongoTemplate.findOne(query, UserLike.class);
            if (userLike == null) {
                userLike = new UserLike();
                userLike.setUserId(userId);
                userLike.setLikeUserId(likeUserId);
                userLike.setIsLike(isLike);
                userLike.setCreated(System.currentTimeMillis());
                userLike.setUpdated(System.currentTimeMillis());
                mongoTemplate.save(userLike);
            } else {
                Update update = new Update();
                update.set("isLike", isLike);
                update.set("updated", System.currentTimeMillis());
                mongoTemplate.updateFirst(query, update, UserLike.class);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    //查询自己喜欢的有那些喜欢自己
    @Override
    public List<UserLike> likeCount(List<Long> likeUserIds, Long userId) {
        Criteria criteria = Criteria.where("likeUserId").is(userId)
                .and("isLike").is(true);

        if(likeUserIds != null && likeUserIds.size() > 0){
            criteria.and("userId").in(likeUserIds);
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, UserLike.class);
    }
}
