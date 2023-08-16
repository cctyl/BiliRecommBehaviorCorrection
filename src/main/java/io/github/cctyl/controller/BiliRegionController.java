package io.github.cctyl.controller;

import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.R;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/region")
@Slf4j
@Api(tags="bilibili分区模块")
public class BiliRegionController {


    @Autowired
    private BiliApi biliApi;


    @GetMapping("/list")
    public R getRegionList(){
        return R.data(biliApi.getAllRegion(true));
    }

}
