package com.site.blog.my.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.site.blog.my.core.entity.Message;
import com.site.blog.my.core.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${chat.server.url:}")
    private String url;

    @Value("${token:}")
    private String token;

    @Override
    public String chat(String content, String user, List<Message> history) {

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = getStringHttpEntity(content,user,history, headers);

        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        if (jsonObject != null){
            return  jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }else {
            return  "助手暂时出现故障，无法响应您的问题！";
        }

    }

    private static HttpEntity<String> getStringHttpEntity(String content, String user,List<Message> history,HttpHeaders headers) {
        String requestBody = "{"
                + "\"model\": \"glm-4-plus\","
                + "\"temperature\": 0.1,"
                + "\"messages\": ["
                + "    {\"role\": \"system\", \"content\": \"你是代码江湖公众号的智能助手，可以为用户提供准确和专业的回答\"},";
        if (history != null && !history.isEmpty()){
            Collections.reverse(history);
            for (Message message : history) {
                if (!message.getFromUser().equals(user)){
                    requestBody += "{\"role\": \"assistant\", \"content\": \"" + message.getContent() + "\"},";
                }else {
                    requestBody += "{\"role\": \"user\", \"content\": \"" + message.getContent() + "\"},";
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
