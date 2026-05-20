package com.recruitment.api.dto;

import java.time.LocalDate;

public class CreateDevRequestPayload {
    public String requesterName;
    public String requesterEmail;
    public String department;
    public String appName;
    public String appType;
    public String description;
    public String priority;
    public LocalDate targetDate;
}
