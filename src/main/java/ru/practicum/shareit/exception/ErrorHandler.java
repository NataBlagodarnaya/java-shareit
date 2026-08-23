package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
        Object target = ex.getBindingResult().getTarget();
        log.error("Ошибка автоматической валидации для объекта: {}", target);//показываем в логе сам запрос где ошибка

        List<String> details = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();//вытаскиваем из каждой ошибки поле
            String errorMessage = error.getDefaultMessage();//вытаскиваем из каждой ошибки сообщение
            details.add(fieldName + ":" + errorMessage);//записываем детали чтобы потом это показать пользователю

            log.error("Детали ошибки валидации -> Поле '{}': {}", fieldName, errorMessage);//логируем каждую ошибку
        });
        String finalMessage = "Ошибка валидации полей: " + String.join(", ", details);// объединяем все ошибки
        return new ErrorResponse(finalMessage);//чтобы пользователь увидел все ошибки
    }

    @ExceptionHandler(DuplicatedDataException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicatedDataException(DuplicatedDataException ex) {
        log.error("Дублирование данных: {}", ex.getMessage(), ex);
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        log.error("Ресурс не найден: {}", ex.getMessage(), ex);
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllUncaughtExceptions(Throwable ex) {
        log.error("Что-то пошло не так. Это не обработанная ошибка: {}", ex.getMessage(), ex);
        return new ErrorResponse(ex.getMessage());
    }
}