package com.recruitment.api.exception;

import java.time.Instant;

public class ErrorResponse {
    public int status;
    public String message;
    public Instant timestamp = Instant.now();

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
