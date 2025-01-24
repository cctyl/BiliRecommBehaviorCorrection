package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.domain.po.PrepareVideo;
import io.github.cctyl.domain.po.Task;
import io.github.cctyl.mapper.PrepareVideoMapper;
import io.github.cctyl.mapper.TaskMapper;
import io.github.cctyl.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Override
    public List<Task> getTaskList() {
        return this.list();
    }
}
