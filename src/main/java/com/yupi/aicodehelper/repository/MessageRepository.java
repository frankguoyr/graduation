package com.yupi.aicodehelper.repository;

import com.yupi.aicodehelper.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreateTimeAsc(Long conversationId);

    @Modifying
    @Transactional
    @Query("delete from Message m where m.conversationId = :conversationId")
    void deleteByConversationId(Long conversationId);
}