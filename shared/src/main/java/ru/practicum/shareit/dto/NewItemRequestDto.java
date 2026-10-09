package ru.practicum.shareit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewItemRequestDto {
    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;
}
