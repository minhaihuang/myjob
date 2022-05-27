package com.hhm.myjob.controller;

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

import javax.annotation.Resource;
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
public class TaskController {
    @Resource
    private CustomTaskScheduler customTaskScheduler;
    private final List<TaskDto> taskList = new ArrayList<>();
    private final Map<String, Object> targetMap = new HashMap<>();
    @RequestMapping("/list")
    public String taskList(ModelMap map){
        map.addAttribute("taskList",taskList);
        map.addAttribute("taskDto",new TaskDto());
        return "thymeleaf/task";
    }

    @PostMapping("/add")
    public String addJob(TaskDto taskDto) throws Exception {

        String key = taskDto.getTaskClass() + "#" + taskDto.getTaskMethod();
        if(!targetMap.containsKey(key)){
            Object o = Class.forName(taskDto.getTaskClass()).newInstance();
            targetMap.put(key, o);

            taskList.add(taskDto);
        }
        return "redirect:/task/list";
    }

    @ResponseBody
    @GetMapping("/operate")
    public String operateJob(@RequestParam Integer index) throws Exception {
        TaskDto taskDto = taskList.get(index);
        if(taskDto.getStatus() == 0){
            // 开启任务
            startJob(taskDto);
            taskDto.setStatus(1);
            log.info("开启任务成功");
        }else{
            stopJob(taskDto);
            taskDto.setStatus(0);
            log.info("停止任务成功");
        }
        return "true";
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
        System.out.println(targetMap.containsKey(key));

        Object o = targetMap.get(key);

        Map<Object, ScheduledFuture<?>> scheduledTasksMap = customTaskScheduler.getScheduledTasks();
        ScheduledFuture<?> scheduledFuture = scheduledTasksMap.get(o);
        scheduledFuture.cancel(true);
    }
}
