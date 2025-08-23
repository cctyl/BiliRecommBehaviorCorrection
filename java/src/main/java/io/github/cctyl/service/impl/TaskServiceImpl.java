package io.github.cctyl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.cctyl.config.TaskPool;
import io.github.cctyl.domain.dto.R;
import io.github.cctyl.domain.enumeration.TaskStatus;
import io.github.cctyl.domain.po.Task;
import io.github.cctyl.domain.vo.OverviewVo;
import io.github.cctyl.mapper.TaskMapper;
import io.github.cctyl.service.TaskService;
import io.github.cctyl.utils.BeanUtil;
import io.github.cctyl.utils.ThreadUtil;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;


@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Override
    public List<Task> getTaskList() {
        return this.list();
    }

    @Override
    public void updateTaskStatus(String name, TaskStatus taskStatus) {

        this.lambdaUpdate()
                .eq(Task::getClassMethodName,name)
                .set(Task::getCurrentRunStatus,taskStatus)
                .update();
    }

    @Override
    public void updateLastRunTime(String name, long millis) {
        this.lambdaUpdate()
                .eq(Task::getClassMethodName,name)
                .set(Task::getLastRunTime,new Date(millis))
                .update();
    }

    @Override
    public Task getByClassAndMethodName(String name) {
        return this.lambdaQuery()
                .eq(Task::getClassMethodName,name)
                .one();
    }



    /**
     * 把任务交给线程池执行并记录日志
     * @param name
     * @param runnable
     * @return
     */
    public boolean doTask(String name, Runnable runnable) {
        addIfNotExist(name);
        updateTaskStatus(name, TaskStatus.WAITING);
        updateLastRunTime(name,  System.currentTimeMillis());
        return TaskPool.putIfAbsent(name,() -> {
            try {
                updateTaskStatus(name, TaskStatus.RUNNING);
                Task task = getByClassAndMethodName(name);
                long start = System.currentTimeMillis();
                runnable.run();
                long end = System.currentTimeMillis();
                task.setLastRunTime(new Date(start))
                        .setTotalRunCount(task.getTotalRunCount() + 1)
                        .setLastRunDuration((int) ((end - start) / 1000))
                        .setCurrentRunStatus(TaskStatus.STOPPED);
                updateById(task);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 不存在则新增该任务
     * @param name
     */
    private synchronized void addIfNotExist(String name) {


        boolean exists = this.lambdaQuery()
                .eq(Task::getClassMethodName, name)
                .exists();
        if (!exists){
            this.save(new Task()
                    .setClassMethodName(name)
                    .setIsEnabled(false)
                    .setCurrentRunStatus(TaskStatus.STOPPED)
                    .setTotalRunCount(0)
                    .setScheduledHour(-1)
            )

            ;
        }

    }

    @Override
    public void updateTaskEnable(boolean value) {
        this.lambdaUpdate()
                .set(Task::getIsEnabled,value)
                .update();

    }


    @Override
    public List<Task> getEnableScheduleTask(int hour) {
     return   this.lambdaQuery().eq(Task::getIsEnabled, true)
                .eq(Task::getScheduledHour,hour)
                .list();
    }

    @Override
    public void resetTaskStatus() {

        this.lambdaUpdate().set(Task::getCurrentRunStatus,TaskStatus.STOPPED).update();

    }


    @Override
    public void fillOverviewInfo(OverviewVo overviewVo) {

        //查找正在运行的任务数量
        List<Task> runningTaskList = this.lambdaQuery()
                .in(Task::getCurrentRunStatus, TaskStatus.RUNNING,TaskStatus.WAITING)
                .orderByAsc(Task::getLastRunTime)
                .list();

        overviewVo.setRunningTaskCount(runningTaskList.size())
                .setTaskList(runningTaskList);
    }

    @Override
    public R commonTriggerTask(String classAndMethodName,String[] paramArr) {
        int index = classAndMethodName.lastIndexOf('.');
        String className = classAndMethodName.substring(0, index);
        String methodName = classAndMethodName.substring(index + 1);

        try {
            Object beanByClassPath = BeanUtil.getBeanByClassPath(className);
            Method taskMethod = beanByClassPath.getClass().getDeclaredMethod(methodName);
            Class<?>[] parameterTypeArr = taskMethod.getParameterTypes();
            boolean invoke;
            if (parameterTypeArr.length < 1) {
                invoke = (boolean) taskMethod.invoke(beanByClassPath);
            } else {
                Object[] args = new Object[parameterTypeArr.length];
                for (int i = 0; i < paramArr.length; i++) {

                    if (parameterTypeArr[i].equals(String.class)) {
                        args[i] = paramArr[i];
                    } else if (parameterTypeArr[i].equals(Integer.class)) {
                        args[i] = Integer.parseInt(paramArr[i]);
                    } else if (parameterTypeArr[i].equals(Long.class)) {
                        args[i] = Long.parseLong(paramArr[i]);
                    } else if (parameterTypeArr[i].equals(Double.class)) {
                        args[i] = Double.parseDouble(paramArr[i]);
                    } else if (parameterTypeArr[i].equals(Boolean.class)) {
                        args[i] = Boolean.parseBoolean(paramArr[i]);
                    } else if (parameterTypeArr[i].equals(Date.class)) {
                        args[i] = new Date(Long.parseLong(paramArr[i]));
                    } else {
                        throw new RuntimeException("不支持的转换 String -->" + paramArr[i].getClass().getName());
                    }
                }
                invoke = (boolean) taskMethod.invoke(beanByClassPath, args);
            }


            if (invoke) {
                return R.ok().setData(classAndMethodName + " 任务已启动");
            } else {
                return R.error().setMessage(classAndMethodName + " 任务正在进行中");
            }
        } catch (NoSuchMethodException e) {
            return R.error().setMessage("无此任务：" + classAndMethodName);
        } catch (IllegalAccessException e) {
            return R.error().setMessage("该任务不允许外部访问：" + classAndMethodName);
        } catch (InvocationTargetException e) {
            return R.error().setMessage("调用异常");
        } catch (ClassNotFoundException e) {
            return R.error().setMessage("类或方法不存在");
        }
    }
}
