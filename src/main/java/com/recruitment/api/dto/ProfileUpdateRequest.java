package com.recruitment.api.dto;
import lombok.Data;


@Data

public class ProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    // department & jobTitle are read-only from the profile page (HR manages them)
    // add them here only if you want users to self-edit
}