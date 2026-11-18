package com.database.societyproject.society.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SocietyRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String address,
        @Size(max = 255) String city,
        @Size(max = 255) String state,
        @Size(max = 20) String pincode,
        Boolean active) { }
