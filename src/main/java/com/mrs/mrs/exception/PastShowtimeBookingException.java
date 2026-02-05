package com.mrs.mrs.exception;

public class PastShowtimeBookingException extends RuntimeException {
    public PastShowtimeBookingException(String message) {
        super(message);
    }
}