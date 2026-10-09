package ru.practicum.shareit.json;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.dto.NewUserRequest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewUserRequestJsonTest {

    private final JacksonTester<NewUserRequest> json;

    private NewUserRequest createNewUserRequest() {
        NewUserRequest request = new NewUserRequest();
        request.setName("Иван");
        request.setEmail("ivan@mail.com");
        return request;
    }

    @Test
    void testNewUserRequestSerialization() throws Exception {
        NewUserRequest request = createNewUserRequest();

        JsonContent<NewUserRequest> result = json.write(request);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@mail.com");
    }
}