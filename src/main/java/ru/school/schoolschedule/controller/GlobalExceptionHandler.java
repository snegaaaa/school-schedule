package ru.school.schoolschedule.controller;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Делает приложение "неубиваемым" для кривых параметров в URL:
 * day=ABOBA, id=lol, classId=NaN и т.п. → показываем 404 вместо 500.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            ConversionFailedException.class,
            MissingServletRequestParameterException.class
    })
    public String handleBadParams(Exception ex,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  Model model) {

        response.setStatus(404);
        model.addAttribute("path", safePath(request));
        return "error/404";
    }

    /**
     * На случай, если где-то включено "throwExceptionIfNoHandlerFound"
     * или Spring выбрасывает исключение для ресурсов.
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public String handleNotFound(Exception ex,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {

        response.setStatus(404);
        model.addAttribute("path", safePath(request));
        return "error/404";
    }

    private String safePath(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            return (uri == null || uri.isBlank()) ? "-" : uri;
        } catch (Exception ignored) {
            return "-";
        }
    }
}
