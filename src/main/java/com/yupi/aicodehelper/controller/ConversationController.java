package com.yupi.aicodehelper.controller;

import com.yupi.aicodehelper.entity.Conversation;
import com.yupi.aicodehelper.entity.Message;
import com.yupi.aicodehelper.repository.ConversationRepository;
import com.yupi.aicodehelper.repository.MessageRepository;
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

        Conversation conversation = new Conversation();
        conversation.setTitle("新对话");

        return conversationRepository.save(conversation);
    }

    @GetMapping("/list")
    public List<Conversation> list() {
        return conversationRepository.findAll();
    }

    @GetMapping("/messages")
    public List<Message> messages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreateTimeAsc(conversationId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        conversationRepository.deleteById(id);
    }



    @DeleteMapping("/delete")
    @Transactional
    public void deleteConversation(Long id) {

        // 删除该会话的所有消息
        messageRepository.deleteByConversationId(id);

        // 删除会话
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