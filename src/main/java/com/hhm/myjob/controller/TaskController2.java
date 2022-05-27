package com.hhm.myjob.controller;

import com.hhm.myjob.dto.Response;
import com.hhm.myjob.dto.TaskDto;
import com.hhm.myjob.scheduler.CustomTaskScheduler;
import com.hhm.myjob.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

/**
 * 任务controller
 * @Author: huanghm
 * @Date: 2022/05/14
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/task3")
public class TaskController2 {
    @Resource
    private CustomTaskScheduler customTaskScheduler;
    private final List<TaskDto> taskList = new ArrayList<>();
    private final Map<String, Object> targetMap = new HashMap<>();

    @GetMapping("/list")
    public Response<List<TaskDto>> taskList(ModelMap map){
        return ResponseUtil.success(taskList);
    }

    @PostMapping("/add")
    public Response<String> addJob(@RequestBody TaskDto taskDto) throws Exception {

        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        if(!targetMap.containsKey(key)){
            Object o = Class.forName(taskDto.getTaskClass()).newInstance();
            targetMap.put(key, o);

            taskList.add(taskDto);
        }
        return ResponseUtil.success("success");
    }

    @PostMapping("/edit")
    public Response<String> edit(@RequestBody TaskDto taskDto) throws Exception {

        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        targetMap.remove(key);
        Iterator<TaskDto> iterator = taskList.iterator();
        while (iterator.hasNext()){
            TaskDto next = iterator.next();
            String key2 = next.getTaskClass() + "#" + next.getTaskMethod();
            if(key.equals(key2)){
                iterator.remove();
            }
        }

        Object o = Class.forName(taskDto.getTaskClass()).newInstance();
        targetMap.put(key, o);
        taskList.add(taskDto);

        return ResponseUtil.success("success");
    }

    @ResponseBody
    @PostMapping("/operate")
    public Response<String> operateJob(@RequestBody TaskDto taskDto) throws Exception {
        TaskDto target = null;
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        Iterator<TaskDto> iterator = taskList.iterator();
        while (iterator.hasNext()){
            TaskDto next = iterator.next();
            String key2 = next.getTaskClass() + "#" + next.getTaskMethod();
            if(key.equals(key2)){
                target = next;
                break;
            }
        }

        if(target.getStatus() == 0){
            // 开启任务
            startJob(target);
            target.setStatus(1);
            log.info("开启任务成功");
        }else{
            stopJob(target);
            target.setStatus(0);
            log.info("停止任务成功");
        }
        return ResponseUtil.success("success");
    }

    private void startJob(TaskDto taskDto) throws Exception{
        String taskClass = taskDto.getTaskClass();
        String cron = taskDto.getCron();
        String method = taskDto.getTaskMethod();

        Object o = targetMap.get(taskClass + "#" + method);

        ScheduledMethodRunnable runnable = new ScheduledMethodRunnable(o,
                Objects.requireNonNull(ReflectionUtils.findMethod(o.getClass(), method)));

        customTaskScheduler.schedule(runnable, new CronTrigger(cron));
    }

    private void stopJob(TaskDto taskDto) throws Exception {
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        Object o = targetMap.get(key);

        Map<Object, ScheduledFuture<?>> scheduledTasksMap = customTaskScheduler.getScheduledTasks();
        ScheduledFuture<?> scheduledFuture = scheduledTasksMap.get(o);
        while (!scheduledFuture.isCancelled()){
            scheduledFuture.cancel(true);
        }

    }
}
