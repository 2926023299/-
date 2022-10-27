package com.tanhua.dubbo.api;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.mappers.UserMapper;
import com.tanhua.model.domain.User;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class UserApiImpl  implements UserApi{

    @Autowired
    private UserMapper userMapper;

    //根据手机号码查询用户
    @Override
    public User findByMobile(String mobile) {
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda()
                .eq(User::getMobile, mobile);

        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public Long save(User user) {
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updatePhone(Long userId, String phone) {
        User user = new User();
        user.setId(userId);
        user.setMobile(phone);
        System.out.println(user);
        userMapper.updateById(user);
    }

    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User findByHuanxin(String huanxinId) {
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda()
                .eq(User::getHxUser, huanxinId);

        return userMapper.selectOne(queryWrapper);
    }

    //查询用户数量
    @Override
    public Integer queryCount() {
        LambdaQueryWrapper<User> queryWrapper = new QueryWrapper<User>().lambda();

        return userMapper.selectCount(queryWrapper);
    }
}
