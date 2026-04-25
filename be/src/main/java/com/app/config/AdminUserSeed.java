package com.app.config;

import com.app.domain.entity.UserEntity;
import com.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Demo admin account (syllabus / local dev). Password: Admin1234!
 * Disable in production with {@code @Profile("!prod")} and set active profiles accordingly.
 */
@Component
@Profile("!prod")
public class AdminUserSeed implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminUserSeed.class);
    private static final String ADMIN_EMAIL = "admin@example.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserSeed(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
            return;
        }
        Instant now = Instant.now();
        UserEntity admin = new UserEntity();
        admin.setId("admin_demo_01");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
        admin.setRole(UserEntity.UserRole.ADMIN);
        admin.setActive(true);
        admin.setCreatedAt(now);
        userRepository.save(admin);
        log.info("Seeded admin user {} (password: Admin1234!)", ADMIN_EMAIL);
    }
}
