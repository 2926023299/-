package com.tanhua.dubbo.api;

import com.tanhua.model.domain.Announcement;

import java.util.List;

public interface AnnouncementApi {

    //查询公告列表
    List<Announcement> findAnnouncements(int page, int pagesize);
}
