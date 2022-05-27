package com.hhm.myjob.dto;

import lombok.Data;

/**
 * @Author: huanghm
 * @Date: 2022/05/14
 * @Description:
 */
@Data
public class TaskDto {
    private String taskName;
    private String taskClass;
    private String taskMethod;
    private String cron;
    private int threadNum;
    private int status = 0;
}
