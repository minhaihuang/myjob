package com.hhm.myjob.task;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author huanghm
 * @Date 2022/5/12
 */
@Slf4j
public class AutoStopTask {
    private int count;

    public void printTask() {
        log.info("hhm任务1，currentThread {}任务执行次数：{}", Thread.currentThread(), count + 1);
        count++;
    }
}
