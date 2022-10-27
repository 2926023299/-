package com.tanhua.dubbo.api;

import com.tanhua.model.domain.Question;

public interface QuestionApi {
    Question findQuestionByUserId(Long userId);

    void saveQuestion(Question question);

    void updateQuestion(Question question);
}
