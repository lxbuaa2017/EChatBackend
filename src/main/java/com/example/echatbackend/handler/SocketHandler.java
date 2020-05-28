package com.example.echatbackend.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.*;
import com.example.echatbackend.dao.MessageRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.util.EncodeUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SocketHandler
 *
 */
@Component
@Data
public class SocketHandler {

    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(SocketHandler.class);
    /**
     * ConcurrentHashMap保存当前SocketServer用户ID对应关系
     */
    private Map<String, UUID> clientMap = new ConcurrentHashMap<>(16);

    private MessageRepository messageRepository;

    private UserRepository userRepository;
    /**
     * socketIOServer
     */
    private final SocketIOServer socketIOServer;

    @Autowired
    public SocketHandler(SocketIOServer socketIOServer,MessageRepository messageRepository,UserRepository userRepository) {
        this.socketIOServer = socketIOServer;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    /**
     * 当客户端发起连接时调用
     *
     * @return void

     */

    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client != null) {
            logger.info(String.valueOf(client.getHandshakeData().getUrlParams()));
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
        //            String imei = client.getHandshakeData().getSingleUrlParam("imei");
//            String applicationId = client.getHandshakeData().getSingleUrlParam("appid");
//            logger.info("连接成功, applicationId=" + applicationId + ", imei=" + imei +
//                    ", sessionId=" + client.getSessionId().toString());
//            client.joinRoom(applicationId);
        // 更新POS监控状态为在线
        /*
        io.in(val.roomid).emit('joined', OnlineUser);
         */
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        String name = EncodeUtil.toUTF8(itemJSONObj.getString("name"));
        String conversationId = EncodeUtil.toUTF8(itemJSONObj.getString("conversationId"));
        logger.info(name+" 加入连接，对话id："+conversationId);
        if(!clientMap.containsKey(name))
            clientMap.put(name,socketIOClient.getSessionId());
        socketIOClient.set("name",name);
        socketIOClient.joinRoom(conversationId);
        socketIOServer.getRoomOperations(conversationId.toString()).sendEvent("joined",clientMap);
    }

    /*
        socket.on('mes', (val) => { // 聊天消息
        apiList.saveMessage(val);
        console.log('OnlineUser', val.roomid);
        socket.to(val.roomid).emit('mes', val);
    });
    第一个mes是server监听，第二个是client监听（即写在了前端代码）
     */
    @OnEvent("mes")
    public void mes(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto) throws UnsupportedEncodingException {
        /*
        mes
        {
            name: this.user.name, //发信人用户名
            mes: this.message, //消息，string类型
            time: utils.formatTime(new Date()),//发信时间
            avatar: this.user.photo,//发信人头像
            nickname: this.user.nickname,//发信人昵称
            read: [this.user.name],//读过的人的姓名列表（不是id），发信时只有发信人读过
            conversationId: this.currSation.id,//会话id
            style: 'mess',//四种类型：'mess'/'emoji'/'img'/file
            userM: this.user.id //发信人id
        }

         */
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        String userName = EncodeUtil.toUTF8(itemJSONObj.getString("name"));
        Integer conversationId = Integer.valueOf(itemJSONObj.getString("conversationId"));
        List<String> readList = (List<String>) itemJSONObj.get("read");
        List<User> readUserList = new ArrayList<>();
        for(String name:readList){
            readUserList.add(userRepository.findByUserName(name));
        }
//        Long time = (Long) itemJSONObj.get("time");
        String message = EncodeUtil.toUTF8(itemJSONObj.getString("mes"));
        String messageType = itemJSONObj.getString("style");
        Message messageObj = new Message(userName,conversationId,readUserList,message,messageType);
        logger.info(messageObj.toString());
        messageRepository.save(messageObj);
        socketIOServer.getRoomOperations(conversationId.toString()).sendEvent("mes",messageDto);
    }
}

