package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.*;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.dto.RecommendUserDto;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.NearUserVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TanhuaService {

    @DubboReference
    private RecommendUserApi recommendUserApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private QuestionApi questionApi;

    @DubboReference
    private UserLikeApi userLikeApi;

    @DubboReference
    private VisitorsApi visitorsApi;

    @DubboReference
    private UserLocationApi userLocationApi;

    @Autowired
    private MessageService messageService;

    @Value("${tanhua.default.recommend.users}")
    private String defaultRecommendUsers;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public TodayBest todayBest() {
        Long userId = UserHolder.getUserId();

        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(userId);
        if (recommendUser == null) {
            RecommendUser recommendUser1 = new RecommendUser();
            recommendUser1.setUserId(1L);
            recommendUser1.setScore(99d);
        }

        assert recommendUser != null;
        UserInfo userInfo = userInfoApi.findById(recommendUser.getUserId());

        return TodayBest.init(userInfo, recommendUser);
    }

    //查询推荐好友列表
    public PageResult recommendation(RecommendUserDto dto) {
        Long userId = UserHolder.getUserId();

        PageResult pr = recommendUserApi.queryRecommendList(dto.getPage(), dto.getPagesize(), userId);

        List<RecommendUser> items = (List<RecommendUser>) pr.getItems();
        if (items == null) {
            return pr;
        }

        List<Long> userIds = CollUtil.getFieldValues(items, "userId", Long.class);

        UserInfo userInfo = new UserInfo();
        userInfo.setAge(dto.getAge());
        userInfo.setGender(dto.getGender());

        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, userInfo);

        List<TodayBest> list = new ArrayList<>();
        items.forEach(i -> {
            UserInfo info = map.get(i.getUserId());
            if (info != null) {
                TodayBest best = TodayBest.init(info, i);
                list.add(best);
            }
        });

        pr.setItems(list);
        return pr;
    }

    //查询佳人详情
    public TodayBest personalInfo(String userId) {
        UserInfo userInfo = userInfoApi.findById(Long.valueOf(userId));

        //查询推荐数据
        RecommendUser recommendUser = recommendUserApi.queryByUserId(userId, UserHolder.getUserId());

        //构建返回对象
        Visitors visitors = new Visitors();
        visitors.setVisitorUserId(UserHolder.getUserId());
        visitors.setDate(System.currentTimeMillis());
        visitors.setUserId(Long.valueOf(userId));
        visitors.setFrom("首页");
        visitors.setScore(recommendUser.getScore());
        visitors.setVisitDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        visitorsApi.save(visitors);

        return TodayBest.init(userInfo, recommendUser);
    }

    //查看陌生人问题
    public String strangerQuestions(Long userId) {
        Question question = questionApi.findQuestionByUserId(userId);

        return question == null ? "who are you!" : question.getTxt();
    }

    //回复陌生人问题
    public void replyQuestions(Long userId, String reply) {
        //1、构造消息数据
        Long currentUserId = UserHolder.getUserId();
        UserInfo userInfo = userInfoApi.findById(currentUserId);
        Map map = new HashMap();
        map.put("userId", currentUserId);
        map.put("huanXinId", Constants.HX_USER_PREFIX + currentUserId);
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", this.strangerQuestions(userId));
        map.put("reply", reply);
        String message = JSON.toJSONString(map);
        //2、调用template对象，发送消息
        Boolean aBoolean = huanXinTemplate.sendMsg(Constants.HX_USER_PREFIX + userId, message);//1、接受方的环信id，2、消息
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
    }

    //探花卡片显示
    public List<TodayBest> queryCardsList() {
        //调用推荐api查询数据列表（排除喜欢/不喜欢的用户）
        List<RecommendUser> users = recommendUserApi.queryCardsList(UserHolder.getUserId());

        //判断数据是否存在，是否构建默认数据
        if (CollUtil.isEmpty(users)) {
            users = new ArrayList<>();
            String[] userIds = defaultRecommendUsers.split(",");

            for (String userId : userIds) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(userId));
                recommendUser.setScore(RandomUtil.randomDouble(60, 90));
                users.add(recommendUser);
            }

        }

        //构造返回vo对象
        List<Long> userIds = CollUtil.getFieldValues(users, "userId", Long.class);
        Map<Long, UserInfo> userInfoMap = userInfoApi.findByIds(userIds, null);

        List<TodayBest> list = new ArrayList<>();
        users.forEach(i -> {
            UserInfo userInfo = userInfoMap.get(i.getUserId());
            if (userInfo != null) {
                TodayBest todayBest = TodayBest.init(userInfo, i);
                list.add(todayBest);
            }
        });

        return list;
    }

    //探花喜欢
    public void likeUser(Long likeUserId) {
        //调用api保存喜欢数据
        Boolean flag = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, true);
        if (!flag) {
            throw new BusinessException(ErrorResult.error());
        }

        //操作redis, 写入喜欢的数据,删除不喜欢的数据
        redisTemplate.opsForSet().remove(Constants.USER_NOT_LIKE_KEY + UserHolder.getUserId(), likeUserId.toString());
        redisTemplate.opsForSet().add(Constants.USER_LIKE_KEY + UserHolder.getUserId(), String.valueOf(likeUserId));

        //判断是否为双向喜欢
        if (isMutualLike(likeUserId, UserHolder.getUserId())) {
            //添加好友
            messageService.contacts(likeUserId);
        }
    }

    //左滑功能
    public void notLikeUser(Long likeUserId) {
        //调用api保存不喜欢数据
        Boolean flag = userLikeApi.saveOrUpdate(UserHolder.getUserId(), likeUserId, false);
        if (!flag) {
            throw new BusinessException(ErrorResult.error());
        }

        //操作redis, 写入喜欢的数据,删除不喜欢的数据
        redisTemplate.opsForSet().remove(Constants.USER_LIKE_KEY + UserHolder.getUserId(), likeUserId.toString());
        redisTemplate.opsForSet().add(Constants.USER_NOT_LIKE_KEY + UserHolder.getUserId(), String.valueOf(likeUserId));

        //删除好友
        //messageService.deleteContacts(likeUserId); 有bug，暂时放弃
    }

    //判断是否双向喜欢
    public Boolean isMutualLike(Long userId, Long likeUserId) {
        //判断是否为双向喜欢
        return redisTemplate.opsForSet().isMember(Constants.USER_LIKE_KEY + userId, likeUserId.toString());
    }


    /**
     * 探花-附近的人
     *
     * @param gender
     * @param distance
     * @return
     */
    public List<NearUserVo> queryNearUser(String gender, String distance) {
        //1、调用api查询附近的用户
        List<Long> userIds = userLocationApi.queryNearUser(UserHolder.getUserId(), Double.valueOf(distance));

        if(CollUtil.isEmpty(userIds)){
            return new ArrayList<>();
        }

        //2、调用api查询用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setGender(gender);
        Map<Long, UserInfo> userInfoMap = userInfoApi.findByIds(userIds, userInfo);

        //构建vo对象
        List<NearUserVo> list = new ArrayList<>();
        userInfoMap.forEach((k, v) -> {
            NearUserVo nearVo = NearUserVo.init(v);
            list.add(nearVo);
        });

        return list;
    }
}
