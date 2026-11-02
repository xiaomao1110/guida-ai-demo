package com.example.guidaaibackend;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service // 这个注解表示它是一个干活的组件，Spring 会自动管理它
public class ChatService {

    // 从 application.properties 里读取 key，这样代码里就不会暴露密码了
    @Value("${deepseek.api.key}")
    private String apiKey;

    public String askDeepSeek(String question) throws Exception {
        // 1. 读取本地资料并切分 (和昨天一样)
        ClassPathResource resource = new ClassPathResource("guida.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder textBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            textBuilder.append(line);
        }
        reader.close();
        String fullText = textBuilder.toString();

        List<String> chunks = new ArrayList<>();
        int chunkSize = 500;
        for (int i = 0; i < fullText.length(); i += chunkSize) {
            chunks.add(fullText.substring(i, Math.min(fullText.length(), i + chunkSize)));
        }
        // 1. 用一个 List 来装匹配度最高的前 3 块资料
        List<String> topChunks = new ArrayList<>();
        List<Integer> topScores = new ArrayList<>();

        for (String chunk : chunks) {
            int matches = 0;
            for (char c : question.toCharArray()) {
                if (chunk.indexOf(c) != -1) {
                    matches++;
                }
            }

            // 简单的排行榜逻辑：如果匹配度大于 2，就加入候选名单
            if (matches >= 2) {
                topChunks.add(chunk);
                topScores.add(matches);
            }
        }

        // 如果什么也没找到，直接拦截
        if (topChunks.isEmpty()) {
            return "抱歉，教务资料库中没有找到与您问题相关的内容。";
        }

        // 简单的排序：把匹配度最高的排在最前面 (冒泡排序)
        for (int i = 0; i < topScores.size(); i++) {
            for (int j = i + 1; j < topScores.size(); j++) {
                if (topScores.get(j) > topScores.get(i)) {
                    // 交换分数
                    int tempScore = topScores.get(i);
                    topScores.set(i, topScores.get(j));
                    topScores.set(j, tempScore);
                    // 交换对应的内容
                    String tempChunk = topChunks.get(i);
                    topChunks.set(i, topChunks.get(j));
                    topChunks.set(j, tempChunk);
                }
            }
        }

        // 2. 拼接前 3 名的资料（如果不足 3 个就全取）
        StringBuilder contextBuilder = new StringBuilder();
        int count = Math.min(3, topChunks.size());
        for (int i = 0; i < count; i++) {
            contextBuilder.append(topChunks.get(i)).append("\n\n---\n\n");
        }
        String bestContext = contextBuilder.toString();

        // 打印出来看看是不是找了多块
        System.out.println("============= 查找到的参考资料 (Top " + count + ") =============");
        System.out.println(bestContext);
        System.out.println("===========================================");
        // 3. 组装 Prompt 和 JSON
        String prompt = "你是一个专业的教务助理。请严格根据以下参考资料回答用户问题。如果资料中没有提到，请直接回答“资料未提及”，不要自己编造。\n\n" +
                "【参考资料】：\n" + bestContext + "\n\n" +
                "【用户问题】：\n" + question;

        JSONObject messageObj = new JSONObject();
        messageObj.put("role", "user");
        messageObj.put("content", prompt);
        JSONArray messagesArray = new JSONArray();
        messagesArray.add(messageObj);
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", "deepseek-chat");
        requestJson.put("messages", messagesArray);

        // 4. 发请求 (注意 apiKey 已经是从上面 @Value 注入进来的了)
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.deepseek.com/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson.toJSONString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. 解析并返回
        JSONObject jsonObject = JSON.parseObject(response.body());
        if (jsonObject.containsKey("choices")) {
            JSONArray choices = jsonObject.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content");
        } else {
            return "API 调用失败，请检查 Key 是否正确。错误信息：" + response.body();
        }
    }
}