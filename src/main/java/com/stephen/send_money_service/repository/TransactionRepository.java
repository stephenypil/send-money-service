package com.stephen.send_money_service.repository;

import com.stephen.send_money_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderIdOrReceiverId(String senderId, String receiverId);

    List<Transaction> findBySenderIdAndTimestampBetween(String senderId, LocalDateTime startOfDay, LocalDateTime now);
}