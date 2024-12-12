package com.site.blog.my.core.service.impl;

import com.site.blog.my.core.entity.Message;
import com.site.blog.my.core.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String chat(String content, String user, List<Message> history) {
        String url = System.getProperty("chat_server_url");
        String token = System.getProperty("token");

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer "+token);

        HttpEntity<String> entity = getStringHttpEntity(content,user,history, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getBody();
    }

    private static HttpEntity<String> getStringHttpEntity(String content, String user,List<Message> history,HttpHeaders headers) {
        String requestBody = "{"
                + "\"model\": \"glm-4-plus\","
                + "\"temperature\": 0.1,"
                + "\"messages\": ["
                + "    {\"role\": \"system\", \"content\": \"你是代码江湖公众号的智能助手，可以为用户提供准确和专业的回答\"},";
        if (history != null && !history.isEmpty()){
            List<Message> historyList = history.reversed();
            for (Message message : historyList) {
                if (!message.getFromUser().equals(user)){
                    requestBody += "{\"role\": \"assistant\", \"content\": \"\" + message.getContent() + \"\"},";
                }else {
                    requestBody += "{\"role\": \"user\", \"content\": \"\" + message.getContent() + \"\"},";
                }
            }
        }


        requestBody += "    {\"role\": \"user\", \"content\": \"" + content + "\"}"
                + "],"
                + "\"tools\": ["
                + "    {\"type\": \"web_search\", \"web_search\": {\"search_result\": true}}"
                + "]"
                + "}";

        return new HttpEntity<>(requestBody, headers);
    }
}
