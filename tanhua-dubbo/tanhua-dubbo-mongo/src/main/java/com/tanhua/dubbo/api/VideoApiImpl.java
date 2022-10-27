package com.tanhua.dubbo.api;

import com.tanhua.dubbo.utils.IdWorker;
import com.tanhua.model.domain.Video;
import com.tanhua.model.vo.PageResult;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class VideoApiImpl implements VideoApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdWorker idWorker;

    /**
     * 保存小视频
     *
     * @param video 小视频对象
     * @return 小视频id
     */
    @Override
    public String save(Video video) {
        //设置属性
        video.setVid(idWorker.getNextId("video"));
        video.setCreated(System.currentTimeMillis());

        //保存
        mongoTemplate.save(video);

        return video.getId().toHexString();
    }

    @Override
    public List<Video> findVideoByVids(List<Long> vids) {

        Query query = Query.query(Criteria.where("vid").in(vids));

        return mongoTemplate.find(query, Video.class);
    }

    @Override
    public List<Video> queryVideoList(int page, Integer pagesize) {
        Query skip = new Query().limit(pagesize)
                .skip((long) (page - 1) * pagesize)
                .with(Sort.by(Sort.Order.desc("created")));

        return mongoTemplate.find(skip, Video.class);
    }

    /**
     * 根据用户id查询小视频列表
     * @param page
     * @param pagesize
     * @param uid
     * @return
     */
    @Override
    public PageResult findByUserId(Integer page, Integer pagesize, Long uid) {
        Query query = Query.query(Criteria.where("userId").is(uid));

        long count = mongoTemplate.count(query, Video.class);

        query.limit(pagesize)
                .skip((long) (page - 1) * pagesize)
                .with(Sort.by(Sort.Order.desc("created")));
        List<Video> videos = mongoTemplate.find(query, Video.class);

        return new PageResult(page, pagesize, (int) count, videos);
    }
}
