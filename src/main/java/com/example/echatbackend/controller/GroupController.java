package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Group;
import com.example.echatbackend.entity.User;
import com.example.echatbackend.service.GroupService;
import com.example.echatbackend.service.TokenService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
public class GroupController extends BaseController {

    private final GroupService groupService;
    private final TokenService tokenService;

    @Autowired
    public GroupController(GroupService groupService, TokenService tokenService) {
        this.groupService = groupService;
        this.tokenService = tokenService;
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
        groupService.saveAndFlush(group);
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
        response.put("data", Arrays.stream(users).map(User::show).toArray(JSONObject[]::new));
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
}
