package com.tanhua.dubbo.api;

import com.tanhua.model.domain.Video;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface VideoApi {

    //保存小视频
    String save(Video video);

    //根据vid查询数据列表
    List<Video> findVideoByVids(List<Long> vids);

    //根据页码查询小视频列表
    List<Video> queryVideoList(int i, Integer pagesize);

    //根据用户id查询小视频列表
    PageResult findByUserId(Integer page, Integer pagesize, Long uid);
}
