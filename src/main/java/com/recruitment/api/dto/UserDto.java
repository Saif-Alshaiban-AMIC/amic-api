package com.recruitment.api.dto;

import java.time.LocalDateTime;

public class UserDto {
    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;
    public String department;
    public String jobTitle;
    public String role;
    public LocalDateTime createdAt;

    public UserDto(Long id, String firstName, String lastName, String email,
                   String phoneNumber, String department, String jobTitle,
                   String role, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.department = department != null ? department : "";
        this.jobTitle = jobTitle;
        this.role = role;
        this.createdAt = createdAt;
    }
}
