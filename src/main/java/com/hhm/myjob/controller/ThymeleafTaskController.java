package com.hhm.myjob.controller;

import com.hhm.myjob.util.SpringBeanUtil;
import com.hhm.myjob.dto.TargetAndSchedulerDto;
import com.hhm.myjob.dto.TaskDto;
import com.hhm.myjob.scheduler.CustomTaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
@Controller
@RequestMapping("/task")
public class ThymeleafTaskController {

    private final List<TaskDto> taskList = new ArrayList<>();
    private final Map<String, TargetAndSchedulerDto> targetMap = new HashMap<>();

    @RequestMapping("/list")
    public String taskList(ModelMap map){
        map.addAttribute("taskList",taskList);
        map.addAttribute("taskDto",new TaskDto());
        return "thymeleaf/task";
    }

    @PostMapping("/add")
    public String addJob(TaskDto taskDto) {

        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        if(!targetMap.containsKey(key)){
            TargetAndSchedulerDto targetAndSchedulerDto = new TargetAndSchedulerDto();
            targetAndSchedulerDto.setTaskName(taskDto.getTaskName());
            targetAndSchedulerDto.setClassName(taskDto.getTaskClass());
            targetAndSchedulerDto.setMethod(taskDto.getTaskMethod());
            targetAndSchedulerDto.setCron(taskDto.getCron());
            targetAndSchedulerDto.setThreadNum(taskDto.getThreadNum());
            targetAndSchedulerDto.setStatus(0);
            targetMap.put(key, targetAndSchedulerDto);

            taskList.add(taskDto);
        }
        return "redirect:/task/list";
    }

    @ResponseBody
    @GetMapping("/operate")
    public String operateJob(@RequestParam Integer index){
        TaskDto taskDto = taskList.get(index);
        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        TargetAndSchedulerDto targetAndSchedulerDto = targetMap.get(key);
        if(taskDto.getStatus() == 0){
            // 开启任务
            startJob(targetAndSchedulerDto);
            taskDto.setStatus(1);
            log.info("开启任务成功");
        }else{
            stopJob(targetAndSchedulerDto);
            taskDto.setStatus(0);
            log.info("停止任务成功");
        }
        return "true";
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
    }
}
