package com.stephen.send_money_service.repository;

import com.stephen.send_money_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}