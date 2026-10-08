package ru.practicum.shareit.json;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.dto.NewItemRequest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewItemRequestJsonTest {

    private final JacksonTester<NewItemRequest> json;

    private NewItemRequest createNewItemRequest() {
        NewItemRequest request = new NewItemRequest();
        request.setName("Дрель");
        request.setDescription("Ударная дрель Makita");
        request.setAvailable(true);
        request.setRequestId(5L);
        return request;
    }

    @Test
    void testNewItemRequestSerialization() throws Exception {
        NewItemRequest request = createNewItemRequest();

        JsonContent<NewItemRequest> result = json.write(request);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Ударная дрель Makita");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(5);
    }
}