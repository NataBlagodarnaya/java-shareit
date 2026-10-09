package ru.practicum.shareit.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingResponse;
import ru.practicum.shareit.dto.BookingState;
import ru.practicum.shareit.dto.NewBookingRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final String userHeader = "X-Sharer-User-Id";

    private NewBookingRequest createRequest() {
        NewBookingRequest request = new NewBookingRequest();
        request.setItemId(2L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));
        return request;
    }

    private BookingResponse createResponse(NewBookingRequest request) {
        BookingResponse response = new BookingResponse();
        response.setId(bookingId);
        response.setStart(request.getStart());
        response.setEnd(request.getEnd());
        return response;
    }

    @Test
    void create_ShouldReturnCreatedBooking() throws Exception {
        NewBookingRequest newBookingRequest = createRequest();
        BookingResponse bookingResponse = createResponse(newBookingRequest);

        Mockito.when(bookingService.createBooking(eq(userId), any(NewBookingRequest.class)))
                .thenReturn(bookingResponse);

        mockMvc.perform(post("/bookings")
                        .header(userHeader, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void approve_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponse bookingResponse = createResponse(createRequest());
        bookingResponse.setStatus(BookingStatus.APPROVED);

        Mockito.when(bookingService.approveBooking(userId, bookingId, true))
                .thenReturn(bookingResponse);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(userHeader, userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingById_ShouldReturnBooking() throws Exception {
        BookingResponse bookingResponse = createResponse(createRequest());

        Mockito.when(bookingService.getBookingById(userId, bookingId))
                .thenReturn(bookingResponse);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void getAllByBooker_WithDefaultState_ShouldReturnBookings() throws Exception {
        BookingResponse bookingResponse = createResponse(createRequest());

        Mockito.when(bookingService.getAllByBooker(userId, BookingState.ALL))
                .thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllByBooker_WithCustomState_ShouldReturnBookings() throws Exception {
        BookingResponse bookingResponse = createResponse(createRequest());

        Mockito.when(bookingService.getAllByBooker(userId, BookingState.FUTURE))
                .thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings")
                        .header(userHeader, userId)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllByOwner_ShouldReturnBookings() throws Exception {
        BookingResponse bookingResponse = createResponse(createRequest());

        Mockito.when(bookingService.getAllByOwner(userId, BookingState.ALL))
                .thenReturn(List.of(bookingResponse));

        mockMvc.perform(get("/bookings/owner")
                        .header(userHeader, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getAllByBooker_WithUnknownState_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(userHeader, userId)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingById_WhenUserIsNotOwnerOrBooker_ShouldReturnNotFoundStatus() throws Exception {
        Mockito.when(bookingService.getBookingById(userId, bookingId))
                .thenThrow(new ru.practicum.shareit.exception.NotFoundException("Доступ запрещен"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(userHeader, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_WhenItemNotAvailable_ShouldReturnBadRequestStatus() throws Exception {
        NewBookingRequest request = createRequest();

        Mockito.when(bookingService.createBooking(eq(userId), any(NewBookingRequest.class)))
                .thenThrow(new ru.practicum.shareit.exception.BadRequestException("Вещь недоступна для бронирования"));

        mockMvc.perform(post("/bookings")
                        .header(userHeader, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
