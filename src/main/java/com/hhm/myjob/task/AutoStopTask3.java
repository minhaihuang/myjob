package com.hhm.myjob.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author huanghm
 * @Date 2022/5/12
 */
@Slf4j
@Component
public class AutoStopTask3 {
    private int count;

    //@Scheduled(cron = "*/3 * * * * *")
    public void printTask() {
        log.info("hhm任务3，currentThread {}任务执行次数：{}", Thread.currentThread(), count + 1);
        count++;
    }
}
