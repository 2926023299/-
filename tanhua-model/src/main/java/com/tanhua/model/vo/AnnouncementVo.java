package com.tanhua.model.vo;

import com.tanhua.model.domain.Announcement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.text.SimpleDateFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementVo implements Serializable {

    private Long id;
    private String title;
    private String description;

    private String createDate; //发布时间

    public static AnnouncementVo init(Announcement item) {
        AnnouncementVo vo = new AnnouncementVo();
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setDescription(item.getDescription());
        vo.setCreateDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getCreated()));
        return vo;
    }
}

