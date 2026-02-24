package com.stephen.send_money_service.service;

import com.stephen.send_money_service.dto.FriendDTO;
import com.stephen.send_money_service.entity.Transaction;
import com.stephen.send_money_service.entity.User;
import com.stephen.send_money_service.repository.TransactionRepository;
import com.stephen.send_money_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate;

    private static final BigDecimal DAILY_LIMIT = new BigDecimal("5000"); // daily limit

    public WalletService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.restTemplate = new RestTemplate();
    }

    // ===========================
    // Money-related methods
    // ===========================

    public BigDecimal getBalance(String userId) {
        logger.info("User {} requested balance", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getBalance();
    }

    @Transactional
    public Transaction sendMoney(String senderId, String receiverId, BigDecimal amount) {
        logger.info("User {} attempts to send {} to user {}", senderId, amount, receiverId);

        if (senderId.equals(receiverId)) {
            logger.warn("User {} tried to send money to themselves", senderId);
            throw new RuntimeException("Cannot send money to yourself");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("User {} tried to send a non-positive amount: {}", senderId, amount);
            throw new RuntimeException("Amount must be positive");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            logger.warn("User {} has insufficient balance to send {}", senderId, amount);
            throw new RuntimeException("Insufficient balance");
        }

        // Daily limit check
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalSentToday = transactionRepository
                .findBySenderIdAndTimestampBetween(senderId, startOfDay, now)
                .stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSentToday.add(amount).compareTo(DAILY_LIMIT) > 0) {
            logger.warn("User {} exceeded daily limit. Sent today: {}, Trying to send: {}",
                    senderId, totalSentToday, amount);
            throw new RuntimeException("Daily transfer limit of " + DAILY_LIMIT + " exceeded");
        }

        // Update balances
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        userRepository.save(sender);
        userRepository.save(receiver);

        // Record transaction
        Transaction tx = new Transaction(senderId, receiverId, amount, LocalDateTime.now());
        Transaction savedTx = transactionRepository.save(tx);
        logger.info("Transaction successful: {} sent {} to {}", senderId, amount, receiverId);

        return savedTx;
    }

    public List<Transaction> getTransactionsForUser(String userId) {
        logger.info("User {} requested their transaction history", userId);
        return transactionRepository.findBySenderIdOrReceiverId(userId, userId);
    }

    public Transaction getTransactionDetails(Long transactionId, String userId) {
        logger.info("User {} requested details for transaction {}", userId, transactionId);

        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!tx.getSenderId().equals(userId) && !tx.getReceiverId().equals(userId)) {
            logger.warn("User {} tried to access transaction {} they are not part of", userId, transactionId);
            throw new RuntimeException("Access denied");
        }

        return tx;
    }

    // ===========================
    // Friend list method
    // ===========================

    public List<FriendDTO> getFriendList(String userId) {
        logger.info("User {} requested friend list", userId);

        List<User> users = userRepository.findAll()
                .stream()
                .filter(u -> !u.getId().equals(userId))
                .collect(Collectors.toList());

        return users.stream().map(u -> {
            ExternalUser externalUser;
            try {
                String url = "https://jsonplaceholder.typicode.com/users/" + u.getId();
                externalUser = restTemplate.getForObject(url, ExternalUser.class);
            } catch (Exception e) {
                externalUser = new ExternalUser();
                externalUser.setName("Unknown");
                logger.warn("Failed to fetch external name for user {}", u.getId());
            }

            return new FriendDTO(u.getId(), externalUser.getName(), u.getBalance());
        }).collect(Collectors.toList());
    }

    // Internal class to map JSONPlaceholder response
    private static class ExternalUser {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}