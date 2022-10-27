package com.tanhua.dubbo.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.model.domain.UserInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserInfoMapper extends BaseMapper<UserInfo> {
    @Select("select * from tb_user_info info where id in (select black_user_id from tb_black_list list where user_id = #{userId})")
    IPage<UserInfo> findBlackList(@Param("iPage") IPage<UserInfo> iPage, @Param("userId") Long userId);
}
