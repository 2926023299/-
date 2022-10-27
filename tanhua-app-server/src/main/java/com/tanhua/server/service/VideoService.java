package com.tanhua.server.service;

import cn.hutool.core.util.PageUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.domain.Video;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VideoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VideoService {

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer WebServer;

    @Autowired
    private OssTemplate ossTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    /**
     * 保存小视频
     *
     * @param videoThumbnail 小视频封面
     * @param videoFile      小视频文件
     */
    public void saveSmallVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {
        // TODO 保存小视频
        if (videoThumbnail.isEmpty() || videoFile.isEmpty()) {
            throw new BusinessException(ErrorResult.error());
        }

        //将视频上传到FastDFS,获取访问url
        String originalFilename = videoFile.getOriginalFilename();
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), suffix, null);
        String videoUrl = WebServer.getWebServerUrl() + storePath.getFullPath();

        //将封面上传到OSS,获取访问url
        String imageUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());

        //构建Video对象,保存到MongoDB
        Video video = new Video();
        video.setUserId(UserHolder.getUserId());
        video.setPicUrl(imageUrl);
        video.setVideoUrl(videoUrl);
        video.setText("这是一段测试视频");
        String videoId = videoApi.save(video);

        if (videoId == null) {
            throw new BusinessException(ErrorResult.error());
        }
    }

    /**
     * 查询视频列表
     *
     * @param page
     * @param pagesize
     * @return
     */
    @Cacheable(value = "videos", key = "T(com.tanhua.server.interceptor.UserHolder).getUserId() + '_' + #page+'_'+#pagesize")
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        // TODO 查询小视频列表

        //查询redis数据
        String redisKey = Constants.VIDEOS_RECOMMEND + UserHolder.getUserId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);

        //判断redis数据是否存在
        List<Video> list = new ArrayList<>();
        int redisPages = 0;
        if (!StringUtils.isEmpty(redisValue)) {
            String[] videoIds = redisValue.split(",");

            //判断redis数据是否大于10条
            if (videoIds.length > pagesize) {
                List<Long> vids = Arrays.stream(videoIds)
                        .skip((long) (page - 1) * pagesize)
                        .limit(pagesize)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());

                list = videoApi.findVideoByVids(vids);
            }

            redisPages = PageUtil.totalPage(videoIds.length, pagesize);
        }

        //如果redis数据不存在,或者redis数据不足10条,则从MongoDB查询数据
        if (list.isEmpty()) {
            list = videoApi.queryVideoList(page - redisPages, pagesize);
        }

        //提取视频id中的用户id
        List<Long> userIds = list.stream().map(Video::getUserId).collect(Collectors.toList());

        //查询用户详情
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);

        //封装vo对象
        List<VideoVo> vos = new ArrayList<>();
        for (Video video : list) {
            UserInfo userInfo = map.get(video.getUserId());

            if (userInfo == null) {
                continue;
            }

            VideoVo vo = VideoVo.init(userInfo, video);

            vos.add(vo);
        }

        return new PageResult(page, pagesize, 0, vos);
    }
}
