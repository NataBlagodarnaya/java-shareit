package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCommentRequest {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 200, message = "Длина текста не должна превышать 200 символов")
    private String text;
}
