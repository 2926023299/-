package com.itheima.test;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.AppServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class OssTest {

    @Autowired
    private OssTemplate ossTemplate;

    @Test
    public void testOssTemplate() {
        // 配置图片上传的路径
        String path = "C:\\Users\\29260\\Desktop\\1.jpg";
        String path2 = "C:\\Users\\29260\\Desktop\\face.jpg";

        //构造FileInputStream
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path2));
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在");
        }

        String url = ossTemplate.upload(path2, inputStream);

        System.out.println(url);
    }

    @Test
    public void test() {
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5t7tQr2SYHDcz6mm8pUs";
        String accessKeySecret = "Ll0oPTBM3udh1LYnQAaYERkYlTKWFD";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "tanhua-rss";


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 填写字符串。
            String content = "Hello OSS";

            // 配置图片上传的路径
            String path = "C:\\Users\\29260\\Desktop\\1.jpg";

            //构造FileInputStream
            FileInputStream inputStream = new FileInputStream(new File(path));

            // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
            String objectName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + UUID.randomUUID().toString()
                    + path.substring(path.lastIndexOf("."));

            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName,inputStream);

            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);

            // 上传字符串。
            ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Caught an FileNotFoundException, which means the client "
                    + "could not find the file specified.");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
