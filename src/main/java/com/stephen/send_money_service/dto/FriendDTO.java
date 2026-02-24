package com.stephen.send_money_service.dto;

import java.math.BigDecimal;

public class FriendDTO {

    private String id;
    private String name;
    private BigDecimal balance;

    public FriendDTO() {}

    public FriendDTO(String id, String name, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBalance() { return balance; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}