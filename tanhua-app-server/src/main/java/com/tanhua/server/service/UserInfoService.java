package com.tanhua.server.service;

import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.UserLikeApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.UserLike;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class UserInfoService {

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private UserLikeApi userLikeApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private AipFaceTemplate aipFaceTemplate;

    public void save(UserInfo userInfo) {
        userInfoApi.save(userInfo);
    }

    public void updateHead(MultipartFile headPhoto, Long id) throws IOException {
        //上传图片到阿里云oss
        String headUrl = ossTemplate.upload(headPhoto.getOriginalFilename(), headPhoto.getInputStream());

        //调用百度云识别人脸
        boolean detect = aipFaceTemplate.detect(headUrl);
        if (!detect) {
            throw new BusinessException(ErrorResult.faceError());
        } else {
            //更新用户头像
            UserInfo userInfo = new UserInfo();
            userInfo.setId(id);
            userInfo.setAvatar(headUrl);
            userInfoApi.update(userInfo);
        }
    }

    public UserInfoVo findById(Long userID) throws InvocationTargetException, IllegalAccessException {
        UserInfo userInfo = userInfoApi.findById(userID);

        UserInfoVo userInfoVo = new UserInfoVo();

        BeanUtils.copyProperties(userInfoVo, userInfo);
        if (userInfo.getAge() != null) {
            userInfoVo.setAge(String.valueOf(userInfo.getAge()));
        }
        return userInfoVo;
    }

    public void update(UserInfo userInfo) {
        userInfoApi.update(userInfo);
    }

    //查询互相喜欢、喜欢和粉丝统计
    public Map<String, Integer> counts() {
        Map<String, Integer> map = new HashMap<>();

        //查询喜欢
        Set<String> set = redisTemplate.opsForSet().members(Constants.USER_LIKE_KEY + UserHolder.getUserId());
        if(set != null) {
            map.put("loveCount", set.size());
        }

        //查询互相喜欢
        List<Long> LikeUserIds = new ArrayList<>();
        for (String s : set) {
            LikeUserIds.add(Long.valueOf(s));
        }
        List<UserLike> fans = userLikeApi.likeCount(LikeUserIds, UserHolder.getUserId());

        if(fans != null) {
            map.put("eachLoveCount", fans.size());
        }else {
            map.put("eachLoveCount", 0);
        }

        //查询粉丝
        List<UserLike> userLikes = userLikeApi.likeCount(null, UserHolder.getUserId());
        if(userLikes != null) {
            map.put("fanCount", userLikes.size());
        }

        return map;
    }
}
