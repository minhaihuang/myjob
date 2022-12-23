package com.hhm.myjob.task;

import com.hhm.myjob.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author huanghm
 * @Date 2022/5/12
 */
@Component
@Slf4j
public class AutoStopTask {
    @Resource
    private UserService userService;
    private int count;

    public void printTask() {
        userService.test();
        log.info("hhm任务1，{}, currentThread {}任务执行次数：{}", userService.test(), Thread.currentThread(), count + 1);
        count++;
    }
}
