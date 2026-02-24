package com.stephen.send_money_service.controller;

import com.stephen.send_money_service.dto.FriendDTO;
import com.stephen.send_money_service.entity.Transaction;
import com.stephen.send_money_service.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    public BigDecimal getBalance(@RequestHeader("X-User-Id") String userId) {
        return walletService.getBalance(userId);
    }

    @PostMapping("/send")
    public Transaction sendMoney(
            @RequestHeader("X-User-Id") String senderId,
            @RequestParam String receiverId,
            @RequestParam BigDecimal amount
    ) {
        return walletService.sendMoney(senderId, receiverId, amount);
    }

    // ✅ All transactions for a user
    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@RequestHeader("X-User-Id") String userId) {
        return walletService.getTransactionsForUser(userId);
    }

    // ✅ Single transaction details
    @GetMapping("/transactions/{id}")
    public Transaction getTransactionDetails(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long id
    ) {
        return walletService.getTransactionDetails(id, userId);
    }

    // Friends
    @GetMapping("/friends")
    public List<FriendDTO> getFriends(@RequestHeader("X-User-Id") String userId) {
        return walletService.getFriendList(userId);
    }
}