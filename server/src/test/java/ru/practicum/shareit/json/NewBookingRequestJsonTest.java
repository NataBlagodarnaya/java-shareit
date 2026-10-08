package ru.practicum.shareit.json;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.dto.NewBookingRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class NewBookingRequestJsonTest {

    private final JacksonTester<NewBookingRequest> json;

    private NewBookingRequest createNewBookingRequest() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(LocalDateTime.of(2026, 10, 10, 12, 0, 0));
        request.setEnd(LocalDateTime.of(2026, 10, 11, 12, 0, 0));
        return request;
    }

    @Test
    void testNewBookingRequestSerialization() throws Exception {
        NewBookingRequest request = createNewBookingRequest();

        JsonContent<NewBookingRequest> result = json.write(request);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-10-10T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-10-11T12:00:00");
    }
}