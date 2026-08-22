package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

    @Data
    public class UpdateUserRequest {
        @NotNull(message = "Id пользователя должен быть указан для обновления")
        private Long id;
        private String name;
        @Email(message = "Email введен некорректно")
        private String email;
    }