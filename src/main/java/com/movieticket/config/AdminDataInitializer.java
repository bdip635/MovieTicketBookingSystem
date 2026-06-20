package com.movieticket.config;

import com.movieticket.domain.entity.User;
import com.movieticket.domain.enums.Role;
import com.movieticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer implements ApplicationRunner {

    static final String ADMIN_EMAIL = "admin@movieticket.com";
    static final String ADMIN_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }

        User admin = User.builder()
                .email(ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                .fullName("System Admin")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Seeded default admin user: {}", ADMIN_EMAIL);
    }
}
