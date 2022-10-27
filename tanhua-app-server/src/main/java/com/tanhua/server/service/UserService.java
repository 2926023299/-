package com.tanhua.server.service;

import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.autoconfig.template.SmsTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private SmsTemplate template;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    @DubboReference
    private UserApi userApi;

    @Autowired
    private UserFreezeService userFreezeService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private MqMessageService mqMessageService;

    /**
     * 发送短信验证码
     *
     * @param phone
     */
    public void sendMsg(String phone) {
        //判断用户是否被冻结
        User user = userApi.findByMobile(phone);
        if (user != null) {
            userFreezeService.isFreeze("1", user.getId());
        }

        //1、随机生成6位数字
        String code = RandomStringUtils.randomNumeric(6);
        //2、调用template对象，发送手机短信
        /*template.sendSms(phone, code);*/
        code = "123456";
        //3、将验证码存入到redis
        redisTemplate.opsForValue().set("CHECK_CODE_" + phone, code, Duration.ofMinutes(5));
    }

    /**
     * 验证登录
     *
     * @param phone
     * @param code
     */
    public Map loginVerification(String phone, String code) {
        //1、从redis中获取下发的验证码
        String redisCode = redisTemplate.opsForValue().get("CHECK_CODE_" + phone);

        //2、对验证码进行校验（验证码是否存在，是否和输入的验证码一致）
        if (StringUtils.isEmpty(redisCode) || !redisCode.equals(code)) {
            //验证码无效
            System.out.println(code + ":" + redisCode);
            throw new BusinessException(ErrorResult.loginError());
        } else {
            System.out.println(code + ":" + redisCode);
            System.out.println("验证码正确");
        }

        //3、删除redis中的验证码
        redisTemplate.delete("CHECK_CODE_" + phone);

        //4、通过手机号码查询用户
        User user = userApi.findByMobile(phone);
        boolean isNew = false;

        String type = "0101";   //默认为登录
        //5、如果用户不存在，创建用户保存到数据库中
        if (user == null) {
            type = "0102";  //注册

            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));
            Long userId = userApi.save(user);
            user.setId(userId);
            isNew = true;

            //注册环信账号
            Boolean create = huanXinTemplate.createUser("hx" + userId, Constants.INIT_PASSWORD);
            if (create) {
                user.setHxUser("hx" + userId);
                user.setHxPassword(Constants.INIT_PASSWORD);
                userApi.update(user);
            }
        }

        //发送消息
        mqMessageService.sendLogService(user.getId(), type, "user", null);

        //6、通过JWT生成token(存入id和手机号码)
        Map tokenMap = new HashMap();
        tokenMap.put("id", user.getId());
        tokenMap.put("mobile", phone);
        String token = JwtUtils.getToken(tokenMap);

        //7、构造返回值
        Map retMap = new HashMap();
        retMap.put("token", token);
        retMap.put("isNew", isNew);

        return retMap;
    }

    public boolean checkVerificationCode(String code) {
        String trueCode = redisTemplate.opsForValue().get("CHECK_CODE_" + UserHolder.getUserPhone());

        //删除redis中的验证码
        redisTemplate.delete("CHECK_CODE_" + UserHolder.getUserPhone());
        System.out.println("验证码: " + code);
        System.out.println("验证码: " + trueCode);
        return code.equals(trueCode);
    }

    public void updatePhone(String phone) {

        Long userId = UserHolder.getUserId();
        userApi.updatePhone(userId, phone);
    }
}
