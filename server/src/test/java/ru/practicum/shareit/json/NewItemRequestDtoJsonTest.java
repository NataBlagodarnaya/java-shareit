package ru.practicum.shareit.json;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.dto.NewItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewItemRequestDtoJsonTest {

    private final JacksonTester<NewItemRequestDto> json;

    private NewItemRequestDto createNewItemRequestDto() {
        NewItemRequestDto dto = new NewItemRequestDto();
        dto.setDescription("Ищу плюшевого мишку для фотосессии");
        return dto;
    }

    @Test
    void testNewItemRequestDtoSerialization() throws Exception {
        NewItemRequestDto dto = createNewItemRequestDto();

        JsonContent<NewItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ищу плюшевого мишку для фотосессии");
    }
}