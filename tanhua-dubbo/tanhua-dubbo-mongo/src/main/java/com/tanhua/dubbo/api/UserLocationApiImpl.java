package com.tanhua.dubbo.api;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.model.mongo.UserLocation;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@DubboService
public class UserLocationApiImpl implements UserLocationApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    //更新或保存地理位置信息
    @Override
    public Boolean updateLocation(Long userId, Double longitude, Double latitude, String address) {
        try {
            //根据用户id查询地理位置信息
            Query query = new Query(Criteria.where("userId").is(userId));
            UserLocation one = mongoTemplate.findOne(query, UserLocation.class);

            //如果没有，保存
            if (one == null) {
                one = new UserLocation();
                one.setUserId(userId);
                one.setAddress(address);
                one.setLocation(new GeoJsonPoint(longitude, latitude));
                one.setCreated(System.currentTimeMillis());
                one.setLastUpdated(System.currentTimeMillis());
                mongoTemplate.save(one);
            } else {
                //如果有，更新
                one.setAddress(address);
                one.setLocation(new GeoJsonPoint(longitude, latitude));
                one.setLastUpdated(System.currentTimeMillis());
                mongoTemplate.save(one);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    //查找附近的人的所有用户id
    @Override
    public List<Long> queryNearUser(Long userId, Double meter) {
        //根据用户id查询地理位置信息
        Query query = new Query(Criteria.where("userId").is(userId));
        UserLocation one = mongoTemplate.findOne(query, UserLocation.class);

        //以当前地理位置绘制原点
        if (one == null) {
            return null;
        }
        GeoJsonPoint point = one.getLocation();

        //绘制半径
        Distance distance = new Distance(meter / 1000, Metrics.KILOMETERS);

        //绘制圆
        Circle circle = new Circle(point, distance);

        //查询附近的人
        Query query1 = new Query(Criteria.where("location").withinSphere(circle));
        List<UserLocation> userLocations = mongoTemplate.find(query1, UserLocation.class);

        return CollUtil.getFieldValues(userLocations, "userId", Long.class);
    }
}
