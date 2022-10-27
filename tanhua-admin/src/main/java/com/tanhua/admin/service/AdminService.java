package com.tanhua.admin.service;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.exception.BusinessException;
import com.tanhua.admin.interceptor.AdminHolder;
import com.tanhua.admin.mapper.AdminMapper;
import com.tanhua.commons.utils.Constants;
import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.Admin;
import com.tanhua.model.vo.AdminVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 用户登录
     * @param map
     * @return
     */
    public Map login(Map map) {
        //1.获取参数
        String username = (String) map.get("username");
        String password = (String) map.get("password");
        String verificationCode = (String) map.get("verificationCode");
        String uuid = (String) map.get("uuid");

        //2.校验参数
        if(StringUtils.isEmpty(username)){
            throw new BusinessException("用户名不能为空");
        }
        if(StringUtils.isEmpty(password)){
            throw new BusinessException("密码不能为空");
        }
        if(StringUtils.isEmpty(verificationCode)){
            throw new BusinessException("验证码不能为空");
        }
        if(StringUtils.isEmpty(uuid)){
            throw new BusinessException("uuid不能为空");
        }

        //3.校验验证码
        String code = redisTemplate.opsForValue().get(Constants.CAP_CODE + uuid);
        if(StringUtils.isEmpty(code) || !verificationCode.equalsIgnoreCase(code)){
            throw new BusinessException("验证码错误");
        }

        redisTemplate.delete(Constants.CAP_CODE + uuid);

        //4.校验用户名和密码
        LambdaQueryWrapper<Admin> queryWrapper = new QueryWrapper<Admin>().lambda();
        queryWrapper.eq(Admin::getUsername,username);
        Admin admin = adminMapper.selectOne(queryWrapper);

        if(admin == null){
            throw new BusinessException("用户名或密码错误");
        }
        String md5Password = SecureUtil.md5(password);
        if(!md5Password.equals(admin.getPassword())){
            throw new BusinessException("用户名或密码错误");
        }

        //5.生成token
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",admin.getId());
        claims.put("username",admin.getUsername());
        String token = JwtUtils.getToken(claims);

        //6.返回结果
        Map retMap = new HashMap();
        retMap.put("token",token);
        return retMap;
    }

    /**
     * 获取当前用户的用户资料
     * @param
     * @return
     */
    public AdminVo profile() {
        Long id = AdminHolder.getUserId();
        Admin admin = adminMapper.selectById(id);
        return AdminVo.init(admin);
    }
}
