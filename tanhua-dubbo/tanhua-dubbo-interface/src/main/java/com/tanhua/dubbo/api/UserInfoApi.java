package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.model.domain.UserInfo;

import java.util.List;
import java.util.Map;

public interface UserInfoApi {

    public void save(UserInfo userinfo);

    public void update(UserInfo userinfo);

    UserInfo findById(Long userID);

    /**
     * 批量查询用户详情, 第二个实参为条件，可以为null
     * 返回值 Map<UserId, UserInfo>
     */
    Map<Long, UserInfo> findByIds(List<Long> userIds, UserInfo info);

    /**
     * 分页查询用户
     */
    IPage<UserInfo> findAll(Integer page, Integer pagesize);
}
