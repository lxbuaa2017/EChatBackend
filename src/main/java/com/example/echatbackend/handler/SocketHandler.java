package com.example.echatbackend.handler;

import com.alibaba.fastjson.JSONObject;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.echatbackend.dao.ConversationRepository;
import com.example.echatbackend.dao.GroupUserRepository;
import com.example.echatbackend.dao.MessageRepository;
import com.example.echatbackend.dao.UserRepository;
import com.example.echatbackend.entity.*;
import com.example.echatbackend.service.ConversationService;
import com.example.echatbackend.service.FriendService;
import com.example.echatbackend.service.GroupService;
import com.example.echatbackend.service.MessageService;
import com.example.echatbackend.util.BeanUtils;
import com.example.echatbackend.util.ConstValue;
import com.example.echatbackend.util.EncodeUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * SocketHandler
 */
@Component
@Data
/*

这个handler可以算作表现层，这里本应只依赖业务层(即service)，但是目前持久层和业务层混在一起了，等一个有缘人把repository都干掉，全部换成service（逃

 */
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

    private GroupService groupService;

    private FriendService friendService;
    /**
     * socketIOServer
     */
    private final SocketIOServer socketIOServer;
    private GroupUserRepository groupUserRepository;

    public static Map<String, UUID> getClientMap() {
        return clientMap;
    }

    //规范化的话还是把repository都写到service去
    @Autowired
    public SocketHandler(SocketIOServer socketIOServer, MessageRepository messageRepository, UserRepository userRepository,
                         ConversationRepository conversationRepository, ConversationService conversationService,
                         MessageService messageService, GroupService groupService, GroupUserRepository groupUserRepository
            , FriendService friendService) {
        this.socketIOServer = socketIOServer;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.groupService = groupService;
        this.groupUserRepository = groupUserRepository;
        this.friendService = friendService;
    }

    /**
     * 当客户端发起连接时调用
     *
     * @return void
     */

    @OnConnect
    public void onConnect(SocketIOClient client) {
        logger.info("socket:onConnect");
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
        logger.info("socket:onDisConnect");
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
    public void join(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        String name = itemJSONObj.getString("name");
        logger.info("socket:join:"+name);
        String conversationId = itemJSONObj.getString("conversationId");
        Conversation conversation = conversationRepository.findByConversationId(conversationId);
        if (conversation == null) {//如果不存在，说明是初次登录的系统会话
            conversation = new Conversation();
            conversation.setType("friend");
            User userM = userRepository.findByUserName(name);
            User echat = userRepository.getOne(3);
            conversation.getUsers().add(userM);
            conversation.getUsers().add(echat);
            conversation.setConversationId(conversationId);
            conversationRepository.save(conversation);
        }
        logger.info(name + " 加入连接，对话id：" + conversationId);
        if (!clientMap.containsKey(name))
            clientMap.put(name, socketIOClient.getSessionId());
        socketIOClient.set("name", name);
        socketIOClient.joinRoom(conversationId);
        Map<String, UUID> onlineUsers = conversationService.getOnlineUser(conversationId);
        socketIOServer.getRoomOperations(conversationId).sendEvent("joined", onlineUsers);
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
    public void leave(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        String name = itemJSONObj.getString("name");
        logger.info("socket:leave:"+name);
        String conversationId = itemJSONObj.getString("conversationId");
        clientMap.remove(name);
        socketIOClient.leaveRoom(conversationId);
        Map<String, UUID> onlineUsers = conversationService.getOnlineUser(conversationId);
        socketIOServer.getRoomOperations(conversationId).sendEvent("leaved", onlineUsers);
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
    public void mes(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
       logger.info("socket:mes");
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
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        String userName = itemJSONObj.getString("name");
        String conversationId = itemJSONObj.getString("conversationId");
        List<String> readList = (List<String>) itemJSONObj.get("read");
        List<User> readUserList = new ArrayList<>();
        for (String name : readList) {
            readUserList.add(userRepository.findByUserName(name));
        }
//        Long time = (Long) itemJSONObj.get("time");
//        String message = EncodeUtil.toUTF8(itemJSONObj.getString("mes"));
        String message = itemJSONObj.getString("mes");
        String messageType = itemJSONObj.getString("style");
        User userM = userRepository.findByUserName(userName);
        Message messageObj = new Message(userM, conversationId, readUserList, message, messageType);
        logger.info(messageObj.toString());
        messageRepository.save(messageObj);
        socketIOServer.getRoomOperations(conversationId).sendEvent("mes", messageObj.show());
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
    public void getHistoryMessages(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) {

        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        String conversationId = itemJSONObj.getString("conversationId");
        logger.info("socket:getHistoryMessages:"+conversationId);
        int offset = Integer.parseInt(itemJSONObj.getString("offset"));
        int limit = Integer.parseInt(itemJSONObj.getString("limit"));
        List<Message> res =  messageService.getMoreMessage(conversationId,offset-1,limit,-1);
//        List<Message> res = messageService.findAllConversationMessage(conversationId);
        JSONObject[] jsonObjects = res.stream().map(Message::show).toArray(JSONObject[]::new);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", res.stream().map(Message::show).toArray(JSONObject[]::new));
        for (JSONObject each : jsonObjects) {
            logger.info(each.toJSONString());
        }
        socketIOClient.sendEvent("getHistoryMessages", jsonObject.get("data"));
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
    public void getSystemMessages(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) {
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        String conversationId = itemJSONObj.getString("conversationId");
        logger.info("socket:getSystemMessages:"+conversationId);
        int offset = Integer.parseInt(itemJSONObj.getString("offset"));
        int limit = Integer.parseInt(itemJSONObj.getString("limit"));
        List<Message> res =  messageService.getMoreMessage(conversationId,offset-1,limit,-1);
//        List<Message> res = messageService.findAllConversationMessage(conversationId);
        JSONObject[] jsonObjects = res.stream().map(Message::show).toArray(JSONObject[]::new);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", res.stream().map(Message::show).toArray(JSONObject[]::new));
        for (JSONObject each : jsonObjects) {
            logger.info(each.toJSONString());
        }
        socketIOClient.sendEvent("getSystemMessages", jsonObject.get("data"));

    }

    /*
    sendValidate（加群申请）
    {
      name: this.user.name, //发信人用户名
      mes: this.introduce, //发信人介绍
      time: utils.formatTime(new Date()), //发信时间
      avatar: this.user.photo, //发信头像
      nickname: this.user.nickname, //发信人昵称
      signature: this.user.signature, //发信人签名
      groupName: group.groupName, //群名
      groupId: group.groupId, //群id
      groupPhoto: group.groupPhoto, //群头像
      userM: this.user.id, // 申请人id
      read: [], //已读人id列表
     conversationId: this.$route.params.id + '-' + this.Vchat.id.split('-')[1],
      state: 'group',
      type: 'validate',
      status: '0'
    }

     */
    @OnEvent("agreeValidate")
    public void agreeValidate(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        logger.info("socket:agreeValidate");
        logger.info(itemJSONObj.toJSONString());
        String state = itemJSONObj.getString("state");
        String name = itemJSONObj.getString("name");
        String conversationId = itemJSONObj.getString("conversationId");//这个是发送者与echat的会话
        if (state.equals("group")) {
            String groupName = itemJSONObj.getString("groupName");
            Integer userId = Integer.valueOf(itemJSONObj.getString("userM"));
            Integer groupId = Integer.valueOf(itemJSONObj.getString("groupId"));
            if (groupService.isUserInGroup(groupId, userId)) {
                logger.info(name + " 已在群 " + groupId.toString() + " 中");
            } else {
                Group group = groupService.findGroupById(groupId);
                User user = userRepository.findById(userId).get();
                GroupUser groupUser = new GroupUser(group, user, false, false, group.getDescription());
                groupUserRepository.save(groupUser);
                logger.info(name + " 已经成功加入群 " + groupId.toString());
                //将申请信息设为已读
                User userM = userRepository.findByUserName(name);
                List<Message> message = messageRepository.findMessageByConversationIdAndUserM(conversationId, userM);
                for (Message each : message) {
                    each.setStatus("1");
                    messageRepository.save(each);
                }

                //通知申请人已同意
                Message agree_message = new Message();
                agree_message.setMessage("加入" + groupName + "的申请已通过");
                agree_message.setStatus("1");
                agree_message.setState("group");
                agree_message.setType("info");
                agree_message.setUserM(user);
                agree_message.setConversationId(conversationId);
                messageRepository.save(agree_message);

                Conversation conversation = conversationRepository.findByConversationId(groupId.toString());
                List<User> userList = conversation.getUsers();//软复制
                if (!userList.contains(user))
                    userList.add(user);
                conversationRepository.save(conversation);
                userM.getConversationList().add(conversation);
                userRepository.save(userM);

                socketIOServer.getRoomOperations(conversationId).sendEvent("takeValidate", agree_message.show());

                //通知群聊有新人加入
                Message org_message = new Message();
                org_message.setType("org");
                org_message.setUserM(user);
                org_message.setConversationId(groupId.toString());
                messageRepository.save(org_message);
                socketIOServer.getRoomOperations(groupId.toString()).sendEvent("org", org_message.show());
            }
        }
    /*
    sendValidate（好友申请）
{
    name: this.user.name, //发信人用户名
    mes: this.introduce, //发信人介绍
    time: utils.formatTime(new Date()), //发信时间
    avatar: this.user.photo, //发信人头像
    nickname: this.user.nickname, //发信人昵称
    signature: this.user.signature, //发信人签名
    read: [], //已读人id列表
    userM: this.user.id, //发信人id
    userY: this.$route.params.id, //对方的id
    userYname: friend.userYname, //对方的昵称
    userYphoto: friend.userYphoto, //对方的照片
    userYloginName: friend.userYloginName, //对方的登录名
    friendRoom : this.user.id + '-' + this.$route.params.id, //这次发信的会话id
    conversationId: this.$route.params.id + '-' + this.Vchat.id.split('-')[1], //这次发信的会话id，上面friendroom颠倒一下
    state: 'friend',
    type: 'validate',
    status: '0'
}

     */
        else if (state.equals("friend")) {
            //如果已经是好友，就不处理。否则就加入好友列表
            int userMId = Integer.parseInt(itemJSONObj.getString("userM"));
            int userYId = Integer.parseInt(itemJSONObj.getString("userY"));
            String userYName = itemJSONObj.getString("userYname");
            Optional<User> optionalUser = userRepository.findById(userMId);
            if (optionalUser.isEmpty()) {
                return;
            }
            if (!friendService.checkFriend(optionalUser.get(), userYId)) {
                friendService.addFriend(userMId, userYId);
            }
            //将申请信息设为已读
            User userM = userRepository.findByUserName(name);
            User userY = userRepository.findByUserName(userYName);
            List<Message> messages = messageRepository.findMessageByConversationIdAndUserM(conversationId, userM);
            for (Message message : messages) {
                message.setStatus("1");
                messageRepository.save(message);
            }

            //通知申请人已同意
            Message agree_message = new Message();
            agree_message.setMessage( name+ " 向 "+userYName+" 发送的好友申请已通过");
            agree_message.setStatus("1");
            agree_message.setState("friend");
            agree_message.setType("info");
            agree_message.setUserM(userM);
            agree_message.setUserY(userY);
            agree_message.setConversationId(conversationId);
            Message agree_message1 = new Message();
            BeanUtils.copyProperties(agree_message,agree_message1);
            agree_message1.setId(null);
            agree_message1.setConversationId(userMId+"-"+ConstValue.ECHAT_ID);
            messageRepository.save(agree_message);
            messageRepository.save(agree_message1);
            if(userMId<userYId){
                conversationId = userMId+"-"+userYId;
            }
            else {
                conversationId = userYId+"-"+userMId;
            }

            Conversation conversation = new Conversation();
            conversation.setConversationId(conversationId);
            conversation.setType("friend");
            conversation.getUsers().add(userM);
            conversation.getUsers().add(userY);
            conversationRepository.save(conversation);
            userM.getConversationList().add(conversation);
            userY.getConversationList().add(conversation);
            userRepository.save(userM);
            userRepository.save(userY);
            socketIOServer.getRoomOperations(userMId+"-"+ConstValue.ECHAT_ID).sendEvent("takeValidate", agree_message.show());
            socketIOClient.sendEvent("ValidateSuccess", "ok");
        }
    }


    @OnEvent("refuseValidate")
    public void refuseValidate(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
        logger.info("socket:refuseValidate");
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        Integer userMId = Integer.valueOf(itemJSONObj.getString("userM"));
        String name = itemJSONObj.getString("name");
        String conversationId = userMId+"-"+ConstValue.ECHAT_ID;
        //将申请信息设为已读
        User userM = userRepository.getOne(userMId);
        String userYName = itemJSONObj.getString("userYname");
        User userY = userRepository.findByUserName(userYName);
        List<Message> messages = messageRepository.findMessageByConversationIdAndUserM(conversationId, userM);
        for (Message message : messages) {
            message.setStatus("2");//状态设为拒绝
            messageRepository.save(message);
        }
        String state = itemJSONObj.getString("state");
        if (state.equals("group")) {
            String groupName = itemJSONObj.getString("groupName");
            Integer groupId = Integer.valueOf(itemJSONObj.getString("groupId"));
            //通知申请人已拒绝
            Message refuse_message = new Message();
            refuse_message.setMessage(userYName + "拒绝你加入" + groupName + "的申请!");
            refuse_message.setStatus("-1");
            refuse_message.setState("group");
            refuse_message.setType("info");
            refuse_message.setUserM(userY);
            refuse_message.setConversationId(conversationId);
            messageRepository.save(refuse_message);
            socketIOServer.getRoomOperations(conversationId).sendEvent("takeValidate", refuse_message.show());

        } else if (state.equals("friend")) {
            Message refuse_message = new Message();
            refuse_message.setMessage(userYName + "拒绝了你的好友申请!");
            refuse_message.setStatus("-1");
            refuse_message.setState("friend");
            refuse_message.setType("info");
            refuse_message.setUserM(userY);
            refuse_message.setConversationId(conversationId);
            messageRepository.save(refuse_message);
            socketIOServer.getRoomOperations(conversationId).sendEvent("takeValidate", refuse_message.show());
        }
    }


    @OnEvent("setReadStatus")
    public void setReadStatus(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
        logger.info("socket:setReadStatus");
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        logger.info(toJSONString(messageDto));
        String userName = itemJSONObj.getString("name");
        User user = userRepository.findByUserName(userName);
        String conversationId = itemJSONObj.getString("conversationId");
        List<Message> messages = messageService.findAllConversationMessage(conversationId);
        for (Message message : messages) {
            List<User> readList = message.getReadList();
            if (!readList.contains(user)) {
                readList.add(user);
                messageRepository.save(message);
            }
        }

    }

    /*
    sendValidate（好友申请）
    {
        name: this.user.name, //发信人用户名
        mes: this.introduce, //发信人介绍
        time: utils.formatTime(new Date()), //发信时间
        avatar: this.user.photo, //发信人头像
        nickname: this.user.nickname, //发信人昵称
        signature: this.user.signature, //发信人签名
        read: [], //已读人id列表
        userM: this.user.id, //发信人id
        userY: this.$route.params.id, //对方的id
        userYname: friend.userYname, //对方的昵称
        userYphoto: friend.userYphoto, //对方的照片
        userYloginName: friend.userYloginName, //对方的登录名
        friendRoom : this.user.id + '-' + this.$route.params.id, //这次发信的会话id
        conversationId: this.$route.params.id + '-' + this.Vchat.id.split('-')[1], //这次发信的会话id，上面friendroom颠倒一下
        state: 'friend',
        type: 'validate',
        status: '0'
    }

     */
/*
sendValidate（加群申请）
{
  name: this.user.name, //发信人用户名
  mes: this.introduce, //发信人介绍
  time: utils.formatTime(new Date()), //发信时间
  avatar: this.user.photo, //发信头像
  nickname: this.user.nickname, //发信人昵称
  signature: this.user.signature, //发信人签名
  groupName: group.groupName, //群名
  groupId: group.groupId, //群id
  groupPhoto: group.groupPhoto, //群头像
  userM: this.user.id, // 申请人id
  read: [], //已读人id列表
 conversationId: this.$route.params.id + '-' + this.Vchat.id.split('-')[1],
  state: 'group',
  type: 'validate',
  status: '0'
}

 */
    @OnEvent("sendValidate")
    public void sendValidate(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) throws UnsupportedEncodingException {
        logger.info("socket:sendValidate");
        JSONObject itemJSONObj = JSONObject.parseObject(toJSONString(messageDto));
        Message message = new Message();
        String state = itemJSONObj.getString("state");
        String mes = itemJSONObj.getString("mes");
        String conversationId = itemJSONObj.getString("conversationId");
        Integer userMId = Integer.valueOf(itemJSONObj.getString("userM"));
        User userM = userRepository.getOne(userMId);
        message.setState(state);
        message.setUserM(userM);
        message.setType("validate");
        message.setStatus("0");
        message.setMessage(mes);
        message.setConversationId(conversationId);
        String userMAndSystemConversationId = "";
        if (state.equals("group")) {
            Integer groupId = Integer.valueOf(itemJSONObj.getString("groupId"));
            Group group = groupService.findGroupById(groupId);
            message.setGroup(group);
            Integer holderId = group.getUser().getId();
        } else if (state.equals("friend")) {
            Integer userYId = Integer.valueOf(itemJSONObj.getString("userY"));
            User userY = userRepository.getOne(userYId);
            message.setUserY(userY);
        }
        userMAndSystemConversationId = userMId+"-"+ ConstValue.ECHAT_ID;
        Message message1 =new Message();
        BeanUtils.copyProperties(message,message1);
        message1.setConversationId(userMAndSystemConversationId);
        messageRepository.save(message);
        messageRepository.save(message1);
        socketIOServer.getRoomOperations(conversationId).sendEvent("takeValidate", message.show());
    }

    @OnEvent("disconnect")
    public void disconnect(SocketIOClient socketIOClient, AckRequest ackRequest, @RequestBody Object messageDto) {
        logger.info("socket:disconnect");
        UUID uuid = socketIOClient.getSessionId();
        while (clientMap.values().remove(uuid)) ;
        socketIOServer.getBroadcastOperations().sendEvent("leaved", clientMap);
        logger.info("用户下线，uuid:" + uuid.toString());
    }
}