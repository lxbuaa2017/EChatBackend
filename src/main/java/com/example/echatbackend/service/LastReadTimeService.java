package com.example.echatbackend.service;

import com.example.echatbackend.dao.LastReadTimeRepository;
import com.example.echatbackend.entity.LastReadTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LastReadTimeService extends BaseService<LastReadTime,Integer, LastReadTimeRepository> {
    @Autowired
    private LastReadTimeRepository lastReadTimeRepository;

    public Long getAndSetNewLastReadTime(String conversationId,Integer userId){
        LastReadTime lastReadTime = lastReadTimeRepository.findByConversationIdAndUserId(conversationId, userId);
        if(lastReadTime==null){
            lastReadTime = setLastReadTime(conversationId, userId);
            return lastReadTime.getLastReadTime();
        }
        Long res = Long.valueOf(lastReadTime.getLastReadTime().toString());//防止覆盖
        lastReadTime.setLastReadTime(System.currentTimeMillis());
        lastReadTimeRepository.saveAndFlush(lastReadTime);
        return res;
    }

    public LastReadTime setLastReadTime(String conversationId,Integer userId){
        Long now = System.currentTimeMillis();
        LastReadTime lastReadTime = lastReadTimeRepository.findByConversationIdAndUserId(conversationId, userId);
        if(lastReadTime==null){
            lastReadTime = new LastReadTime();
            lastReadTime.setConversationId(conversationId);
            lastReadTime.setUserId(userId);
        }
        lastReadTime.setLastReadTime(now);
        return lastReadTimeRepository.saveAndFlush(lastReadTime);
    }
}
