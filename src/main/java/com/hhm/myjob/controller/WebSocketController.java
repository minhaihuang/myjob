package com.hhm.myjob.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @Author huanghm
 * @Date 2022/5/25
 */
@ServerEndpoint("/logUtil")
@Slf4j
@RestController
public class WebSocketController {

    private static Session session;

    @OnOpen
    public void onOpen(Session session) {
        WebSocketController.session = session;
    }

    @OnClose
    public void onClose() {

    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            sendMessage(message+"：成功收到");
        } catch (Exception e) {
            log.error("发送异常");
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
    }

    public static void sendMessage(String message) throws Exception {
        if (session!=null){
            if (WebSocketController.session.isOpen()) {
                WebSocketController.session.getBasicRemote().sendText(message);
            }
        }
    }
}
