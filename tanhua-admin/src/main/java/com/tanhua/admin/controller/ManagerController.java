package com.tanhua.admin.controller;

import com.tanhua.admin.service.ManagerService;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/manage")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    /**
     * 查询所有用户
     *
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = managerService.findAllUsers(page, pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据id查询用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity findUserById(@PathVariable Long userId) {
        UserInfo info = managerService.findUserById(userId);
        return ResponseEntity.ok(info);
    }

    /**
     * 查询指定用户发布的所有视频列表
     */
    @GetMapping("/videos")
    public ResponseEntity videos(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                 Long uid) {
        PageResult result = managerService.findAllVideos(page, pagesize, uid);
        return ResponseEntity.ok(result);
    }

    /**
     * 动态查询
     */
    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid, Integer state) {
        PageResult result = managerService.findMovement(page, pagesize, uid, state);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据动态id查询动态详情
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity findMovementById(@PathVariable("id") String id) {

        MovementsVo vo = managerService.findMovementById(id);

        return ResponseEntity.ok(vo);
    }

    /**
     * 查询评论列表
     * @param page
     * @param pagesize
     * @param messageID
     * @return
     */
    @GetMapping("/messages/comments")
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize,
                                       String messageID) {
        PageResult result = managerService.findComments(page, pagesize, messageID);

        result.getItems().forEach(item -> {
            System.out.println("评论:" + item);
        });

        return ResponseEntity.ok(result);
    }

    //用户冻结
    @PostMapping("/users/freeze")
    public ResponseEntity freeze(@RequestBody Map params) {
        Map map =  managerService.userFreeze(params);
        return ResponseEntity.ok(map);
    }

    //用户解冻
    @PostMapping("/users/unfreeze")
    public ResponseEntity unfreeze(@RequestBody  Map params) {
        Map map =  managerService.userUnfreeze(params);
        return ResponseEntity.ok(map);
    }
}
