package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.dubbo.mappers.SettingsMapper;
import com.tanhua.model.domain.Settings;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class SettingsApiImpl implements SettingsApi {

    @Autowired
    private SettingsMapper settingMapper;

    @Override
    public Settings findByUserId(Long userId) {
        LambdaQueryWrapper<Settings> queryWrapper = new LambdaQueryWrapper<>();
        return settingMapper.selectOne(queryWrapper.eq(Settings::getUserId, userId));
    }

    @Override
    public void save(Settings settings) {
        settingMapper.insert(settings);
    }

    @Override
    public void update(Settings settings) {
        settingMapper.updateById(settings);
    }
}
