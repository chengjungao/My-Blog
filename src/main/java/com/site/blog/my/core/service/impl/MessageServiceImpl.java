package com.site.blog.my.core.service.impl;

import com.site.blog.my.core.dao.MessageMapper;
import com.site.blog.my.core.entity.Message;
import com.site.blog.my.core.service.ChatService;
import com.site.blog.my.core.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private ChatService chatService;

    @Override
    public Message handleMessage(Message message) {
        Message replyMessage = new Message();
        // 查询历史消息
        List<Message> historyMessages = messageMapper.selectByUser(message.getFromUser());
        // 插入新消息
        messageMapper.insert(message);

        replyMessage.setContent(chatService.chat(message.getContent(),message.getFromUser(), historyMessages));
        replyMessage.setToUser(message.getFromUser());
        replyMessage.setFromUser(message.getToUser());
        replyMessage.setMsgType("text");
        replyMessage.setCreateTime(new java.util.Date());

        // 插入回复消息
        messageMapper.insert(replyMessage);

        return replyMessage;
    }
}
