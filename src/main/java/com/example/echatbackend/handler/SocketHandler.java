package com.example.echatbackend.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.*;
import com.example.echatbackend.util.EncodeUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SocketHandler
 *
 */
@Component
public class SocketHandler {

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(SocketHandler.class);

    /**
     * ConcurrentHashMap保存当前SocketServer用户ID对应关系
     */
    private Map<String, UUID> clientMap = new ConcurrentHashMap<>(16);

    public Map<String, UUID> getClientMap() {
        return clientMap;
    }

    public void setClientMap(Map<String, UUID> clientMap) {
        this.clientMap = clientMap;
    }

    /**
     * socketIOServer
     */
    private final SocketIOServer socketIOServer;

    @Autowired
    public SocketHandler(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;
    }

    /**
     * 当客户端发起连接时调用
     *
     * @param socketIOClient
     * @return void

     */

    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client != null) {
            logger.info(String.valueOf(client.getHandshakeData().getUrlParams()));
//            String imei = client.getHandshakeData().getSingleUrlParam("imei");
//            String applicationId = client.getHandshakeData().getSingleUrlParam("appid");
//            logger.info("连接成功, applicationId=" + applicationId + ", imei=" + imei +
//                    ", sessionId=" + client.getSessionId().toString());
//            client.joinRoom(applicationId);
            // 更新POS监控状态为在线
        } else {
            logger.error("客户端为空");
        }
    }
    /**
     * 客户端断开连接时调用，刷新客户端信息
     *
     * @param socketIOClient
     * @return void

     */
    @OnDisconnect
    public void onDisConnect(SocketIOClient socketIOClient) {
        String userName = socketIOClient.getHandshakeData().getSingleUrlParam("userName");
        if (StringUtils.isNotBlank(userName)) {
            logger.info("用户{}断开长连接通知, NettySocketSessionId: {}, NettySocketRemoteAddress: {}",
                    userName, socketIOClient.getSessionId().toString(), socketIOClient.getRemoteAddress().toString());
            // 移除
            clientMap.remove(userName);
            // 发送下线通知
            this.sendMsg(null, null,
                    "下线啦");
        }
    }

    /**
     * sendMsg发送消息事件
     *
     * @param socketIOClient
     * @param ackRequest
     * @param messageDto
     * @return void
     */
    @OnEvent("sendMsg")
    public void sendMsg(SocketIOClient socketIOClient, AckRequest ackRequest, Object messageDto) {
        if (messageDto != null) {
            // 全部发送
            clientMap.forEach((key, value) -> {
                if (value != null) {
                    socketIOServer.getClient(value).sendEvent("receiveMsg", messageDto);
                }
            });
        }
    }

    /*
        socket.on('join', (val) => {
        // if (OnlineUser[val.name]) {
        //     console.log('yijiaru', val.name);
        //     return;
        // }
        socket.join(val.roomid, () => {
            console.log('加入了', val.name);
            OnlineUser[val.name] = socket.id;
            io.in(val.roomid).emit('joined', OnlineUser); // 包括发送者
            // console.log('join', val.roomid, OnlineUser);
        });
    });
     */
    @OnEvent("join")
    public void join(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto) throws UnsupportedEncodingException {
//        LinkedHashMap jsonObject = (LinkedHashMap)messageDto;
        String encode = EncodeUtil.getEncoding(messageDto.toString());
        String objStr = EncodeUtil.toUTF8(messageDto.toString());
//        迷惑
        String utf8 = new String (objStr.getBytes ( "ISO8859-1" ), encode );
        logger.info(utf8);
    }
}

