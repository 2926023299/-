package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.UserLike;

import java.util.List;

public interface UserLikeApi {
    Boolean saveOrUpdate(Long userId, Long likeUserId, boolean b);

    //查询自己喜欢的有那些喜欢自己
    List<UserLike> likeCount(List<Long> likeUserIds, Long userId);
}
