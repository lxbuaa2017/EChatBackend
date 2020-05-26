package com.example.echatbackend.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.echatbackend.entity.Message;
import com.example.echatbackend.service.MessageService;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

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
        Integer id = request.getInteger("id");
        if (id == null) {
            return;
        }
        messageService.deleteMessage(id);
    }

    // 加载更多消息
    @PostMapping("/mes/loadMoreMessages")
    public ResponseEntity<Object> loadMoreMessages(@NotNull @RequestBody JSONObject request) {
        Integer roomId = request.getInteger("roomId");
        Integer offset = request.getInteger("offset");
        Integer limit = request.getInteger("limit");
        if (roomId == null || offset == null || limit == null) {
            return requestFail(-1, "参数错误");
        }
        Page<Message> messages = messageService.getMoreMessage(roomId, offset, limit);
        List<Message> messageList = messages.getContent();
        return ResponseEntity.ok(JSONArray.parseArray(JSON.toJSONString(messageList.stream().map(Message::show).toArray(JSONObject[]::new))));
    }

    // 返回所有表情
    @PostMapping("/expre/getExpression")
    public ResponseEntity<Object> getExpression() {
        Collection<Emoji> allEmojis = EmojiManager.getAll();
        int num = allEmojis.size();
        JSONObject result = new JSONObject();
        JSONObject[] resultJSON = new JSONObject[num];
        int count = 0;
        for (Emoji e : allEmojis) {
            JSONObject aEmoji = new JSONObject();
            aEmoji.put("_id", e.getUnicode());
            aEmoji.put("name", e.getAliases().get(0));
            aEmoji.put("code", e.getHtmlDecimal());
            aEmoji.put("info", e.getDescription());
            resultJSON[count++] = aEmoji;
        }
        result.put("list", resultJSON);
        return requestSuccess(result);
    }
}
