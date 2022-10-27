package com.tanhua.admin.listener;

import com.tanhua.autoconfig.template.AliyunGreenTemplate;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.model.mongo.Movement;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuditListener {

    @DubboReference
    private MovementApi movementApi;

    @Autowired
    private AliyunGreenTemplate aliyunGreenTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = "tanhua.audit.queue",
                    durable = "true"
            ),
            exchange = @Exchange(
                    value = "tanhua.audit.exchange",
                    type = ExchangeTypes.TOPIC),
            key = {"audit.movement"})
    )
    public void listenCreate(String movementId) throws Exception {
        try {
            //1、根据动态id查询动态
            Movement movement = movementApi.findById(movementId);
            //对于RocketMQ消息有可能出现重复，解决方法判断 (幂等性)
            int state = 0;
            if (movement != null && movement.getState() == 0) {
                Map<String, String> textScan = aliyunGreenTemplate.greenTextScan(movement.getTextContent());
                Map imageScan = aliyunGreenTemplate.imageScan(movement.getMedias());
                if (textScan != null && imageScan != null) {
                    String textSuggestion = textScan.get("suggestion");
                    String imageSuggestion = (String) imageScan.get("suggestion");
                    if ("block".equals(textSuggestion) || "block".equals(imageSuggestion)) {
                        state = 2;
                    } else if ("pass".equals(textSuggestion) || "pass".equals(imageSuggestion)) {
                        state = 1;
                    }
                }
            }
            movementApi.updateState(movementId, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
