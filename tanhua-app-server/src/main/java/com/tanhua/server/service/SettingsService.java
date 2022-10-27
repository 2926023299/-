package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.Settings;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SettingsService {

    @DubboReference
    private QuestionApi questionApi;

    @DubboReference
    private SettingsApi settingsApi;

    @DubboReference
    private BlackListApi blacklistApi;

    /**
     * 通用设置
     *
     * @param
     * @return
     */
    public SettingsVo settings() {
        SettingsVo vo = new SettingsVo();

        //获取用户id
        Long userId = UserHolder.getUserId();
        vo.setId(userId);

        //获取用户的手机号码
        vo.setPhone(UserHolder.getUserPhone());

        //获取用户的陌生人问题
        Question question = questionApi.findQuestionByUserId(userId);
        if (question != null) {
            if (question.getTxt() != null) {
                vo.setStrangerQuestion(question.getTxt());
            } else {
                vo.setStrangerQuestion("where are you from?");
            }
        }

        //获取用户的app通知开关数据
        Settings settings = settingsApi.findByUserId(userId);
        if (settings != null) {
            vo.setLikeNotification(settings.getLikeNotification());
            vo.setPinglunNotification(settings.getPinglunNotification());
            vo.setGonggaoNotification(settings.getGonggaoNotification());
        }

        return vo;
    }

    public void saveQuestions(String context) {
        //获取用户id
        Long userId = UserHolder.getUserId();

        Question questionExists = questionApi.findQuestionByUserId(userId);
        if (questionExists != null) {
            questionExists.setTxt(context);
            questionApi.updateQuestion(questionExists);
        } else {
            //保存用户的陌生人问题
            Question question = new Question();
            question.setUserId(userId);
            question.setTxt(context);
            questionApi.saveQuestion(question);
        }

    }

    /**
     * 保存通知设置
     *
     * @param map
     */
    public void saveNotifications(Map map) {
        Long userId = UserHolder.getUserId();

        Settings byUserId = settingsApi.findByUserId(userId);
        if (byUserId == null) {
            Settings settings = new Settings();
            settings.setUserId(userId);
            settings.setPinglunNotification((Boolean) map.get("likeNotification"));
            settings.setGonggaoNotification((Boolean) map.get("pinglunNotification"));
            settings.setLikeNotification((Boolean) map.get("gonggaoNotification"));
            settingsApi.save(settings);
        } else {
            byUserId.setPinglunNotification((Boolean) map.get("likeNotification"));
            byUserId.setGonggaoNotification((Boolean) map.get("pinglunNotification"));
            byUserId.setLikeNotification((Boolean) map.get("gonggaoNotification"));
            settingsApi.update(byUserId);
        }
    }

    /**
     * 分页查询黑名单列表
     * @param page
     * @param size
     * @return
     */
    public PageResult blacklist(int page, int size) {
        Long userId = UserHolder.getUserId();

        IPage<UserInfo> iPage = blacklistApi.findByUserId(userId, page, size);

        return new PageResult(page,size, (int) iPage.getTotal(), iPage.getRecords());
    }

    //取消黑名单
    public void deleteBlackList(Long blackUserId) {
        Long userId = UserHolder.getUserId();

        blacklistApi.delete(userId, blackUserId);
    }
}
