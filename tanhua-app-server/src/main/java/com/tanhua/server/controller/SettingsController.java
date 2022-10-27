package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * 通知设置
     *
     * @param
     * @return
     */
    @GetMapping("/settings")
    public ResponseEntity settings() {
        SettingsVo vo = settingsService.settings();

        return ResponseEntity.ok(vo);
    }

    /**
     * 设置陌生人问题
     */
    @PostMapping("/questions")
    public ResponseEntity questions(@RequestBody Map map) {
        String context = (String) map.get("content");

        settingsService.saveQuestions(context);

        return ResponseEntity.ok(null);
    }

    /**
     * 设置通知开关
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity notifications(@RequestBody Map map) {

        settingsService.saveNotifications(map);

        return ResponseEntity.ok(null);
    }

    /**
     * 黑名单列表查询
     */
    @GetMapping("/blacklist")
    public ResponseEntity listBlacklist(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {

        PageResult pr = settingsService.blacklist(page, size);

        return ResponseEntity.ok(pr);
    }

    @DeleteMapping("/blacklist/{uid}")
    public ResponseEntity deleteBlackList(@PathVariable("uid") Long blackUserId) {
        settingsService.deleteBlackList(blackUserId);
        return ResponseEntity.ok(null);
    }
}
