package com.itheima.test;

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.server.AppServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FastDFSTest {

    /**
     * 测试上传文件
     */
    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer webServer;

    @Test
    public void testUpLode() throws IOException {
        //指定文件
        File file = new File("C:\\Users\\29260\\Desktop\\face.jpg");

        //上传文件
        StorePath path = client.uploadFile(Files.newInputStream(file.toPath()), file.length(), "jpg", null);

        //获取文件路径
        String fullPath = path.getFullPath();
        System.out.println(fullPath);
        String url = webServer.getWebServerUrl() + fullPath;
        System.out.println(url);
    }

}
