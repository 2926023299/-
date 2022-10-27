package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    //根据环信id查询用户详情
    @GetMapping("/userinfo")
    public ResponseEntity userinfo(String huanxinId) {
        UserInfoVo vo = messageService.findUserInfo(huanxinId);
        return ResponseEntity.ok(vo);
    }

    //添加好友
    @PostMapping("/contacts")
    public ResponseEntity contacts(@RequestBody Map map) {
        Long friendId = Long.valueOf(map.get("userId").toString());
        messageService.contacts(friendId);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/contacts")
    public ResponseEntity contacts(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize, String keyword) {
        PageResult pageResult = messageService.findContacts(page, pagesize, keyword);
        return ResponseEntity.ok(pageResult);
    }

    //查询点赞列表
    @GetMapping("/likes")
    public ResponseEntity likes(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pageResult = messageService.findLikes(page, pagesize);

        return ResponseEntity.ok(pageResult);
    }

    //查询评论列表
    @GetMapping("/comments")
    public ResponseEntity comments(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pageResult = messageService.findComments(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    //查询喜欢列表
    @GetMapping("/loves")
    public ResponseEntity loves(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pageResult = messageService.findLoves(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }

    //查询公告列表
    @GetMapping("/announcements")
    public ResponseEntity announcements(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int pagesize) {
        PageResult pageResult = messageService.findAnnouncements(page, pagesize);
        return ResponseEntity.ok(pageResult);
    }
}
