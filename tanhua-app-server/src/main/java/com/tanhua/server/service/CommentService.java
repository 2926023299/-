package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommentService {

    @DubboReference
    private CommentApi commentApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //发布评论
    public void saveComment(String movementId, String comment) {
        Long userId = UserHolder.getUserId();

        Comment comment1 = new Comment();
        comment1.setPublishId(new ObjectId(movementId));
        comment1.setCommentType(CommentType.COMMENT.getType());
        comment1.setUserId(userId);
        comment1.setContent(comment);
        comment1.setCreated(System.currentTimeMillis());

        Integer commentCount = commentApi.save(comment1);
        log.info("commentCount = " + commentCount);
    }

    //分页查询某个动态的评论列表
    public PageResult findFriendMovements(Integer page, Integer pagesize, String movementId) {
        List<Comment> list = commentApi.findComments(movementId, CommentType.COMMENT, page, pagesize);

        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }

        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //构建vo对象
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            if (userInfo != null) {
                CommentVo vo = CommentVo.init(userInfo, comment);

                //拼接redis的key，将用户的点赞状态存入redis
                String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
                String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getUserId();

                if (redisTemplate.opsForHash().hasKey(key, hashKey)) {
                    vo.setHasLiked(1);
                }

                vos.add(vo);
            }
        }

        return new PageResult(page, pagesize, 0, vos);
    }

    //动态点赞
    public Integer likeComment(String movementId) {
        //调用api查询用户是否已点赞
        boolean has = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LIKE);

        if (has) {
            throw new BusinessException(ErrorResult.likeError());
        }

        Comment comment = new Comment();
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setPublishId(new ObjectId(movementId));
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());

        Integer count = commentApi.save(comment);

        //拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();

        redisTemplate.opsForHash().put(key, hashKey, "1");

        return count;
    }

    //取消点赞
    public Integer notLikeComment(String movementId) {
        //调用api查询用户是否已点赞
        boolean has = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LIKE);

        if (!has) {
            throw new BusinessException(ErrorResult.disLikeError());
        }

        Comment comment = new Comment();
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setPublishId(new ObjectId(movementId));
        comment.setUserId(UserHolder.getUserId());

        Integer count = commentApi.remove(comment);

        //拼接redis的key，将用户的点赞状态redis中删除
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();

        redisTemplate.opsForHash().delete(key, hashKey);

        return count;
    }

    //喜欢
    public Integer loveComment(String movementId) {
        //调用api查询用户是否已喜欢
        boolean has = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LOVE);

        if (has) {
            throw new BusinessException(ErrorResult.loveError());
        }

        Comment comment = new Comment();
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setPublishId(new ObjectId(movementId));
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());

        Integer count = commentApi.save(comment);

        //拼接redis的key，将用户的喜欢状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();

        redisTemplate.opsForHash().put(key, hashKey, "1");

        return count;
    }

    //取消喜欢
    public Integer notLoveComment(String movementId) {
        //调用api查询用户是否已点赞
        boolean has = commentApi.hasComment(movementId, UserHolder.getUserId(), CommentType.LOVE);

        if (!has) {
            throw new BusinessException(ErrorResult.disloveError());
        }

        Comment comment = new Comment();
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setPublishId(new ObjectId(movementId));
        comment.setUserId(UserHolder.getUserId());

        Integer count = commentApi.remove(comment);

        //拼接redis的key，将用户的点赞状态redis中删除
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();

        redisTemplate.opsForHash().delete(key, hashKey);

        return count;
    }

    //评论点赞
    public Integer commentLike(String commentId) {
        //拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getUserId();

        //调用api查询用户是否已点赞
        if(redisTemplate.opsForHash().hasKey(key, hashKey)){
            throw new BusinessException(ErrorResult.likeError());
        }

        Comment comment = commentApi.commentLike(commentId, 1);

        redisTemplate.opsForHash().put(key, hashKey, "1");

        return comment.getLikeCount();
    }

    //取消点赞
    public Integer commentNotLike(String commentId) {
        //拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + commentId;
        String hashKey = Constants.COMMENT_LIKE_HASHKEY + UserHolder.getUserId();

        //调用api查询用户是否已点赞
        if(!redisTemplate.opsForHash().hasKey(key, hashKey)){
            throw new BusinessException(ErrorResult.disLikeError());
        }

        Comment comment = commentApi.commentLike(commentId, -1);

        redisTemplate.opsForHash().delete(key, hashKey);

        return comment.getLikeCount();
    }
}
