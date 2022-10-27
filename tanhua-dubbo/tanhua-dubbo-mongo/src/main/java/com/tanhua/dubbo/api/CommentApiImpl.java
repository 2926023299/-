package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Comment;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Movement;
import org.apache.dubbo.config.annotation.DubboService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@DubboService
public class CommentApiImpl implements CommentApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    //发布评论，并获取评论数量
    @Override
    public Integer save(Comment comment1) {

        //查询动态
        Movement movement = mongoTemplate.findById(comment1.getPublishId(), Movement.class);

        //向comment对象设置被评论人属性
        if (movement != null) {
            comment1.setPublishUserId(movement.getUserId());
        }

        mongoTemplate.save(comment1);

        //更新动态表中的对应字段
        Query query = Query.query(Criteria.where("id").is(comment1.getPublishId()));
        Update update = new Update();
        if (comment1.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", 1);
        } else if (comment1.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", 1);
        } else {
            update.inc("loveCount", 1);
        }

        //设置更新参数
        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);//获取更新后的最新数据
        Movement newMovement = mongoTemplate.findAndModify(query, update, options, Movement.class);

        if (newMovement != null) {
            return newMovement.statisCount(comment1.getCommentType());
        }
        return -1;
    }

    //分页查询单个动态所有评论
    @Override
    public List<Comment> findComments(String movementId, CommentType type, Integer page, Integer pagesize) {
        Criteria criteria = Criteria.where("publishId").is(new ObjectId(movementId))
                .and("commentType").is(type.getType());

        Query query = Query.query(criteria)
                .skip((long) (page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        return mongoTemplate.find(query, Comment.class);
    }

    //判断comment数据是否存在
    @Override
    public boolean hasComment(String movementId, Long userId, CommentType like) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(movementId))
                .and("commentType").is(like.getType());

        Query query = Query.query(criteria);

        return mongoTemplate.exists(query, Comment.class);
    }

    @Override
    public Integer remove(Comment comment) {
        //修改Comment表数据
        Criteria criteria = Criteria.where("userId").is(comment.getUserId())
                .and("publishId").is(comment.getPublishId())
                .and("commentType").is(comment.getCommentType());

        Query query = Query.query(criteria);

        mongoTemplate.remove(query, Comment.class);

        //修改动态表中的总数量
        Query query1 = Query.query(Criteria.where("id").is(comment.getPublishId()));
        Update update = new Update();
        if (comment.getCommentType() == CommentType.LIKE.getType()) {
            update.inc("likeCount", -1);
        } else if (comment.getCommentType() == CommentType.COMMENT.getType()) {
            update.inc("commentCount", -1);
        } else {
            update.inc("loveCount", -1);
        }
        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);//获取更新后的最新数据
        Movement newMovement = mongoTemplate.findAndModify(query1, update, findAndModifyOptions, Movement.class);

        //获取最新的点赞数量
        if (newMovement != null) {
            return newMovement.statisCount(comment.getCommentType());
        }

        return null;
    }

    //评论点赞
    @Override
    public Comment commentLike(String commentId, int count) {
        //修改评论表中的点赞数量
        Criteria criteria = Criteria.where("id").is(new ObjectId(commentId));
        Query query = Query.query(criteria);
        Update update = new Update();
        update.inc("likeCount", count);

        //设置更新参数
        FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions();
        findAndModifyOptions.returnNew(true);//获取更新后的最新数据

        return mongoTemplate.findAndModify(query, update, findAndModifyOptions, Comment.class);
    }

    @Override
    public List<Comment> findByUserId(Long userId, int page, int pagesize, CommentType type) {
        Criteria criteria = Criteria.where("publishUserId").is(userId)
                .and("commentType").is(type.getType());


        Query.query(criteria)
                .skip((long) (page - 1) * pagesize)
                .limit(pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        return mongoTemplate.find(Query.query(criteria), Comment.class);
    }

    @Override
    public List<Comment> findComments(String movementId, CommentType type) {
        Criteria criteria = Criteria.where("publishId").is(new ObjectId(movementId))
                .and("commentType").is(type.getType());

        Query query = Query.query(criteria)
                .with(Sort.by(Sort.Order.desc("created")));

        return mongoTemplate.find(query, Comment.class);
    }

}
