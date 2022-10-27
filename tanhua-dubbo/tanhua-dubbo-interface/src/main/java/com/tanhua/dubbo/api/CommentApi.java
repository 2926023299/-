package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Comment;
import com.tanhua.model.enums.CommentType;

import java.util.List;

public interface CommentApi {

    //发布评论，并获取评论数量
    Integer save(Comment comment1);

    //分页查询动态评论
    List<Comment> findComments(String movementId, CommentType type, Integer page, Integer pagesize);

    //判断点赞是否存在
    boolean hasComment(String movementId, Long userId, CommentType like);

    //删除
    Integer remove(Comment comment);

    //评论点赞功能
    Comment commentLike(String commentId, int count);

    //根据用户id和评论类型查询评论
    List<Comment> findByUserId(Long userId, int page, int pagesize, CommentType type);

    //查询动态评论
    List<Comment> findComments(String movementId, CommentType type);
}
