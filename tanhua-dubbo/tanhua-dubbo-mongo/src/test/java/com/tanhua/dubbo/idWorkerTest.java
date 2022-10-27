package com.tanhua.dubbo;

import com.tanhua.dubbo.utils.IdWorker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DubboMongoApplication.class)
public class idWorkerTest {

    @Autowired
    private IdWorker idWorker;

    @Test
    public void test() {
        Long nextId = idWorker.getNextId("test");
        System.out.println(nextId);
    }
}
