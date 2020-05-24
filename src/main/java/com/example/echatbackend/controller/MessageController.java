package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.service.MessageService;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@CrossOrigin
@RestController
public class MessageController extends BaseController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }


    // 删除信息
    @PostMapping("/mes/removeMessage")
    public void removeMessage(@NotNull @RequestBody JSONObject request) {
    }

    // 加载更多消息
    @PostMapping("/mes/loadMoreMessages")
    public ResponseEntity<Object> loadMoreMessages(@NotNull @RequestBody JSONObject request) {
        /*
        [{
            //message类的所有字段
        }]
         */
        return null;
    }

    // 返回所有表情
    @PostMapping("/expre/getExpression")
    public String getExpression(){
        Collection<Emoji> allEmojis= EmojiManager.getAll();
        int num=allEmojis.size();
        org.json.JSONObject result=new org.json.JSONObject();
        int counter=0;
        String[] urls={};
        String[] ids=new String[num];
        String[] names=new String[num];
        String[] infos=new String[num];
        for(Emoji e:allEmojis){
            counter++;
            ids[counter-1]=e.getUnicode();
            names[counter-1]=e.getAliases().get(0);
            infos[counter-1]=e.getDescription();
        }
        result.put("list",urls);
        result.put("_id",ids);
        result.put("name",names);
        result.put("info",infos);
        return result.toString();
    }
}
