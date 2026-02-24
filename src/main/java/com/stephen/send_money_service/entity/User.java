package com.stephen.send_money_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "users") // use plural to avoid SQL reserved keyword issues
public class User {

    @Id
    private String id;

    private BigDecimal balance;

    public User() {}

    public User(String id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}