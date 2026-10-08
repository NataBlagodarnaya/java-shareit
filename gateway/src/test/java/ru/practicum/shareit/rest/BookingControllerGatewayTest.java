package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.dto.NewBookingRequest;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    private final Long userId = 1L;
    private final String userHeader = "X-Sharer-User-Id";

    private NewBookingRequest createNewBookingRequest(LocalDateTime start, LocalDateTime end) {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(1L);
        request.setStart(start);
        request.setEnd(end);
        return request;
    }

    @Test
    void create_WhenDataIsValid_ShouldReturnStatusOk() throws Exception {
        NewBookingRequest request = createNewBookingRequest(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Mockito.when(bookingClient.createBooking(eq(userId), any(NewBookingRequest.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mvc.perform(post("/bookings")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenStartIsInPast_ShouldReturnBadRequest_ByValidation() throws Exception {
        NewBookingRequest badRequest = createNewBookingRequest(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        mvc.perform(post("/bookings")
                        .header(userHeader, userId)
                        .content(mapper.writeValueAsString(badRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingClient);
    }

    @Test
    void getAllByBooker_WithUnknownState_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/bookings")
                        .header(userHeader, userId)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(bookingClient);
    }
}