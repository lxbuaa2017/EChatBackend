package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.dao.ConversationRepository;
import com.example.echatbackend.entity.Conversation;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.GroupUser;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
public class GroupController extends BaseController {

    private final GroupService groupService;
    private final TokenService tokenService;
    private final ConversationService  conversationService;
    private final UserService userService;
    private final GroupUserService groupUserService;
    @Autowired
    public GroupController(GroupService groupService, TokenService tokenService,
                           ConversationService conversationService,UserService userService
        ,GroupUserService groupUserService) {
        this.groupService = groupService;
        this.tokenService = tokenService;
        this.conversationService=conversationService;
        this.userService = userService;
        this.groupUserService = groupUserService;
    }

    // 创建群
    @PostMapping("/group/createGroup")
    public ResponseEntity<Object> createGroup(@RequestBody JSONObject request) {
        String groupName = request.getString("groupName");
        String groupDesc = request.getString("groupDesc");
        String groupImage = request.getString("groupAvatar");
        User user = tokenService.getCurrentUser();
        if (groupName == null || groupDesc == null || groupImage == null) {
            return requestFail(-1, "参数错误");
        }
        Group group = new Group();
        group.setName(groupName);
        group.setDescription(groupDesc);
        group.setAvatar(groupImage);
        group.setUser(user);
        group = groupService.saveAndFlush(group);
        Conversation conversation = new Conversation();
        conversation.setGroup(group);
        conversation.setConversationId(group.getId().toString());
        conversation.setType("group");
        List<User> userList = new ArrayList<>();
        userList.add(user);
        conversation.setUsers(userList);
        conversation = conversationService.saveAndFlush(conversation);
        user.getConversationList().add(conversation);
        userService.saveAndFlush(user);
        group.setConversation(conversation);
        groupService.saveAndFlush(group);
        GroupUser groupUser = new GroupUser(group,user,true,"");
        groupUserService.saveAndFlush(groupUser);
        JSONObject response = new JSONObject();
        response.put("id", group.getId());
        return requestSuccess(response);
        //创建成功
        /*{
            code:0//成功创建
            id:number //群聊id
        }*/
        //创建失败
        /*{
            code:-1//失败
            msg:string//失败信息
        }*/
    }



    // 查找我的群
    @GetMapping("/group/getMyGroup")
    public ResponseEntity<Object> getMyGroup() {
        /*res
            {
            groupId:number //群id
            groupName:string//群名
            groupDesc string//群简介
            groupImage: string//群头像
            holderName: string//群主
            }
        */
        User user = tokenService.getCurrentUser();
        Group[] groups = groupService.findGroupByUser(user);
        JSONObject response = new JSONObject();
        response.put("data", Arrays.stream(groups).map(Group::show).toArray(JSONObject[]::new));
        return requestSuccess(response);
    }

    // 查找群内成员
    @GetMapping("/group/getGroupUser")
    public ResponseEntity<Object> getGroupUser(Integer id, String keyword) {
        /*res
            [{
            userId: number//用户id
            name: string//用户名
            photo: string//用户头像
             signature: string, //签名
            },
            {…
            }]
        */
        if (id == null || keyword == null) {
            return requestFail(-1, "参数错误");
        }
        Group group = groupService.findGroupById(id);
        if (group == null) {
            return requestFail(-1, "群组不存在");
        }
        User[] users = groupService.findUserByGroup(group);
        JSONObject response = new JSONObject();
        response.put("data", Arrays.stream(users).map(User::showWithOnlineStatus).toArray(JSONObject[]::new));
        return requestSuccess(response);
    }

    // 查找群基本信息
    @PostMapping("/group/searchGroup")
    public ResponseEntity<Object> searchGroup(@RequestBody JSONObject request) {
        String keyword = request.getString("keyword");
        Integer offset = request.getInteger("offset");
        Integer limit = request.getInteger("limit");
        Integer type = request.getInteger("type");
        List<Group> groupList;
        if (keyword == null || offset == null || limit == null || type == null) {
            return requestFail(-1, "参数错误");
        }
        keyword = keyword.trim();
        if (type == 1) {
            groupList = groupService.searchGroupByCode(keyword, offset - 1, limit);
        } else if (type == 2) {
            groupList = groupService.searchGroupByName(keyword, offset - 1, limit);
        } else {
            return requestFail(-1, "参数错误");
        }
        JSONObject response = new JSONObject();
        response.put("data", groupList.stream().map(Group::show).toArray());
        return requestSuccess(response);
    }

    //根据id获取群信息
    @GetMapping("/group/getGroupInfo")
    public ResponseEntity<Object> getGroupInfo(@RequestParam Integer groupId){
        Group group = groupService.findGroupById(groupId);
        JSONObject response = new JSONObject();
        response.put("data", group.show());
        return requestSuccess(response);
    }

    //判断当前用户是否加入群聊
    @GetMapping("/group/checkIfInGroup")
    public ResponseEntity<Object> checkIfInGroup(@RequestParam Integer groupId){
        User user = tokenService.getCurrentUser();
        Group group = groupService.findGroupById(groupId);
        boolean res = groupUserService.checkIfInGroup(group,user);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("isMygroup", res);
        return requestSuccess(jsonObject);
    }
}
