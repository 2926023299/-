package com.tanhua.admin.listener;

import com.alibaba.fastjson.JSON;
import com.tanhua.admin.mapper.LogMapper;
import com.tanhua.model.domain.Log;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class LogListener {

    @Autowired
    private LogMapper logMapper;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(
                            value = "tanhua.log.queue",
                            durable = "true"
                    ),
                    exchange = @Exchange(
                            value = "tanhua.log.exchange",
                            type = ExchangeTypes.TOPIC),
                    key = {"log.*"})
    )
    public void listenCreate(String message) {
        try {
            Map map = JSON.parseObject(message, Map.class);

            Long userId = Long.valueOf(map.get("userId").toString());
            String type = (String) map.get("type");
            String logTime = (String) map.get("logTime");

            //构造log对象
            Log log = new Log(userId, logTime, type);
            System.out.println("log{" + log + "}");

            logMapper.insert(log);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}