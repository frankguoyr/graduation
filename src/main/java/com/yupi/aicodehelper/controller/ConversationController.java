package com.yupi.aicodehelper.controller;

import com.yupi.aicodehelper.entity.Conversation;
import com.yupi.aicodehelper.entity.Message;
import com.yupi.aicodehelper.repository.ConversationRepository;
import com.yupi.aicodehelper.repository.MessageRepository;
import com.yupi.aicodehelper.utils.LoginContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private ConversationRepository conversationRepository;

    @Resource
    private MessageRepository messageRepository;

    @PostMapping("/create")
    public Conversation create() {

        Long userId = LoginContext.get();

        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Conversation conversation = new Conversation();
        conversation.setTitle("新对话");
        conversation.setUserId(userId);

        return conversationRepository.save(conversation);
    }

    @GetMapping("/list")
    public List<Conversation> list() {
        Long userId = LoginContext.get();
        return conversationRepository.findByUserId(userId);
    }

    @GetMapping("/messages")
    public List<Message> messages(Long conversationId) {

        Long userId = LoginContext.get();

        return messageRepository
                .findByConversationIdAndUserIdOrderByCreateTimeAsc(conversationId, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        conversationRepository.deleteById(id);
    }



    @DeleteMapping("/delete")
    @Transactional
    public void deleteConversation(Long id) {

        Long userId = LoginContext.get();

        Conversation conversation = conversationRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("非法操作");
        }

        messageRepository.deleteByConversationId(id);
        conversationRepository.deleteById(id);

    }


    @PutMapping("/rename")
    public Conversation rename(Long id, String title) {

        Conversation conversation = conversationRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("会话不存在"));

        conversation.setTitle(title);

        return conversationRepository.save(conversation);
    }
}