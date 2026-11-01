package com.example.guidaaibackend;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class DeepSeekController {

    @GetMapping("/ask")
    public String askAI() throws Exception {
        // 1. 替换成你真实的 API Key
        String apiKey = "我真实的api";

        // 2. 组装请求 JSON
        String requestBody = """
                {
                    "model": "deepseek-chat",
                    "messages": [
                        {"role": "user", "content": "你好，请用一句话介绍你自己。"}
                    ]
                }
                """;

        // 3. 构建请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.deepseek.com/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // 4. 发送请求并获取响应
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. 解析 JSON 提取回答
        String responseBody = response.body();
        JSONObject jsonObject = JSON.parseObject(responseBody);
        JSONArray choices = jsonObject.getJSONArray("choices");

        // 6. 返回 AI 的话
        return choices.getJSONObject(0).getJSONObject("message").getString("content");
    }
}