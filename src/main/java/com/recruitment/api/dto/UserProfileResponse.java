package com.recruitment.api.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String department;
    private String jobTitle;
    private String profilePicture;
    private String createdAt;
}