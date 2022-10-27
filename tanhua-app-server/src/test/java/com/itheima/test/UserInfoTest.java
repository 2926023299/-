package com.itheima.test;

import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.server.AppServerApplication;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class UserInfoTest {
    @DubboReference
    private UserInfoApi userInfoApi;

    @Test
    public void test(){
        List ids = new ArrayList<>();
        ids.add(1);
        ids.add(2);
        ids.add(3);
        ids.add(5);
        ids.add(4);

        Map map = userInfoApi.findByIds(ids, null);
        System.out.println(map.toString());
    }
}
