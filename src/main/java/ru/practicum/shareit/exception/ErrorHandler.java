package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
        Object target = ex.getBindingResult().getTarget();
        log.error("Ошибка автоматической валидации для объекта: {}", target);//показываем в логе сам запрос где ошибка

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();//вытаскиваем из каждой ошибки поле
            String errorMessage = error.getDefaultMessage();//вытаскиваем из каждой ошибки сообщение
            errors.put(fieldName, errorMessage);//записываем в мапу чтобы потом это показать пользователю

            log.error("Детали ошибки валидации -> Поле '{}': {}", fieldName, errorMessage);//логируем каждую ошибку
        });

        return errors;//чтобы пользователь увидел в чем ошибка
    }

    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleDuplicatedDataException(DuplicatedDataException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Передаем текст ошибки пользователю
        log.error("Дублирование данных: {}", ex.getMessage(), ex);
        return error;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFoundException(NotFoundException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Передаем текст ошибки пользователю
        log.error("Ресурс не найден: {}", ex.getMessage());
        return error;
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleAllUncaughtExceptions(Throwable ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage()); // Передаем текст ошибки пользователю
        log.error("Что-то пошло не так. Это не обработанная ошибка: {}", ex.getMessage(), ex);
        return error;
    }
}