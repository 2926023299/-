package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserService userService;

    /**
     * 查询用户资料
     */
    @GetMapping()
    public ResponseEntity users(@RequestHeader("Authorization") String token, Long userID) throws InvocationTargetException, IllegalAccessException {

        //获取用户id
        if (userID == null) {
            userID = UserHolder.getUserId();
        }

        UserInfoVo vo = userInfoService.findById(userID);
        return ResponseEntity.ok(vo);
    }

    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfo userInfo, @RequestHeader("Authorization") String token) {

        //获取用户id
        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);

        return ResponseEntity.ok().build();
    }

    /**
     * 更新头像
     */
    @PostMapping("/header")
    public ResponseEntity head(MultipartFile headPhoto, @RequestHeader("Authorization") String token) throws IOException {

        //向userInfo中设置用户id
        //保存头像
        userInfoService.updateHead(headPhoto, UserHolder.getUserId());

        return ResponseEntity.ok().build();
    }

    /**
     * 修改手机号
     */
    @PostMapping("phone/sendVerificationCode")
    public ResponseEntity updatePhoneSentCode() {

        userService.sendMsg(UserHolder.getUserPhone());

        return ResponseEntity.ok(null);
    }

    /**
     * 检查更改手机的验证码
     *
     * @param
     * @return
     */
    @PostMapping("phone/checkVerificationCode")
    public ResponseEntity updatePhoneCheckVerificationCode(@RequestBody Map map) {

        boolean flag = userService.checkVerificationCode((String) map.get("verificationCode"));
        System.out.println("更改手机号验证：" + flag);
        Map<String, Boolean> map1 = new HashMap<>();
        map1.put("verification",flag);
        return ResponseEntity.ok(map1);
    }

    @PostMapping("/phone")
    public ResponseEntity updatePhone(@RequestBody Map map) {
        userService.updatePhone((String) map.get("phone"));
        return ResponseEntity.ok(null);
    }

    @GetMapping("/counts")
    public ResponseEntity counts() {
        Map<String, Integer> map = userInfoService.counts();

        return ResponseEntity.ok(map);
    }
}
