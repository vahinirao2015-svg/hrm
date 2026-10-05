package com.salohi.hrms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class HrmsApplication {
    public static void main(String[] args) { SpringApplication.run(HrmsApplication.class, args); }

    /** Seeds the first admin account (change ADMIN_PASSWORD in production). */
    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder enc) {
        return args -> {
            if (repo.findByUsername("admin").isEmpty()) {
                String pwd = System.getenv().getOrDefault("ADMIN_PASSWORD", "Admin@123");
                repo.save(new AppUser("admin", enc.encode(pwd), Role.ADMIN));
            }
        };
    }
}
