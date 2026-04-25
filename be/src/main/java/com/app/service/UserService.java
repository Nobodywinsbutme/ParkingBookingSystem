package com.app.service;

import com.app.domain.entity.UserEntity;
import com.app.repository.UserRepository;
import com.app.support.ApiException;
import com.app.web.form.RegisterForm;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserEntity> listAllForAdmin() {
        return userRepository.findAllByOrderByEmailAsc();
    }

    @Transactional
    public void register(RegisterForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Passwords do not match");
        }
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Email already registered");
        }
        Instant now = Instant.now();
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID().toString().replace("-", ""));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(UserEntity.UserRole.USER);
        user.setActive(true);
        user.setCreatedAt(now);
        userRepository.save(user);
    }
}
