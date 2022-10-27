package com.tanhua.server.controller;

import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.service.CommentService;
import com.tanhua.server.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class MovementController {

    @Autowired
    private MovementService movementService;

    @Autowired
    private CommentService commentService;

    /**
     * 发布动态
     */
    @PostMapping()
    public ResponseEntity movements(Movement movement, MultipartFile[] imageContent) throws IOException {

        movementService.publishMovements(movement, imageContent);

        return ResponseEntity.ok(null);
    }

    //查询我的动态
    @GetMapping("/all")
    public ResponseEntity findByUserId(Long userId,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize) {

        PageResult result = movementService.findByUserId(userId, page, pagesize);

        return ResponseEntity.ok(result);
    }

    /**
     * 查询好友动态
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping()
    public ResponseEntity movements(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize) {

        PageResult result = movementService.findFriendsMovements(page, pagesize);

        return ResponseEntity.ok(result);
    }

    /**
     * 查询推荐动态
     * @param page
     * @param pagesize
     * @return
     */
    @GetMapping("/recommend")
    public ResponseEntity recommendMovements(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer pagesize) {

        PageResult result = movementService.findRecommendMovements(page, pagesize);

        System.out.println("推荐动态数据 = " + result.toString());
        return ResponseEntity.ok(result);
    }

    /**
     * 查询指定好友动态
     * @param movementId
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity findById(@PathVariable("id") String movementId) {
        MovementsVo vo = movementService.findById(movementId);

        return ResponseEntity.ok(vo);
    }

    //点赞动态
    @GetMapping("/{id}/like")
    public ResponseEntity movementLike(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.likeComment(movementId);

        return ResponseEntity.ok(likeCount);
    }

    //取消点赞
    @GetMapping("/{id}/dislike")
    public ResponseEntity movementNotLike(@PathVariable("id") String movementId) {
        Integer likeCount = commentService.notLikeComment(movementId);

        return ResponseEntity.ok(likeCount);
    }

    //喜欢功能
    @GetMapping("/{id}/love")
    public ResponseEntity movementLove(@PathVariable("id") String movementId) {
        Integer loveCount = commentService.loveComment(movementId);

        return ResponseEntity.ok(loveCount);
    }

    //取消喜欢
    @GetMapping("/{id}/unlove")
    public ResponseEntity movementNotLove(@PathVariable("id") String movementId) {
        Integer loveCount = commentService.notLoveComment(movementId);

        return ResponseEntity.ok(loveCount);
    }

    /**
     * 谁看过我
     */
    @GetMapping("visitors")
    public ResponseEntity queryVisitorsList(){
        List<VisitorsVo> list = movementService.queryVisitorsList();
        return ResponseEntity.ok(list);
    }
}
