package com.itheima.test;

import com.baidu.aip.face.AipFace;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.server.AppServerApplication;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FaceTest {

    @Autowired
    public AipFaceTemplate aipFaceTemplate;

    @Test
    public void test() {
        String url = "https://tanhua-rss.oss-cn-hangzhou.aliyuncs.com/2022-08-31/b28548fb-2dae-475b-9a6a-a71192c4fba3.jpg";
        boolean detect = aipFaceTemplate.detect(url);

        if (detect) {
            System.out.println("检测到人脸!");
        } else {
            System.out.println("未检测到人脸!");
        }
    }

    //设置APPID/AK/SK
    public static final String APP_ID = "27232121";
    public static final String API_KEY = "9WIC2VS7POAV75wDc5Opt3Kx";
    public static final String SECRET_KEY = "h6sWjKQC8sS4c0trTuRxBqtefrUChyNU";

    public static void main(String[] args) {
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);

        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
           /* client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
            client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理*/

        // 调用接口
        String path = "https://tanhua-rss.oss-cn-hangzhou.aliyuncs.com/2022-08-31/b28548fb-2dae-475b-9a6a-a71192c4fba3.jpg";
        String imageType = "URL";

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(path, imageType, options);
        System.out.println(res.toString(2));

        Object error_code = res.get("error_code");
        if ((int) error_code == 0) {
            System.out.println("检测成功");
        }

    }
}
