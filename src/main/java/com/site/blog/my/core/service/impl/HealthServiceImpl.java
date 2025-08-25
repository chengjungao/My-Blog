package com.site.blog.my.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.site.blog.my.core.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

public class HealthServiceImpl implements HealthService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${vision.server.url:}")
    private String visionServerUrl;

    @Value("${vision.token:}")
    private String visionToken;

    @Override
    public String analyzeIngredients(MultipartFile image) {
        try {
            String prompt ="请分析这张食品配料表图片： \n 1. 提取所有配料名称 \n 2. 对每个配料说明其常见用途（如防腐剂、增稠剂等） \n 3. 给出每人每日建议摄入量 \n 4. 标注是否有潜在健康风险 \n " +
                    "请以 JSON 数组格式返回，不要额外解释： [ \"ingredient\": \"苯甲酸钠\", \"purpose\": \"防腐剂\", \"recommended_intake\": \"≤5mg/kg体重/天\", \"risk\": \"过量可能引起过敏\"}] ";
            HttpEntity<String> entity = getStringHttpEntityVision(image,prompt);
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(visionServerUrl, HttpMethod.POST, entity, String.class);
            JSONObject jsonObject = JSON.parseObject(response.getBody());
            return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        }catch (Exception e) {
            return "助手暂时出现故障，无法响应您的问题！";
        }
    }

    @Override
    public String estimateCalories(MultipartFile image) {
        try {
            String prompt = " 请分析这张餐食图片： 1. 识别主要食物（如米饭、鸡胸肉） \n 2. 估算每种食物的分量（克数） \n 3. 给出每种食物的热量（kcal） \n4. 计算总热量 \n " +
                    "请以 JSON 格式返回： {\"foods\": [ {\"name\": \"白米饭\", \"weight\": \"150g\", \"calories\": 175}],\"total_calories\": 175}";
            HttpEntity<String> entity = getStringHttpEntityVision(image,prompt);
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(visionServerUrl, HttpMethod.POST, entity, String.class);
            JSONObject jsonObject = JSON.parseObject(response.getBody());
            return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        }catch (Exception e) {
            return "助手暂时出现故障，无法响应您的问题！";
        }
    }

    private HttpEntity<String> getStringHttpEntityVision(MultipartFile image,String systemPrompt) throws Exception {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Authorization", "Bearer " + visionToken);

        String base64_image = java.util.Base64.getEncoder().encodeToString(image.getBytes());
        String content = "data:" + image.getContentType() + ";base64," + base64_image;
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-vl-plus");
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", 512);
        JSONArray messages = new JSONArray();
        messages.add(new JSONObject().fluentPut("role", "system").fluentPut("content", systemPrompt));
        JSONArray imageContent = new JSONArray();
        imageContent.add(new JSONObject().fluentPut("image", content));
        messages.add(new JSONObject().fluentPut("role", "user").fluentPut("content", imageContent));

        requestBody.put("messages", messages);
        return new HttpEntity<>(requestBody.toString(), headers);
    }
}
