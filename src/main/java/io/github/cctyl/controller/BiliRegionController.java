package io.github.cctyl.controller;

import io.github.cctyl.api.BiliApi;
import io.github.cctyl.domain.dto.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/region")
@Slf4j
@Tag(name="bilibili分区模块")
public class BiliRegionController {


    @Autowired
    private BiliApi biliApi;


    @GetMapping("/list")
    public R getRegionList(){
        return R.data(biliApi.getAllRegion(true));
    }

}
