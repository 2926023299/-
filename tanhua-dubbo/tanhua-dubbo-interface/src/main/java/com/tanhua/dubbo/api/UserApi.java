package com.tanhua.dubbo.api;

import com.tanhua.model.domain.User;

public interface UserApi {

    //根据手机号码查询用户
    User findByMobile(String mobile);

    //保存用户，返回用户id
    Long save(User user);

    void updatePhone(Long userId, String phone);

    void update(User user);

    User findById(Long userId);

    User findByHuanxin(String huanxinId);

    //查询用户数量
    Integer queryCount();
}
