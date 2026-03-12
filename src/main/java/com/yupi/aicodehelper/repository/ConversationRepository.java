package com.yupi.aicodehelper.repository;

import com.yupi.aicodehelper.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}