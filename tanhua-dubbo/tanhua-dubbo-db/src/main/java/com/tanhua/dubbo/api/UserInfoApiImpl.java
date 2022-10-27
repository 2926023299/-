package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tanhua.dubbo.mappers.UserInfoMapper;
import com.tanhua.model.domain.UserInfo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void save(UserInfo userinfo) {
        userInfoMapper.insert(userinfo);
    }

    @Override
    public void update(UserInfo userinfo) {
        userInfoMapper.updateById(userinfo);
    }

    @Override
    public UserInfo findById(Long userID) {
        return userInfoMapper.selectById(userID);
    }

    @Override
    public Map<Long, UserInfo> findByIds(List<Long> userIds, UserInfo info) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();

        if (userIds != null)
            wrapper.in(UserInfo::getId, userIds);

        if (info != null) {
            wrapper.lt(info.getAge() != null, UserInfo::getAge, info.getAge());
            wrapper.eq(info.getGender() != null, UserInfo::getGender, info.getGender());
            wrapper.like(info.getNickname() != null, UserInfo::getNickname, info.getNickname());
        }
        List<UserInfo> list = userInfoMapper.selectList(wrapper);

        return CollUtil.fieldValueMap(list, "id");
    }

    /**
     * 分页查询用户
     * @param page
     * @param pagesize
     * @return
     */
    @Override
    public IPage<UserInfo> findAll(Integer page, Integer pagesize) {
        IPage<UserInfo> iPage = new Page<>(page, pagesize);

        return userInfoMapper.selectPage(iPage, null);
    }
}
