package com.itheima.test;

import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import com.tanhua.server.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class HuanXingTest {

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @DubboReference
    private UserApi userApi;

    @Test
    public void test() {
        huanXinTemplate.createUser("user03", "123456");
    }

    @Test
    public void register() {
        for (int i = 1; i < 106; i++) {
            User user = userApi.findById(Long.valueOf(i));
            if(user != null) {
                Boolean create = huanXinTemplate.createUser("hx" + user.getId(), Constants.INIT_PASSWORD);
                if (create){
                    user.setHxUser("hx" + user.getId());
                    user.setHxPassword(Constants.INIT_PASSWORD);
                    userApi.update(user);
                }
            }
        }
    }

    @Test
    public void test2() {
        User user = userApi.findById(106L);
        Boolean create = huanXinTemplate.createUser("hx" + user.getId(), Constants.INIT_PASSWORD);

        if (create){
            user.setHxUser("hx" + user.getId());
            user.setHxPassword(Constants.INIT_PASSWORD);
            userApi.update(user);
        }
    }

}
