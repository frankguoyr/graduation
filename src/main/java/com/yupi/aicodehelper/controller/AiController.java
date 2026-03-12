package com.yupi.aicodehelper.controller;

import com.yupi.aicodehelper.ai.AiCodeHelperService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.yupi.aicodehelper.repository.MessageRepository;
import com.yupi.aicodehelper.entity.Message;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(Long conversationId, String message) {

        // 保存用户消息
        Message userMsg = new Message();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(message);
        messageRepository.save(userMsg);

        StringBuilder aiResponse = new StringBuilder();

        return aiCodeHelperService.chatStream(conversationId.intValue(), message)
                .doOnNext(aiResponse::append)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                .doOnComplete(() -> {
                    Message aiMsg = new Message();
                    aiMsg.setConversationId(conversationId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(aiResponse.toString());
                    messageRepository.save(aiMsg);
                });
    }
    
}
