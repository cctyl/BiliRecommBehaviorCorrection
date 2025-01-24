package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.enumeration.TaskStatus;
import io.github.cctyl.domain.po.Task;

import java.util.List;

public interface TaskService extends IService<Task> {
    List<Task> getTaskList();

    void updateTaskStatus(String name, TaskStatus taskStatus);

    void updateLastRunTime(String name, long millis);

    Task getByClassAndMethodName(String name);


    boolean doTask(String name, Runnable runnable);

    void updateTaskEnable(boolean value);

    List<Task> getEnableScheduleTask(int hour);

    R commonTriggerTask(String classAndMethodName);
}
