package com.tanhua.dubbo.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tanhua.dubbo.mappers.QuestionMapper;
import com.tanhua.model.domain.Question;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService
public class QuestionApiImpl implements QuestionApi {
    @Autowired
    private QuestionMapper questionMapper;


    @Override
    public Question findQuestionByUserId(Long userId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        return questionMapper.selectOne(queryWrapper.eq(Question::getUserId, userId));
    }

    @Override
    public void saveQuestion(Question question) {
        questionMapper.insert(question);
    }

    @Override
    public void updateQuestion(Question question) {
        questionMapper.updateById(question);
    }
}
