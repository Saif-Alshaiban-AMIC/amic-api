package com.recruitment.api.dto;

import com.recruitment.api.model.Department;
import com.recruitment.api.model.Role;

public class CreateUserRequest {
    public String firstName;
    public String lastName;
    public String email;
    public String password;
    public String phoneNumber;
    public Department department;
    public String jobTitle;
    public Role role = Role.USER;
}
