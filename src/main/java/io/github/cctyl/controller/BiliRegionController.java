package io.github.cctyl.controller;

import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/region")
public class BiliRegionController {


    @Autowired
    private BiliApi biliApi;


    @GetMapping("/list")
    public R getRegionList(){
        return R.data(biliApi.getAllRegion());
    }

}
