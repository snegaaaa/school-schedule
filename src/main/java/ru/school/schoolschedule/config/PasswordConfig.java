package ru.school.schoolschedule.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Поддержка разных форматов:
        // 1) новый формат: "{bcrypt}$2a$..." (DelegatingPasswordEncoder)
        // 2) старый формат: "$2a$..." без префикса (legacy BCrypt)
        DelegatingPasswordEncoder delegating =
                (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();

        // Если в базе лежат старые BCrypt-хэши без {id}, разрешаем их матчить.
        delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

        return delegating;
    }
}
