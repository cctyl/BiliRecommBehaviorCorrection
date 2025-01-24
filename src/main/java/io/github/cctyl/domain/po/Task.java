package io.github.cctyl.domain.po;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class Task {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 上次运行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastRunTime;
    /**
     * 是否开启定时任务
     */
    private Boolean isEnabled;
    /**
     * 当前运行状态
     */
    private Boolean currentRunStatus;
    /**
     * 定时执行的时间，整点
     */
    private String scheduledHour;
    /**
     * 总运行次数
     */
    private Integer totalRunCount;
    /**
     * 上次运行时间
     */
    private Integer lastRunDuration;
    /**
     * 任务名
     */
    private String taskName;

    /**
     * 该任务对应的方法路径
     */
    private String classMethodName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务图片
     */
    private String img;
}
