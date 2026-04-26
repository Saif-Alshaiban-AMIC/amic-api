package com.recruitment.api.dto;

import java.util.List;

public class BulkCreateResult {
    public int created;
    public int failed;
    public List<String> errors;

    public BulkCreateResult(int created, int failed, List<String> errors) {
        this.created = created;
        this.failed = failed;
        this.errors = errors;
    }
}
