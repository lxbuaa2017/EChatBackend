package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class GroupController extends BaseController {
    // 创建群
    @PostMapping("/group/createGroup")
    public ResponseEntity<Object> createGroup(@NotNull @RequestBody JSONObject request) {
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
        return null;
    }

    // 查找我的群
    @PostMapping("/group/getMyGroup")
    public ResponseEntity<Object> getMyGroup(@NotNull @RequestBody JSONObject request) {
        /*res
            {
            groupId:number //群id
            groupName:string//群名
            groupDesc string//群简介
            groupImage: string//群头像
            holderName: string//群主
            }
        */
        return null;
    }

    // 查找群内成员
    @PostMapping("/group/getGroupUser")
    public ResponseEntity<Object> getGroupUser(@NotNull @RequestBody JSONObject request) {
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
        return null;
    }

    // 查找群基本信息
    @PostMapping("/group/searchGroup")
    public ResponseEntity<Object> searchGroup(@NotNull @RequestBody JSONObject request) {
        /*[{
            groupId:number //群id
            groupName:string//群名
            groupDesc string//群简介
            groupImage: string//群头像
            holderName: string//群主
            },
            {…
            }]
        */
        return null;
    }

    // 群详细信息
    @PostMapping("/group/getGroupDetailed")
    public ResponseEntity<Object> getGroupDetailed(@NotNull @RequestBody JSONObject request) {
        // 见文档
        return null;
    }
}
