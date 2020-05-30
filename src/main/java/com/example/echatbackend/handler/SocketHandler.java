package com.example.echatbackend.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.*;
import com.example.echatbackend.dao.ConversationRepository;
import com.example.echatbackend.dao.MessageRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.Conversation;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.ConversationService;
import com.example.echatbackend.service.MessageService;
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
    private static Map<String, UUID> clientMap = new ConcurrentHashMap<>(16);

    private MessageRepository messageRepository;

    private UserRepository userRepository;

    private ConversationRepository conversationRepository;

    private ConversationService conversationService;

    private MessageService messageService;
    /**
     * socketIOServer
     */
    private final SocketIOServer socketIOServer;

    public static Map<String, UUID> getClientMap() {
        return clientMap;
    }
//规范化的话还是把repository都写到service去
    @Autowired
    public SocketHandler(SocketIOServer socketIOServer, MessageRepository messageRepository, UserRepository userRepository,
                         ConversationRepository conversationRepository, ConversationService conversationService,
                         MessageService messageService) {
        this.socketIOServer = socketIOServer;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationService = conversationService;
        this.messageService = messageService;
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
//            this.sendMsg(null, null,
//                    "下线啦");
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
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        String name = EncodeUtil.toUTF8(itemJSONObj.getString("name"));
        String conversationId = EncodeUtil.toUTF8(itemJSONObj.getString("conversationId"));
        logger.info(name+" 加入连接，对话id："+conversationId);
        if(!clientMap.containsKey(name))
            clientMap.put(name,socketIOClient.getSessionId());
        socketIOClient.set("name",name);
        socketIOClient.joinRoom(conversationId);
        Map<String,UUID> onlineUsers = conversationService.getOnlineUser(Integer.valueOf(conversationId));
        socketIOServer.getRoomOperations(conversationId.toString()).sendEvent("joined",onlineUsers);
    }

    /*
        socket.on('leave', (val) => {
        delete OnlineUser[val.name];
        console.log('leave', OnlineUser);
        socket.leave(val.roomid, () => {
            socket.to(val.roomid).emit('leaved', OnlineUser);
            // console.log('leave', val.roomid, OnlineUser);
        });
    });

            leave（离开会话）
        {
            name: this.user.name, //发信人用户名
            time: utils.formatTime(new Date()), //发信时间
            avatar: this.user.photo, //发信人照片
            conversationId: v.id //会话id
        }

     */
//虽然这里只是离开单个房间，但是前端会使用循环调用，会退出所有房间

    @OnEvent("leave")
    public void leave(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto) throws UnsupportedEncodingException {
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        String name = EncodeUtil.toUTF8(itemJSONObj.getString("name"));
        String conversationId = itemJSONObj.getString("conversationId");
        clientMap.remove(name);
        socketIOClient.leaveRoom(conversationId);
        Map<String,UUID> onlineUsers = conversationService.getOnlineUser(Integer.valueOf(conversationId));
        socketIOServer.getRoomOperations(conversationId).sendEvent("leaved",onlineUsers);
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

    /*
        socket.on('getHistoryMessages', (pramas) => { // 获取历史消息
        apiList.getHistoryMessages(pramas, 1, (res) => { // 1 正序
            if (res.code === 0) {
                socket.emit('getHistoryMessages', res.data); // 发送给发送者（当前客户端）
            } else {
                console.log('查询历史记录失败');
            }
        });
    });

    {
    conversationId: v.id,  //会话的id
    offset: 1,  //页，从1开始计数
    limit: 200 //每页的记录上限
}
     */
    @OnEvent("getHistoryMessages")
    public void getHistoryMessages(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto){
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        int conversationId = Integer.parseInt(itemJSONObj.getString("conversationId"));
        int offset = Integer.parseInt(itemJSONObj.getString("offset"));
        int limit = Integer.parseInt(itemJSONObj.getString("limit"));
        List<Message> res =  messageService.getMoreMessage(conversationId,offset,limit,1);
        socketIOClient.sendEvent("getHistoryMessages",res);
    }

    /*
        socket.on('getSystemMessages', (pramas) => { // 获取历史消息
        apiList.getHistoryMessages(pramas, -1, (res) => { // -1 倒序
            if (res.code === 0) {
                socket.emit('getSystemMessages', res.data); // 发送给发送者（当前客户端）
            } else {
                console.log('查询vchat历史记录失败');
            }
        });
    });
     */
    @OnEvent("getSystemMessages")
    public void getSystemMessages(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto){
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        int conversationId = Integer.parseInt(itemJSONObj.getString("conversationId"));
        int offset = Integer.parseInt(itemJSONObj.getString("offset"));
        int limit = Integer.parseInt(itemJSONObj.getString("limit"));
        List<Message> res =  messageService.getMoreMessage(conversationId,offset,limit,-1);
        socketIOClient.sendEvent("getSystemMessages",res);
    }
/*
    socket.on('agreeValidate', (val) => { // 同意好友或加群申请
        if (val.state === 'group') { // 群聊验证
            apiList.InsertGroupUsers(val, r => {
                if (r.code === -1) {
                    console.log('加入群聊失败');
                } else if (r.code === -2) {
                    console.log('更新群成员数量失败');
                } else if (r.code === -3) {
                    console.log('群成员已存在');
                } else if (r.code === 0) {
                    let pr = {
                        status: '1',
                        userM: val['userM']
                    };
                    apiList.setMessageStatus(pr);
                    // 通知申请人验证已同意
                    let value = {
                        name: '',
                        mes: val.userYname + '同意你加入' + val.groupName + '!',
                        time: utils.formatTime(new Date()),
                        avatar: val.userYphoto,
                        nickname: val.userYname,
                        groupName: val.groupName,
                        groupId: val.groupId,
                        groupPhoto: val.groupPhoto,
                        read: [],
                        status: '1', // 同意
                        state: 'group',
                        type: 'info',
                        roomid: val.userM + '-' + val.roomid.split('-')[1]
                    };
                    apiList.saveMessage(value); // 保存通知消息
                    let params = {
                        name: val.groupName,
                        photo: val.groupPhoto,
                        id: val.groupId,
                        type: 'group'
                    };
                    apiList.ServeraddConversitionList(val.name, params, () => {
                        socket.to(value.roomid).emit('takeValidate', value);
                        // 通知群聊
                        let org = {
                            type: 'org',
                            nickname: val.nickname,
                            time: utils.formatTime(new Date()),
                            roomid: val.groupId
                        };
                        apiList.saveMessage(org); // 保存通知消息
                        socket.to(org.roomid).emit('org', org);
                    }); // 添加到申请人会话列表
                }
            });
        } else if (val.state === 'friend') { // 写入好友表
            apiList.addFriend(val, r => {
                if (r.code === 0) {
                    let pr = {
                        status: '1',
                        userM: val['userM']
                    };
                    apiList.setMessageStatus(pr);
                    // 通知申请人验证已同意
                    let value = {
                        name: '',
                        mes: val.userYname + '同意了你的好友请求！',
                        time: utils.formatTime(new Date()),
                        avatar: val.userYphoto,
                        nickname: val.userYname,
                        read: [],
                        state: 'friend',
                        type: 'info',
                        status: '1', // 同意
                        roomid: val.userM + '-' + val.roomid.split('-')[1]
                    };
                    apiList.saveMessage(value); // 保存通知消息
                    let userMparams = { // 申请人信息
                        name: val.nickname,
                        photo: val.avatar,
                        id: val.friendRoom,
                        type: 'friend'
                    };
                    let userYparams = { // 好友信息
                        name: val.userYname,
                        photo: val.userYphoto,
                        id: val.friendRoom,
                        type: 'friend'
                    };
                    apiList.ServeraddConversitionList(val.name, userYparams, () => {
                        apiList.ServeraddConversitionList(val.userYloginName, userMparams, () => {
                            socket.to(value.roomid).emit('takeValidate', value);
                            socket.emit('ValidateSuccess', 'ok');
                        }); // 添加到自己会话列表
                    }); // 添加到申请人会话列表
                }else {
                    console.log('添加好友失败');
                }
            });
        }
    });


 */
    @OnEvent("agreeValidate")
    public void agreeValidate(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto){
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        String state = itemJSONObj.getString("state");
        if(state.equals("group")){
            /*
            todo
             */
        }
        else if(state.equals("friend")){
            /*
            todo
             */
        }
    }

    @OnEvent("setReadStatus")
    public void setReadStatus(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto) {
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        /*
        todo
         */
    }

    @OnEvent("sendValidate")
    public void sendValidate(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto) {
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        /*
        todo
         */
    }

    @OnEvent("disconnect")
    public void disconnect(SocketIOClient socketIOClient, AckRequest ackRequest,@RequestBody Object  messageDto) {
        JSONObject itemJSONObj = JSONObject.parseObject(JSON.toJSONString(messageDto));
        /*
        todo
         */
    }
}