package com.tanhua.dubbo.api;

import java.util.List;

public interface UserLocationApi {
    //更新或保存地理位置信息
    Boolean updateLocation(Long userId, Double longitude, Double latitude, String address);

    //查找附近的人的所有用户id
    List<Long> queryNearUser(Long userId, Double valueOf);
}
