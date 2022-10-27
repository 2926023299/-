package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface RecommendUserApi {
    RecommendUser queryWithMaxScore(Long toUserId);

    PageResult queryRecommendList(Integer page, Integer pageSize, Long userId);

    RecommendUser queryByUserId(String userId, Long userId1);

    List<RecommendUser> queryCardsList(Long userId);
}
