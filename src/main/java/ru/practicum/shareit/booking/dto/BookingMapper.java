package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

public final class BookingMapper {

    public static BookingResponse toBookingResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setStart(booking.getStart());
        response.setEnd(booking.getEnd());
        response.setStatus(booking.getStatus());

        response.setBooker(UserMapper.toUserShortDto(booking.getBooker()));
        response.setItem(ItemMapper.toItemShortDto(booking.getItem()));

        return response;
    }

    public static BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingShortDto(
                booking.getId(),
                booking.getBooker().getId()
        );
    }

    public static Booking toBooking(NewBookingRequest newBookingRequest) {
        if (newBookingRequest == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setStart(newBookingRequest.getStart());
        booking.setEnd(newBookingRequest.getEnd());

        return booking;
    }
}
