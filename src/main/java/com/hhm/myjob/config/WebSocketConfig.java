package com.hhm.myjob.config;

import com.hhm.myjob.dto.LoggerMessage;
import com.hhm.myjob.filter.LoggerQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author huanghm
 * @Date 2022/5/25
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker // 注解开启STOMP协议来传输基于代理的消息，此时控制器支持使用
@MessageMapping
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    private SimpMessagingTemplate messagingTemplate;

    //配置WebSocket消息代理端点，即stomp服务端;spring boot自带的webSocket模块提供stomp的服务端
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 将clientMessage注册为STOMP的一个端点
        // 客户端在订阅或发布消息到目的路径前，要连接该端点
        // setAllowedOrigins允许所有域连接，否则浏览器可能报403错误
        registry.addEndpoint("/websocket").setAllowedOriginPatterns("*").addInterceptors().withSockJS(); //
    }

    /**
     * 推送日志到/topic/pullLogger
     */
    @PostConstruct
    public void pushLogger(){
        ExecutorService executorService= Executors.newFixedThreadPool(2);
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        LoggerMessage log = LoggerQueue.getInstance().poll();
                        if(log!=null){
                            if(messagingTemplate!=null)
                                //服务端发送
                                messagingTemplate.convertAndSend("/topic/pullFileLogger",log);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        executorService.submit(runnable);
    }

    public SimpMessagingTemplate getMessagingTemplate() {
        return messagingTemplate;
    }

    public void setMessagingTemplate(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
}
