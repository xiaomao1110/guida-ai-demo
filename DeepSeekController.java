package com.example.guidaaibackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeepSeekController {

    // 把刚才写的 Service 招进来，帮我们干活
    @Autowired
    private ChatService chatService;

    // 以前是 @GetMapping，现在是 @PostMapping，代表接收 POST 请求
    // @RequestBody 表示从请求的 JSON 包裹里提取数据
    @PostMapping("/ask")
    public String askAI(@RequestBody Map<String, String> requestBody) {
        try {
            // 从前端传来的 JSON 里获取 "question" 字段
            String question = requestBody.get("question");
            if (question == null || question.isEmpty()) {
                return "问题不能为空！";
            }

            // 呼叫 Service 去干活
            return chatService.askDeepSeek(question);

        } catch (Exception e) {
            e.printStackTrace();
            return "服务器开小差了：" + e.getMessage();
        }
    }
}