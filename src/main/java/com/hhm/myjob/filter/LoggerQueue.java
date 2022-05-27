package com.hhm.myjob.filter;

import com.hhm.myjob.dto.LoggerMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author huanghm
 * @Date 2022/5/25
 */
public class LoggerQueue {
    //队列大小
    public static final int QUEUE_MAX_SIZE = 10000;
    private static LoggerQueue alarmMessageQueue = new LoggerQueue();
    //阻塞队列
    private BlockingQueue blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);

    private LoggerQueue() {
    }

    public static LoggerQueue getInstance() {
        return alarmMessageQueue;
    }

    /**
     * @Description: 消息入队
     * @Return: boolean
     * @Author: leijun
     * @Date: 2019/11/26
     **/
    public boolean push(LoggerMessage log) {
        //System.out.println("消息入队的信息===="+log);
        return this.blockingQueue.add(log);//队列满了就抛出异常，不阻塞
    }

    /**
     * @Description: 消息出队
     * @Return: com.unismc.springbootudcap.powersecurity.entity.LoggerMessage
     * @Author: leijun
     * @Date: 2019/11/26
     **/
    public LoggerMessage poll() {
        LoggerMessage result = null;
        try {
            //System.out.println("输出:"+this.blockingQueue);
            result = (LoggerMessage) this.blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }
}
