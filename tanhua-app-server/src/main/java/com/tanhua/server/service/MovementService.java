package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovementService {

    @Autowired
    private OssTemplate ossTemplate;

    @DubboReference
    private MovementApi movementApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private VisitorsApi visitorsApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MqMessageService mqMessageService;

    /**
     * 发布动态
     *
     * @param movement
     * @param imageContent
     */
    public void publishMovements(Movement movement, MultipartFile[] imageContent) throws IOException {
        //判断发布动态的内容是否存在
        if (StringUtils.isEmpty(movement.getTextContent())) {
            throw new BusinessException(ErrorResult.contentError());
        }

        //获取当前登录的用户id
        Long userId = UserHolder.getUserId();

        //将文件内容上传到阿里云OSS， 获取请求地址
        List<String> medias = new ArrayList<String>();
        for (MultipartFile multipartFile : imageContent) {
            String upload = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
            medias.add(upload);
        }

        //将数据封装到Movement对象
        movement.setUserId(userId);
        movement.setMedias(medias);

        //调用api进行动态发布
        String movementId = movementApi.publish(movement);

        mqMessageService.sendAudiService(movementId);
    }

    /**
     * 查询个人动态
     *
     * @param userId
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        //根据用户id调用API查询个人动态内容（PageResult --Movement）
        PageResult pr = movementApi.findByUserId(userId, page, pagesize);

        //获取PageResult中的item列表对象
        List<Movement> items = (List<Movement>) pr.getItems();


        return this.getPageResult(page, pagesize, items);
    }

    /**
     * 查询好友动态
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findFriendsMovements(Integer page, Integer pagesize) {
        Long userId = UserHolder.getUserId();

        List<Movement> list = movementApi.findFriendsMovements(page, pagesize, userId);
        return getPageResult(page, pagesize, list);
    }

    //公共方法
    private PageResult getPageResult(Integer page, Integer pagesize, List<Movement> list) {
        if (list.isEmpty()) {
            return new PageResult();
        }

        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> userInfoMap = userInfoApi.findByIds(userIds, null);

        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : list) {
            UserInfo userInfo = userInfoMap.get(movement.getUserId());
            if (userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, movement);

                //点赞状态, 判断redis的hashKey是否存在
                //拼接redis的key，将用户的点赞状态存入redis
                String key = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toHexString();
                String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();

                if (redisTemplate.opsForHash().hasKey(key, hashKey)) {
                    vo.setHasLiked(1);
                }

                //喜欢状态, 判断redis的hashKey是否存在
                //拼接redis的key，将用户的喜欢状态存入redis
                String key1 = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toHexString();
                String hashKey1 = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();

                if (redisTemplate.opsForHash().hasKey(key1, hashKey1)) {
                    vo.setHasLoved(1);
                }

                vos.add(vo);
            }
        }

        return new PageResult(page, pagesize, 0, vos);
    }

    /**
     * 查询推荐动态
     *
     * @param page
     * @param pagesize
     * @return
     */
    public PageResult findRecommendMovements(Integer page, Integer pagesize) {
        //从redis中获取推荐数据
        String redisKey = Constants.MOVEMENTS_RECOMMEND + UserHolder.getUserId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);

        //判断推荐数据是否存在
        List<Movement> list = Collections.EMPTY_LIST;
        if (StringUtils.isEmpty(redisValue)) {
            //不存在则当调用API随机构造10条动态数据
            list = movementApi.randomMovements(pagesize);
        } else {
            //处理pid数据
            String[] values = redisValue.split(",");
            if ((page - 1) * pagesize < values.length) {
                List<Long> pids = Arrays.stream(values).skip((long) (page - 1) * pagesize).limit(pagesize)
                        .map(Long::valueOf)
                        .collect(Collectors.toList());

                list = movementApi.findMovementsByPids(pids);
            }
        }

        return new PageResult(page, pagesize, 0, list);
    }

    public MovementsVo findById(String movementId) {
        //发送消息
        mqMessageService.sendLogService(UserHolder.getUserId(), "0202", "movement", movementId);

        //调用api查询动态详情
        Movement movement = movementApi.findById(movementId);

        //幻化vo对象
        if (movement != null) {
            UserInfo userInfo = userInfoApi.findById(movement.getUserId());
            if (userInfo != null) {
                return MovementsVo.init(userInfo, movement);
            }
        }
        return null;
    }

    /**
     * 首页-访客列表
     *
     * @return
     */
    public List<VisitorsVo> queryVisitorsList() {
        //查询访问时间
        String redisKey = Constants.VISITORS_USER;
        String value = (String) redisTemplate.opsForHash().get(redisKey, UserHolder.getUserId().toString());

        Long date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);

        //调用api查询数据列表
        List<Visitors> list = visitorsApi.queryVisitorsList(UserHolder.getUserId(), date);
        if (list == null) {
            return new ArrayList<>();
        }

        //提取用户id
        List<Long> visitorUserIds = CollUtil.getFieldValues(list, "visitorUserId", Long.class);

        //查看用户详情
        Map<Long, UserInfo> map = userInfoApi.findByIds(visitorUserIds, null);

        //构造返回对象
        List<VisitorsVo> vos = new ArrayList<>();
        for (Visitors visitors : list) {
            UserInfo userInfo = map.get(visitors.getVisitorUserId());
            if (userInfo != null) {
                vos.add(VisitorsVo.init(userInfo, visitors));
            }
        }

        return vos;
    }
}
