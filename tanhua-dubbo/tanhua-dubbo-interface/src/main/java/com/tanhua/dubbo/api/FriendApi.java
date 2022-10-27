package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Friend;

import java.util.List;

public interface FriendApi {
    void save(Long userId, Long friendId);  //添加好友关系

    //查询好友列表
    List<Friend> findContacts(Long userId, int page, int pagesize);

    void deleteContact(Long userId, Long likeUserId);
}
