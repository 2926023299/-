package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.dubbo.utils.TimeLineService;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.MovementTimeLine;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class MovementApiImpl implements MovementApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TimeLineService timeLineService;

    //发布动态
    @Override
    public String publish(Movement movement) {
        try {
            //保存动态详情
            movement.setPid(idWorker.getNextId("movement"));
            movement.setCreated(System.currentTimeMillis());

            mongoTemplate.save(movement);

            //查询当前用户的好友数据
            timeLineService.saveTimeLine(movement.getUserId(), movement.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return movement.getId().toHexString();
    }

    @Override
    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        Criteria criteria = Criteria.where("userId").is(userId);

        Query query = Query.query(criteria)
                .skip((long) (page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        List<Movement> movements = mongoTemplate.find(query, Movement.class);

        return new PageResult(page, pagesize, 0, movements);
    }

    @Override
    public List<Movement> findFriendsMovements(Integer page, Integer pagesize, Long userId) {
        Query query = Query.query(Criteria.where("friendId")
                        .is(userId))
                .skip((long) (page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        List<MovementTimeLine> movementTimeLines = mongoTemplate.find(query, MovementTimeLine.class);

        //提取动态id列表
        List<ObjectId> movementIds = CollUtil.getFieldValues(movementTimeLines, "movementId", ObjectId.class);

        //根据动态id查询动态详情
        Query movementQuery = Query.query(Criteria.where("id").in(movementIds));

        return mongoTemplate.find(movementQuery, Movement.class);
    }

    /**
     * 根据pid查询
     *
     * @param pids
     * @return
     */
    @Override
    public List<Movement> findMovementsByPids(List<Long> pids) {
        Query query = Query.query(Criteria.where("pid").in(pids));

        return mongoTemplate.find(query, Movement.class);
    }

    @Override
    public List<Movement> randomMovements(Integer pagesize) {
        //创建统计对象，设置统计参数
        TypedAggregation aggregation = Aggregation.newAggregation(Movement.class, Aggregation.sample(pagesize));

        AggregationResults<Movement> aggregate = mongoTemplate.aggregate(aggregation, Movement.class);

        return aggregate.getMappedResults();
    }

    @Override
    public Movement findById(String movementId) {
        return mongoTemplate.findById(movementId, Movement.class);
    }

    /**
     * 查询动态
     *
     * @param page
     * @param pagesize
     * @param uid      非必要
     * @param state    非必要
     * @return
     */
    @Override
    public PageResult findAllMovements(Integer page, Integer pagesize, Long uid, Integer state) {
        Query query = new Query();

        if (uid != null) {
            query.addCriteria(Criteria.where("userId").is(uid));
        }

        if (state != null) {
            query.addCriteria(Criteria.where("state").is(state));
        }

        long count = mongoTemplate.count(query, Movement.class);

        query.skip((long) (page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        List<Movement> movements = mongoTemplate.find(query, Movement.class);

        return new PageResult(page, pagesize, (int) count, movements);
    }

    @Override
    public void updateState(String movementId, int state) {
        Query query = Query.query(Criteria.where("id").is(new ObjectId(movementId)));

        mongoTemplate.updateFirst(query, Update.update("state", state), Movement.class);
    }
}
