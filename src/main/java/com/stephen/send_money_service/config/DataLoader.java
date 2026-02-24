package com.stephen.send_money_service.config;

import com.stephen.send_money_service.entity.User;
import com.stephen.send_money_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {

        // Only insert if DB is empty
        if (userRepository.count() == 0) {

            User u1 = new User("1", new BigDecimal("1000.00"));
            User u2 = new User("2", new BigDecimal("500.00"));
            User u3 = new User("3", new BigDecimal("250.50"));

            userRepository.save(u1);
            userRepository.save(u2);
            userRepository.save(u3);

            System.out.println("Seeded test users!");
        }
    }
}