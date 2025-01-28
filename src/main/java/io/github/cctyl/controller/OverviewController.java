package io.github.cctyl.controller;

import io.github.cctyl.api.BiliApi;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.dto.Region;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/overview")
@Slf4j
@Tag(name="总览模块")
@RequiredArgsConstructor
public class OverviewController {


    private final TaskService taskService;
    private final DictService dictService;
    private final VideoDetailService videoDetailService;
    private final ConfigService configService;
    private final PrepareVideoService prepareVideoService;



    @GetMapping("")
    public R<OverviewVo> overviewInfo(
            @RequestParam int year

    ){
        OverviewVo overviewVo = new OverviewVo();
        overviewVo.setYear(year);
        taskService.fillOverviewInfo(overviewVo);
        dictService.fillOverviewInfo(overviewVo);
        videoDetailService.fillOverviewInfo(overviewVo);
        prepareVideoService.fillOverviewInfo(overviewVo);
        configService.fillOverviewInfo(overviewVo);

        return R.data(overviewVo);
    }



}
