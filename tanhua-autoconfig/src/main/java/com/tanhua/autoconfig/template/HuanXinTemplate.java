package com.tanhua.autoconfig.template;

import cn.hutool.core.collection.CollUtil;
import com.easemob.im.server.EMProperties;
import com.easemob.im.server.EMService;
import com.easemob.im.server.model.EMTextMessage;
import com.tanhua.autoconfig.properties.HuanXinProperties;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Set;

@Slf4j
public class HuanXinTemplate {
    private EMService service;

    public HuanXinTemplate(HuanXinProperties properties) {
        EMProperties emProperties = EMProperties.builder()
                .setAppkey(properties.getAppkey())
                .setClientId(properties.getClientId())
                .setClientSecret(properties.getClientSecret())
                .build();

        service = new EMService(emProperties);
    }

    //创建环信用户
    public Boolean createUser(String userName, String password) {
        try {
            service.user().create(userName, password).block(Duration.ofSeconds(5L));

            return true;
        } catch (Exception e) {
            log.error("创建环信用户失败，用户名：{}，密码：{}，错误{}", userName, password, e);
        }
        return false;
    }

    //添加联系人
    public Boolean addContact(String userName1, String userName2) {
        try {
            service.contact().add(userName1, userName2).block(Duration.ofSeconds(5L));

            return true;
        } catch (Exception e) {
            log.error("添加联系人失败，用户名1：{}，用户名2：{}，错误{}", userName1, userName2, e.getMessage());
        }
        return false;
    }

    //删除联系人
    public Boolean deleteContact(String userName1, String userName2) {
        try {
            service.contact().remove(userName1, userName2).block(Duration.ofSeconds(5L));

            return true;
        } catch (Exception e) {
            log.error("删除联系人失败，用户名1：{}，用户名2：{}，错误{}", userName1, userName2, e.getMessage());
        }
        return false;
    }

    //发送消息
    public Boolean sendMsg(String userName, String msg) {
        try {
            Set<String> set = CollUtil.newHashSet(userName);

            //文本消息
            EMTextMessage message = new EMTextMessage().text(msg);

            service.message().send("admin", "users", set, message, null).block();

            return true;
        } catch (Exception e) {
            log.error("发送消息失败，msg：{}，错误{}", msg, e);
        }
        return false;
    }
}
