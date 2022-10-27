package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.server.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Autowired
    private CommentService commentService;

    /**
     * 发布评论
     */
    @PostMapping
    public ResponseEntity saveComments(@RequestBody Map map) {
        String movementId = (String) map.get("movementId");
        String comment = (String) map.get("comment");
        commentService.saveComment(movementId, comment);
        return ResponseEntity.ok(null);
    }

    //分页查询评论列表
    @GetMapping
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize,
                                       String movementId) {
        PageResult pr = commentService.findFriendMovements(page, pagesize, movementId);

        return ResponseEntity.ok(pr);
    }

    //评论点赞
    @GetMapping("/{id}/like")
    public ResponseEntity CommentLike(@PathVariable("id") String commentId){
        Integer likeCount = commentService.commentLike(commentId);

        return ResponseEntity.ok(likeCount);
    }

    //评论取消点赞
    @GetMapping("/{id}/dislike")
    public ResponseEntity CommentNotLike(@PathVariable("id") String commentId){
        Integer likeCount = commentService.commentNotLike(commentId);

        return ResponseEntity.ok(likeCount);
    }
}
