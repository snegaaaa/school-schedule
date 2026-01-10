package ru.school.schoolschedule.config;

import java.util.Map;

import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class ErrorViewConfig {

    @Bean
    public ErrorViewResolver normalizeUnknownUrlTo404() {
        return (HttpServletRequest request, HttpStatus status, Map<String, Object> model) -> {

            // Исходный URL, который вызвал ошибку (например "/abc")
            String originalUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

            // Если почему-то прилетел 403, но это НЕ админ-раздел — показываем 404.
            if (status == HttpStatus.FORBIDDEN && originalUri != null) {
                boolean adminArea =
                        originalUri.startsWith("/setup")
                        || originalUri.startsWith("/classes")
                        || originalUri.startsWith("/access-denied");

                if (!adminArea) {
                    return new ModelAndView("error/404", model, HttpStatus.NOT_FOUND);
                }
            }

            // Остальное не трогаем — пусть Boot сам выбирает страницы ошибок
            return null;
        };
    }
}
