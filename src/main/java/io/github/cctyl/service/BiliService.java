package io.github.cctyl.service;

import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSONObject;
import io.github.cctyl.api.BiliApi;
import io.github.cctyl.entity.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * 相关任务处理
 */
@Service
@Slf4j
public class BiliService {


    @Autowired
    private BiliApi biliApi;


    /**
     * 检查cookie状态
     * 调用历史记录接口来实现
     * @return true 有效  false 无效
     */
    public boolean checkCookie(){
        JSONObject history = biliApi.getHistory();
        log.info("检查cookie状态：{}",history.toString());
        return history.getIntValue("code")==0;
    }


    /**
     * 更新一下必要的cookie
     */
    public void updateCookie(){
        biliApi.getHome();
    }

    /**
     * 处理搜索结果
     *  根据视频信息判断，
     *  最后得出结果，到底是喜欢的视频，还是不喜欢的视频
     *  对于不喜欢的视频，执行点踩操作
     *  对于喜欢视频，执行点赞操作
     * @param thumbUpVideoList
     * @param dislikeVideoList
     * @param searchResult
     */
    public void handleVideo(List<String> thumbUpVideoList, List<String> dislikeVideoList, SearchResult searchResult) {

        //0.获取视频详情 实际上，信息已经足够，但是为了模拟用户真实操作，还是调用一次

        biliApi.getVideoDetail(searchResult.getBvid());




    }
}
