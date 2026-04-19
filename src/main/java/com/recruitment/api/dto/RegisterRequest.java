package com.recruitment.api.dto;

import com.recruitment.api.model.Department;

public class RegisterRequest {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String phoneNumber;
    public Department department;
    public String jobTitle;
}