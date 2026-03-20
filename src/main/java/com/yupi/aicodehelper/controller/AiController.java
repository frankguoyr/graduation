package com.yupi.aicodehelper.controller;

import com.yupi.aicodehelper.ai.AiCodeHelperService;
import com.yupi.aicodehelper.utils.LoginContext;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.yupi.aicodehelper.repository.MessageRepository;
import com.yupi.aicodehelper.entity.Message;
import com.yupi.aicodehelper.repository.ConversationRepository;
import com.yupi.aicodehelper.entity.Conversation;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private MessageRepository messageRepository;

    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Resource
    private ConversationRepository conversationRepository;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(Long conversationId, String message, Long userId) {

        LoginContext.set(userId);

        Long currentUserId = LoginContext.get();

        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        if (conversation.getUserId() == null || !conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权限访问该会话");
        }

        // 保存用户消息
        Message userMsg = new Message();
        userMsg.setUserId(currentUserId);
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

                    // 保存AI消息
                    Message aiMsg = new Message();
                    aiMsg.setUserId(currentUserId);
                    aiMsg.setConversationId(conversationId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(aiResponse.toString());
                    messageRepository.save(aiMsg);

//                    // 获取当前会话
//                    Conversation conversation = conversationRepository
//                            .findById(conversationId)
//                            .orElse(null);

                    if (conversation != null && "新对话".equals(conversation.getTitle())) {

                        // 调用AI生成标题
                        String prompt = "请根据用户的问题生成一个不超过12个字的标题，要求符合用户的对话，只返回生产的标题：" + message;

                        String title = aiCodeHelperService.chat(prompt);

                        conversation.setTitle(title.trim());

                        conversationRepository.save(conversation);
                    }

                });
    }
    
}
