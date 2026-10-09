package ru.practicum.shareit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentRequest {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 200, message = "Длина текста не должна превышать 200 символов")
    private String text;
}
