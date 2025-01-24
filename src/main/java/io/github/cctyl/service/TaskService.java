package io.github.cctyl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.cctyl.domain.po.Stat;
import io.github.cctyl.domain.po.Task;

import java.util.List;

public interface TaskService extends IService<Task> {
    List<Task> getTaskList();

}
