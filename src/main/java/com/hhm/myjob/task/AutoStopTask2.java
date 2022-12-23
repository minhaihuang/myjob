package com.hhm.myjob.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author huanghm
 * @Date 2022/5/12
 */
@Component
@Slf4j
public class AutoStopTask2 {

    private int count;

    public void printTask() {
        log.info("hhm任务2，currentThread {}任务执行次数：{}", Thread.currentThread(), count + 1);
        count++;
    }
}
