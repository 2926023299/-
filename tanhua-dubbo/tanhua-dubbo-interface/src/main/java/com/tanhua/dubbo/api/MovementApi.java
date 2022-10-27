package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface MovementApi {
    /**
     * 发布动态
     * @param movement
     */
    String publish(Movement movement);

    PageResult findByUserId(Long userId, Integer page, Integer pagesize);

    /**
     * 根据用户id，查询用户好友发布的动态列表
     * @param page
     * @param pagesize
     * @param userId
     * @return
     */
    List<Movement> findFriendsMovements(Integer page, Integer pagesize, Long userId);

    /**
     * 根据pid获取动态
     * @param pids
     * @return
     */
    List<Movement> findMovementsByPids(List<Long> pids);

    /**
     * 随机获取动态数据
     * @param pagesize
     * @return
     */
    List<Movement> randomMovements(Integer pagesize);

    Movement findById(String movementId);

    /**
     * 查询动态
     *
     * @param
     * @return
     */
    PageResult findAllMovements(Integer page, Integer pagesize, Long uid, Integer state);

    //更新动态状态
    void updateState(String movementId, int state);
}
