package com.hhm.myjob;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.util.ErrorHandler;

import java.time.Clock;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author huanghm
 * @Date 2022/5/17
 */
public class ScheduledThreadPoolExecutorTest {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.AbortPolicy());
        Trigger trigger = new CronTrigger("*/3 * * * * *");
        Clock clock = Clock.systemDefaultZone();
        ErrorHandler errorHandler = TaskUtils.getDefaultErrorHandler(true);
        // 启动
        ScheduledFuture<?> schedule = (new MyRescheduleRunnable(new MyRunnable(), trigger, clock, executor, errorHandler)).schedule();
        // 停止
        boolean cancel = schedule.cancel(true);
        System.out.println(cancel);
    }

    private static class MyRunnable implements Runnable{
        long count = 0;
        @Override
        public void run() {
            System.out.println("hello," + (count++));
        }
    }

}
