package com.hhm.myjob.controller;

import com.hhm.myjob.task.AutoStopTask;
import com.hhm.myjob.task.AutoStopTask2;
import com.hhm.myjob.scheduler.CustomTaskScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @Author huanghm
 * @Date 2022/5/12
 */
//@RestController
@Slf4j
public class StartJonController {
    @Resource
    private CustomTaskScheduler customTaskScheduler;
    @Resource
    private AutoStopTask autoStopTask;
    @Resource
    private AutoStopTask2 autoStopTask2;

    @GetMapping("/start")
    public String startJob(@RequestParam(name = "type", required = false, defaultValue = "1") String type) throws Exception {

        if("1".equals(type)) {
            String className = "com.hhm.myjob.task.AutoStopTask";
            Object o = Class.forName(className).newInstance();
            ScheduledMethodRunnable runnable = new ScheduledMethodRunnable(o,
                    Objects.requireNonNull(ReflectionUtils.findMethod(o.getClass(), "printTask")));
            customTaskScheduler.schedule(runnable, new CronTrigger("*/3 * * * * *"));
        }else{
            ScheduledMethodRunnable runnable = new ScheduledMethodRunnable(autoStopTask2,
                    Objects.requireNonNull(ReflectionUtils.findMethod(autoStopTask2.getClass(), "printTask")));
            customTaskScheduler.schedule(runnable, new CronTrigger("*/10 * * * * *"));
        }
        log.info("重启成功");
        return "success";
    }

    @GetMapping("/stop")
    public boolean stopJob(@RequestParam(name = "type", required = false, defaultValue = "1") String type){
        boolean flag;
        if("1".equals(type)){
            flag = customTaskScheduler.getScheduledTasks().get(autoStopTask.getClass().getName()).cancel(true);
        }else{
            flag = customTaskScheduler.getScheduledTasks().get(autoStopTask2.getClass().getName()).cancel(true);
        }

        if(flag){
            log.info("停止成功");
        }
        return flag;
    }
}
