package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.Announcement;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.vo.*;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private FriendApi friendApi;

    @DubboReference
    private MovementApi movementApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @DubboReference
    private AnnouncementApi announcementApi;

    @DubboReference
    private CommentApi commentApi;

    //根据环信用户id，查询用户详情
    public UserInfoVo findUserInfo(String huanxinId) {
        //1、根据环信id查询用户id
        User user = userApi.findByHuanxin(huanxinId);

        //2、根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(user.getId());

        //构建返回对象
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, userInfoVo);
        if (userInfo.getAge() != null) {
            userInfoVo.setAge(userInfo.getAge().toString());
        }

        return userInfoVo;
    }

    //添加好友关系
    public void contacts(Long friendId) {
        //1、将好友关系注册到环信
        Boolean aBoolean = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + UserHolder.getUserId(),
                Constants.HX_USER_PREFIX + friendId);
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
        //2、如果注册成功，记录好友关系到mongodb
        friendApi.save(UserHolder.getUserId(), friendId);
    }

    /**
     * 查找联系人
     *
     * @param page     页码
     * @param pagesize 每页显示的条数
     * @param keyword  搜索关键字
     * @return 分页结果
     */
    public PageResult findContacts(int page, int pagesize, String keyword) {
        //查找好友关系
        List<Friend> friends = friendApi.findContacts(UserHolder.getUserId(), page, pagesize);

        //查找好友详情
        if (friends.isEmpty()) {
            return new PageResult();
        }
        List<Long> userIds = CollUtil.getFieldValues(friends, "friendId", Long.class);

        UserInfo userInfo = new UserInfo();
        userInfo.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, userInfo);

        //构建返回对象
        List<ContactVo> contactsVo = CollUtil.newArrayList();
        for (Friend friend : friends) {
            UserInfo info = map.get(friend.getFriendId());
            ContactVo contactVo = ContactVo.init(info);
            contactsVo.add(contactVo);
        }

        return new PageResult(page, pagesize, contactsVo.size(), contactsVo);
    }

    //查询点赞列表
    public PageResult findLikes(int page, int pagesize) {
        //2、查询评论列表
        List<Comment> list1 = commentApi.findByUserId(UserHolder.getUserId(), page, pagesize, CommentType.LIKE);
        List<Comment> list2 = commentApi.findByUserId(UserHolder.getUserId(), page, pagesize, CommentType.COMMENT);

        List<Comment> list = (List<Comment>) CollUtil.addAll(list1, list2);

        //3、查询点赞人详情
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //4、构建返回对象
        List<CommentVo> commentVo = CollUtil.newArrayList();
        for (Comment comment : list) {
            CommentVo vo = CommentVo.init(map.get(comment.getUserId()), comment);
            commentVo.add(vo);
        }

        commentVo.sort((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()));

        return new PageResult(page, pagesize, commentVo.size(), commentVo);
    }

    //查询评论列表
    public PageResult findComments(int page, int pagesize) {
        //1、查询评论列表
        List<Comment> comments = commentApi.findByUserId(UserHolder.getUserId(), page, pagesize, CommentType.COMMENT);

        //2、查询评论人详情
        List<Long> userIds = CollUtil.getFieldValues(comments, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //3、构建返回对象
        List<CommentVo> commentVo = CollUtil.newArrayList();
        for (Comment comment : comments) {
            CommentVo vo = CommentVo.init(map.get(comment.getUserId()), comment);
            commentVo.add(vo);
        }

        commentVo.sort((o1, o2) -> o2.getCreateDate().compareTo(o1.getCreateDate()));

        return comments.isEmpty() ? new PageResult() : new PageResult(page, pagesize, commentVo.size(), commentVo);
    }

    //查询喜欢列表
    public PageResult findLoves(int page, int pagesize) {
        //查询评论列表
        List<Comment> commentList = commentApi.findByUserId(UserHolder.getUserId(), page, pagesize, CommentType.LOVE);
        if (commentList.isEmpty()) {
            return new PageResult();
        }

        //查询评论人详情
        List<Long> userIds = CollUtil.getFieldValues(commentList, "userId", Long.class);

        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //构建返回对象
        List<CommentVo> commentVo = CollUtil.newArrayList();
        for (Comment comment : commentList) {
            CommentVo vo = CommentVo.init(map.get(comment.getUserId()), comment);
            commentVo.add(vo);
        }

        return commentList.isEmpty() ? new PageResult() : new PageResult(page, pagesize, commentVo.size(), commentVo);
    }

    //查看公告
    public PageResult findAnnouncements(int page, int pagesize) {
        List<Announcement> announcements =  announcementApi.findAnnouncements(page, pagesize);
        if (announcements.isEmpty()) {
            return new PageResult();
        }

        List<AnnouncementVo> vos = new ArrayList<>();
        for (Announcement announcement : announcements) {
            AnnouncementVo announcementVo = AnnouncementVo.init(announcement);
            vos.add(announcementVo);
        }

        return new PageResult(page, pagesize, announcements.size(), vos);
    }

    //删除好友
    public void deleteContacts(Long likeUserId) {
        huanXinTemplate.deleteContact(UserHolder.getUserId().toString(), likeUserId.toString());

        friendApi.deleteContact(UserHolder.getUserId(), likeUserId);
    }
}
