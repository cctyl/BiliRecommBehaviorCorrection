package io.github.cctyl.config;

import io.github.cctyl.utils.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 任务池子
 *
 * @author tyl
 */
@Slf4j
public class TaskPool {

    /**
     * 方法名与对应任务的map
     */
    private static final Map<String, Future<?>> METHOD_NAME_TASK_MAP = new ConcurrentHashMap<>();

    private static final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock r = rw.readLock();
    private static final ReentrantReadWriteLock.WriteLock w = rw.writeLock();
    //单个线程的线程池
    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    public static List<String> getRunningTaskName() {
        var list = new ArrayList<String>();
        for (Map.Entry<String, Future<?>> entry : METHOD_NAME_TASK_MAP.entrySet()) {
            if (
                    entry.getValue() != null && !entry.getValue().isDone()
            ) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    /**
     * 获取调用者方法所对应的任务
     *
     * @return
     */
    public static Future<?> getTask() {
        String enclosingMethodName = ReflectUtil.getEnclosingMethodName();
        r.lock();
        try {
            return METHOD_NAME_TASK_MAP.get(enclosingMethodName);
        } finally {
            r.unlock();
        }

    }

    /**
     * 是否存在当前方法对应的任务，并且该任务正在运行中
     *
     * @return true 任务存在并且正在运行，false 任务不存在或者存在但是已完成
     */
    public static boolean existsRunningTask() {
        String enclosingMethodName = ReflectUtil.getEnclosingMethodName();
        r.lock();
        try {
            Future<?> futureTask = METHOD_NAME_TASK_MAP.get(enclosingMethodName);
            if (
                    futureTask == null
                            ||
                            futureTask.isDone()
            ) {
                return false;
            }
            return true;
        } finally {
            r.unlock();
        }

    }

    public static boolean putIfAbsent(String enclosingMethodName ,Runnable runnable) {
        r.lock();
        try {
            Future<?> futureTask = METHOD_NAME_TASK_MAP.get(enclosingMethodName);
            if (
                    futureTask != null
                            &&
                            !futureTask.isDone()
            ) {
                return false;
            }

        } finally {
            r.unlock();
        }

        w.lock();
        try {
            Future<Void> future = CompletableFuture.runAsync(runnable,singleThreadExecutor);
            METHOD_NAME_TASK_MAP.put(enclosingMethodName, future);
        } finally {
            w.unlock();
        }

        return true;
    }
    public static boolean putIfAbsent(Runnable runnable) {
        String enclosingMethodName = ReflectUtil.getEnclosingMethodName();
        return putIfAbsent(enclosingMethodName,runnable);
    }

    /**
     * 放入一个新任务
     *
     * @param runnable
     */
    public static void putTask(Runnable runnable) {
        String enclosingMethodName = ReflectUtil.getEnclosingMethodName();
        r.lock();

        try {
            Future<?> futureTask = METHOD_NAME_TASK_MAP.get(enclosingMethodName);
            if (
                    futureTask != null
                            &&
                            !futureTask.isDone()
            ) {
                log.debug("{}任务已存在，且尚未运行完成，无法新增该任务", enclosingMethodName);
                return;
            }
        } finally {
            r.unlock();
        }

        w.lock();
        try {
            Future<Void> future = CompletableFuture.runAsync(runnable,singleThreadExecutor);
            //如果不存在，或者存在，但是已经完成了，则允许添加新任务
            METHOD_NAME_TASK_MAP.put(enclosingMethodName, future);
        } finally {
            w.unlock();
        }

    }
}
