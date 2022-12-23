package com.hhm.myjob.controller;

import com.hhm.myjob.config.WebSocketConfig;
import com.hhm.myjob.dto.Response;
import com.hhm.myjob.dto.TargetAndSchedulerDto;
import com.hhm.myjob.dto.TaskDto;
import com.hhm.myjob.scheduler.CustomTaskScheduler;
import com.hhm.myjob.util.ResponseUtil;
import com.hhm.myjob.util.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
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
@RequestMapping("/task2")
public class TaskController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private WebSocketConfig webSocketConfig;
    private final Map<String, TargetAndSchedulerDto> targetMap = new HashMap<>();

    @GetMapping("/list")
    public Response<List<TaskDto>> taskList(){
        if(webSocketConfig.getMessagingTemplate() == null){
            webSocketConfig.setMessagingTemplate(messagingTemplate);
        }
        List<TaskDto> taskList = new ArrayList<>();
        targetMap.values().forEach(e -> {
            TaskDto taskDto = new TaskDto();
            taskDto.setTaskName(e.getTaskName());
            taskDto.setTaskClass(e.getClassName());
            taskDto.setTaskMethod(e.getMethod());
            taskDto.setCron(e.getCron());
            taskDto.setStatus(e.getStatus());
            taskDto.setThreadNum(e.getThreadNum());
            taskList.add(taskDto);
        });
        return ResponseUtil.success(taskList);
    }

    @PostMapping("/add")
    public Response<String> addJob(@RequestBody TaskDto taskDto){
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        if(targetMap.containsKey(key)){
            return ResponseUtil.fail("已存在该任务");
        }
        TargetAndSchedulerDto targetAndSchedulerDto = new TargetAndSchedulerDto();
        targetAndSchedulerDto.setTaskName(taskDto.getTaskName());
        targetAndSchedulerDto.setClassName(taskDto.getTaskClass());
        targetAndSchedulerDto.setMethod(taskDto.getTaskMethod());
        targetAndSchedulerDto.setCron(taskDto.getCron());
        targetAndSchedulerDto.setThreadNum(taskDto.getThreadNum());
        targetAndSchedulerDto.setStatus(0);
        targetMap.put(key, targetAndSchedulerDto);
        return ResponseUtil.success("success");
    }

    @PostMapping("/edit")
    public Response<String> edit(@RequestBody TaskDto taskDto){
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        if(!targetMap.containsKey(key)){
            return ResponseUtil.fail("不存在该任务");
        }
        TargetAndSchedulerDto targetAndSchedulerDto = targetMap.get(key);
        if(0 != targetAndSchedulerDto.getStatus()){
            return ResponseUtil.fail("任务运行中，不能编辑");
        }
        targetAndSchedulerDto.setCron(taskDto.getCron());

        return ResponseUtil.success("success");
    }

    @PostMapping("/delete")
    public Response<String> delete(@RequestBody TaskDto taskDto){
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        TargetAndSchedulerDto targetAndSchedulerDto = targetMap.get(key);
        if(0 != targetAndSchedulerDto.getStatus()){
            return ResponseUtil.fail("任务运行中，不能删除");
        }

        if(targetAndSchedulerDto.getCustomTaskScheduler() != null){
            while (!targetAndSchedulerDto.getCustomTaskScheduler().getScheduledExecutor().isShutdown()){
                targetAndSchedulerDto.getCustomTaskScheduler().destroy();
            }
            targetAndSchedulerDto.setCustomTaskScheduler(null);
        }
        targetMap.remove(key);
        return ResponseUtil.success("success");
    }

    @ResponseBody
    @PostMapping("/operate")
    public Response<String> operateJob(@RequestBody TaskDto taskDto){
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        if(!targetMap.containsKey(key)){
            return ResponseUtil.fail("不存在该任务");
        }
        TargetAndSchedulerDto targetAndSchedulerDto = targetMap.get(key);

        try {
            if(targetAndSchedulerDto.getStatus() == 0){
                // 开启任务
                startJob(targetAndSchedulerDto);
                targetAndSchedulerDto.setStatus(1);
                log.info("开启任务成功");
            }else{
                stopJob(targetAndSchedulerDto);
                targetAndSchedulerDto.setStatus(0);
                log.info("停止任务成功");
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return ResponseUtil.fail(e.getMessage());
        }

        return ResponseUtil.success("success");
    }

    private void startJob(TargetAndSchedulerDto targetAndSchedulerDto){

        if(targetAndSchedulerDto.getCustomTaskScheduler() == null && targetAndSchedulerDto.getTarget() == null){
            Object o = null;
            try {
                // o = Class.forName(targetAndSchedulerDto.getClassName()).newInstance();
                o = SpringBeanUtil.getBean(Class.forName(targetAndSchedulerDto.getClassName()));
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                throw new RuntimeException("不存在该class");
            }
            CustomTaskScheduler customTaskScheduler = new CustomTaskScheduler();
            targetAndSchedulerDto.setTarget(o);
            targetAndSchedulerDto.setCustomTaskScheduler(customTaskScheduler);
        }

        try {
            ScheduledMethodRunnable runnable = new ScheduledMethodRunnable(targetAndSchedulerDto.getTarget(),
                    Objects.requireNonNull(ReflectionUtils.findMethod(targetAndSchedulerDto.getTarget().getClass(), targetAndSchedulerDto.getMethod())));
            targetAndSchedulerDto.getCustomTaskScheduler().schedule(runnable, new CronTrigger(targetAndSchedulerDto.getCron()));
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException("开启任务失败，" + e.getMessage());
        }
    }

    private void stopJob(TargetAndSchedulerDto targetAndSchedulerDto){
        Map<Object, ScheduledFuture<?>> scheduledTasksMap = targetAndSchedulerDto.getCustomTaskScheduler().getScheduledTasks();
        ScheduledFuture<?> scheduledFuture = scheduledTasksMap.get(targetAndSchedulerDto.getTarget());
        while (!scheduledFuture.isCancelled()){
            scheduledFuture.cancel(true);
        }
        targetAndSchedulerDto.setTarget(null);
        targetAndSchedulerDto.setCustomTaskScheduler(null);
    }
}
