package io.github.cctyl.domain.vo;

import io.github.cctyl.domain.po.Task;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class OverviewVo {

    private int year;

    //当前运行任务数
    private Integer runningTaskCount;

    //黑名单规则数
    private long blackRuleCount;

    //白名单规则数
    private long whiteRuleCount;

    //搜索关键词数
    private long searchKeywordCount;

    //待筛选的黑名单关键词
    private long blackCacheCount;

    //系统运行天数
    private long runDays;


    //历史处理过的白名单数据 天-数量
    private List<Map<String,Integer>> whiteHistory;

    //历史处理过的黑名单数据 天-数量
    private List<Map<String,Integer>> blackHistory;

    //历史处理过的其他数据 天-数量
    private List<Map<String,Integer>> otherHistory;

    //待二次处理的数据量
    private long secondHandleCount;

    //待三次处理的数据量
    private long thirdHandleCount;

    //正在运行的任务列表
    private List<Task> taskList;

    //历史点赞的视频数
    private long likeVideoCount;

    //历史点踩的视频数
    private long hateVideoCount;

}
