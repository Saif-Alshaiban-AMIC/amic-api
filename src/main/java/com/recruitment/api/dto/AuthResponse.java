package com.recruitment.api.dto;

public class AuthResponse {

    public String accessToken;
    public String refreshToken;
    public String tokenType = "Bearer";
    public long   expiresIn;
    public String role;

    // Populated only when mustChangePassword is true.
    // accessToken / refreshToken will be null in that case.
    public Boolean mustChangePassword;
    public String  setupToken;

    /** Normal successful login — full token pair. */
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, String role) {
        this.accessToken  = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn    = expiresIn;
        this.role         = role;
    }

    /** First-login forced-reset — no auth tokens yet, only a short-lived setup token. */
    public static AuthResponse requiresSetup(String setupToken) {
        AuthResponse r = new AuthResponse(null, null, 0, null);
        r.mustChangePassword = true;
        r.setupToken = setupToken;
        return r;
    }
}
