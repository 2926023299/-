package com.tanhua.admin.controller;

import com.tanhua.admin.service.AnalysisService;
import com.tanhua.model.dto.YearReturnDto;
import com.tanhua.model.vo.AnalysisSummaryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/dashboard")
public class DashBoardController {

    @Autowired
    private AnalysisService analysisService;

    /**
     * 新增、活跃用户、次日留存率
     *
     * @return
     */
    @GetMapping("/summary")
    public ResponseEntity query() throws ParseException {

        AnalysisSummaryVo vo =  analysisService.selectCount();

        return ResponseEntity.ok(vo);
    }


    @GetMapping("/users")
    public ResponseEntity query2(String sd, String ed, String type) throws ParseException {

        YearReturnDto dto =  analysisService.query(sd, ed, type);

        return ResponseEntity.ok(dto);
    }
}
