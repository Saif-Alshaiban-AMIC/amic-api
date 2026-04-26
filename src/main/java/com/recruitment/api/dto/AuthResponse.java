package com.recruitment.api.dto;

public class AuthResponse {

    public String accessToken;
    public String refreshToken;
    public String tokenType = "Bearer";
    public long expiresIn;
    public String role;

    public AuthResponse(String accessToken, String refreshToken, long expiresIn, String role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.role = role;
    }
}
