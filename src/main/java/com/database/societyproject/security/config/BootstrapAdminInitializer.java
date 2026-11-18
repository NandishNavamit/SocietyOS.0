package com.database.societyproject.security.config;

import com.database.societyproject.security.entity.*;
import com.database.societyproject.security.repository.UserSocietyRoleRepository;
import com.database.societyproject.user.entity.User;
import com.database.societyproject.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminInitializer {
    @Bean
    CommandLineRunner bootstrapAdmin(UserRepository users, UserSocietyRoleRepository roles, PasswordEncoder encoder,
            @Value("${app.bootstrap-admin.email}") String email,
            @Value("${app.bootstrap-admin.password}") String password) {
        return args -> {
            if (email == null || email.isBlank() || password == null || password.isBlank()) return;
            User user = users.findByEmailIgnoreCase(email).orElseGet(() -> {
                User created = new User();
                created.setFullName("Bootstrap Administrator");
                created.setEmail(email); created.setPhoneNumber("+10000000000");
                created.setPasswordHash(encoder.encode(password)); created.setActive(true);
                return users.save(created);
            });
            if (user.getPasswordHash() == null) user.setPasswordHash(encoder.encode(password));
            if (roles.findByUserIdAndSocietyIsNullAndRole(user.getId(), UserRole.ADMIN).isEmpty()) {
                UserSocietyRole role = new UserSocietyRole();
                role.setUser(user); role.setRole(UserRole.ADMIN); role.setActive(true);
                roles.save(role);
            }
        };
    }
}
