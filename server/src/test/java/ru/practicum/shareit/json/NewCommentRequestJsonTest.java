package ru.practicum.shareit.json;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.dto.NewCommentRequest;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewCommentRequestJsonTest {

    private final JacksonTester<NewCommentRequest> json;

    private NewCommentRequest createNewCommentRequest() {
        NewCommentRequest request = new NewCommentRequest();
        request.setText("Отличный инструмент, всё работает супер!");
        return request;
    }

    @Test
    void testNewCommentRequestSerialization() throws Exception {
        NewCommentRequest request = createNewCommentRequest();

        JsonContent<NewCommentRequest> result = json.write(request);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличный инструмент, всё работает супер!");
    }
}