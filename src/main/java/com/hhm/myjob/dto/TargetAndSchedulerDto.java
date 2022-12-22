package com.hhm.myjob.dto;

import com.hhm.myjob.scheduler.CustomTaskScheduler;
import lombok.Data;

/**
 * @Author huanghm
 * @Date 2022/5/25
 */
@Data
public class TargetAndSchedulerDto {
    // 基本信息
    private String method;
    private String className;
    private String taskName;
    private String cron;
    private int threadNum;
    private int status = 0;

    // 对象信息
    private Object target;

    // 任务调度器
    private CustomTaskScheduler customTaskScheduler;
}
