package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.server.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/smallVideos")
public class SmallVideosController {

    @Autowired
    private VideoService videoService;


    /**
     * 上传小视频
     *
     * @param videoThumbnail 小视频封面
     * @param videoFile      视频文件
     */
    @PostMapping
    public ResponseEntity saveSmallVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        videoService.saveSmallVideos(videoThumbnail, videoFile);

        return ResponseEntity.ok(null);
    }

    /**
     * 视频列表
     */
    @GetMapping
    public ResponseEntity queryVideoList(@RequestParam(defaultValue = "1")  Integer page,
                                         @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = videoService.queryVideoList(page, pagesize);
        return ResponseEntity.ok(result);
    }
}
