package com.tanhua.admin.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ManagerService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private MovementApi movementApi;

    @DubboReference
    private CommentApi commentApi;

    /**
     * 查询所有用户
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findAllUsers(Integer page, Integer pagesize) {
        IPage<UserInfo> iPage = userInfoApi.findAll(page, pagesize);

        List<UserInfo> records = iPage.getRecords();
        for (UserInfo record : records) {
            //查询redis中的冻结状态
            String key = Constants.USER_FREEZE + record.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                record.setUserStatus("2");
            } else {
                record.setUserStatus("1");
            }
        }

        return new PageResult(page, pagesize, (int) iPage.getTotal(), records);
    }

    /**
     * 根据id查询用户详情
     *
     * @param userId
     * @return
     */
    public UserInfo findUserById(Long userId) {
        UserInfo userInfo = userInfoApi.findById(userId);

        //查询redis中的冻结状态
        String key = Constants.USER_FREEZE + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            userInfo.setUserStatus("2");
        } else {
            userInfo.setUserStatus("1");
        }

        return userInfo;
    }


    /**
     * 查询指定用户发布的所有视频列表
     *
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    public PageResult findAllVideos(Integer page, Integer pagesize, Long uid) {
        return videoApi.findByUserId(page, pagesize, uid);
    }

    /**
     * 查询指定用户发布的所有动态列表
     *
     * @param page
     * @param pagesize
     * @param uid
     * @param state
     * @return
     */
    public PageResult findMovement(Integer page, Integer pagesize, Long uid, Integer state) {
        PageResult result = movementApi.findAllMovements(page, pagesize, uid, state);

        List<Movement> items = (List<Movement>) result.getItems();

        //构建vo对象
        if (CollUtil.isEmpty(items)) {
            return new PageResult();
        }

        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);
        Map<Long, UserInfo> userInfoMap = userInfoApi.findByIds(userIds, null);

        List<MovementsVo> list = new ArrayList<>();
        for (Movement movement : items) {
            UserInfo userInfo = userInfoMap.get(movement.getUserId());

            if (userInfo == null) {
                continue;
            }

            MovementsVo vo = MovementsVo.init(userInfo, movement);
            list.add(vo);
        }

        result.setItems(list);

        return result;
    }

    /**
     * 根据动态id查询动态详情
     *
     * @param id
     * @return
     */
    public MovementsVo findMovementById(String id) {

        Movement movement = movementApi.findById(id);
        if (movement == null) {
            return null;
        }

        //查询用户信息
        UserInfo userInfo = userInfoApi.findById(movement.getUserId());
        if (userInfo == null) {
            return null;
        }

        //构建vo对象
        return MovementsVo.init(userInfo, movement);
    }

    /**
     * 查询评论列表
     *
     * @param page
     * @param pagesize
     * @param movementId
     * @return
     */
    public PageResult findComments(Integer page, Integer pagesize, String movementId) {
        Movement movement = movementApi.findById(movementId);

        //查找评论列表
        /*List<Comment> comments = commentApi.findComments(movementId, CommentType.COMMENT, page, pagesize);*/
        List<Comment> comments = commentApi.findComments(movementId, CommentType.COMMENT);
        if (CollUtil.isEmpty(comments)) {
            return new PageResult();
        }

        //查询用户信息
        List<Long> userIds = CollUtil.getFieldValues(comments, "userId", Long.class);
        if (CollUtil.isEmpty(userIds)) {
            return new PageResult();
        }

        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //构建vo对象
        List<CommentVo> list = new ArrayList<>();
        for (Comment comment : comments) {
            UserInfo userInfo = map.get(comment.getUserId());
            if (userInfo == null) {
                continue;
            }
            CommentVo vo = CommentVo.init(userInfo, comment);
            list.add(vo);
        }

        return new PageResult(page, pagesize, (int) movement.getCommentCount(), list);
    }

    /**
     * 用户冻结
     *
     * @param params
     * @return
     */
    //用户冻结
    public Map userFreeze(Map params) {
        int freezingTime = Integer.parseInt(params.get("freezingTime").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        int days = 0;
        if (freezingTime == 1) {
            days = 3;
        }
        if (freezingTime == 2) {
            days = 7;
        }
        if (freezingTime == 3) {
            days = -1;
        }
        String value = JSON.toJSONString(params);
        redisTemplate.opsForValue().set(Constants.USER_FREEZE + userId, value, days, TimeUnit.MINUTES);
        Map map = new HashMap();
        map.put("message", "冻结成功");
        return map;
    }

    //用户解冻
    public Map userUnfreeze(Map params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        redisTemplate.delete(Constants.USER_FREEZE + userId);
        Map map = new HashMap();
        map.put("message", "解冻成功");
        return map;
    }
}
