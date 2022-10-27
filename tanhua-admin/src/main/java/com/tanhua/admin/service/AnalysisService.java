package com.tanhua.admin.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.admin.mapper.AnalysisMapper;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.model.domain.Analysis;
import com.tanhua.model.domain.Log;
import com.tanhua.model.dto.YearDateDto;
import com.tanhua.model.dto.YearReturnDto;
import com.tanhua.model.vo.AnalysisSummaryVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class AnalysisService {

    @Autowired
    private com.tanhua.admin.mapper.LogMapper logMapper;

    @Autowired
    private AnalysisMapper analysisMapper;

    @DubboReference
    private UserApi userApi;

    /**
     * 定时统计tb_log表中的数据,保存或者更新tb_analysis表中的数据
     * 1.查询tb_log表中的数据
     * 2.构造Analysis对象
     * 3.保存或者更新tb_analysis表中的数据
     */
    public void analysis() throws ParseException {

        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String yestodayStr = DateUtil.yesterday().toString("yyyy-MM-dd"); //工具类
        //1、统计每日注册用户数
        Integer numRegistered = logMapper.queryByTypeAndLogTime("0102", todayStr);
        //2、统计每日登陆用户
        Integer numLogin = logMapper.queryByTypeAndLogTime("0101", todayStr);
        //3、统计活跃的用户数
        Integer numActive = logMapper.queryByLogTime(todayStr);
        //4、统计次日留存的用户数
        Integer numRetention1d = logMapper.queryNumRetention1d(todayStr, yestodayStr);
        //5、根据当前时间查询AnalysisByDay数据
        QueryWrapper<Analysis> qw = new QueryWrapper<>();
        ;
        qw.eq("record_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));


        Analysis analysis = analysisMapper.selectOne(qw);
        if (analysis == null) {
            //7、如果不存在，保存
            System.out.println("保存新数据");
            analysis = new Analysis();
            analysis.setRecordDate(new Date());
            analysis.setNumRegistered(numRegistered);
            analysis.setNumLogin(numLogin);
            analysis.setNumActive(numActive);
            analysis.setNumRetention1d(numRetention1d);
            analysis.setCreated(new Date());
            analysisMapper.insert(analysis);
        } else {
            //8、如果存在，更新
            System.out.println("更新数据");
            analysis.setNumRegistered(numRegistered);
            analysis.setNumLogin(numLogin);
            analysis.setNumActive(numActive);
            analysis.setNumRetention1d(numRetention1d);
            analysisMapper.updateById(analysis);
        }
    }

    public AnalysisSummaryVo selectCount() throws ParseException {
        AnalysisSummaryVo vo = new AnalysisSummaryVo();

        //1、查询user表中的数据
        int numUser = userApi.queryCount();

        //2、构造AnalysisSummaryVo对象
        vo.setCumulativeUsers((long) numUser);
        vo.setActivePassMonth(Long.valueOf(this.selectCountByActivePassMonth(30L * 24 * 60 * 60 * 1000).toString()));
        vo.setActivePassWeek(Long.valueOf(this.selectCountByActivePassMonth(7L * 24 * 60 * 60 * 1000).toString()));
        vo.setNewUsersToday(this.selectCountByNewUsersToday());
        vo.setNewUsersTodayRate(BigDecimal.valueOf(12.0));
        vo.setLoginTimesToday(Long.valueOf(logMapper.queryByTypeAndLogTime("0101", new SimpleDateFormat("yyyy-MM-dd").format(new Date())).toString()));
        vo.setNewUsersTodayRate(BigDecimal.valueOf(32.0));
        vo.setActiveUsersToday(Long.valueOf(logMapper.queryByLogTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date())).toString()));
        vo.setActiveUsersTodayRate(BigDecimal.valueOf(7.2));

        return vo;
    }

    //查询今日注册的用户数
    private Long selectCountByNewUsersToday() {
        String todayStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        return logMapper.queryByTypeAndLogTime("0102", todayStr).longValue();
    }

    //查询上周活跃的用户数
    public Integer selectCountByActivePassMonth(Long time) {
        LambdaQueryWrapper<Log> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Log::getLogTime, new SimpleDateFormat("yyyy-MM-dd").format(new Date(new Date().getTime() - time)));
        //去重
        queryWrapper.select(Log::getUserId);

        return logMapper.selectCount(queryWrapper);
    }


    public YearReturnDto query(String sd, String ed, String type) {

        List<YearDateDto> thisYear = new ArrayList<>();
        List<YearDateDto> lastYear = new ArrayList<>();

        //查询今年的数据
        List<Analysis> analyses = this.selectActive(sd, ed);
        for (Analysis analysis : analyses) {
            YearDateDto one = new YearDateDto();

            if (Objects.equals(type, "101")) {
                one.setTitle(new SimpleDateFormat().format(analysis.getRecordDate()));
                one.setAmount(analysis.getNumRegistered());
            } else if (Objects.equals(type, "102")) {
                one.setTitle(new SimpleDateFormat().format(analysis.getRecordDate()));
                one.setAmount(analysis.getNumActive());
            }

            thisYear.add(one);
        }

        //查询去年的数据

        return new YearReturnDto(thisYear, lastYear);
    }

    private List<Analysis> selectActive(String sd, String ed) {
        LambdaQueryWrapper<Analysis> queryWrapper = new LambdaQueryWrapper<>();

        String start = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(sd)));
        String end = new SimpleDateFormat("yyyy-MM-dd").format(new Date(Long.parseLong(ed)));
        System.out.println(start);
        System.out.println(end);

        queryWrapper.between(Analysis::getRecordDate, start, end);

        return analysisMapper.selectList(queryWrapper);
    }
}
