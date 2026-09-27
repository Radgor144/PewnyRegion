package com.pewnyregion.region.data.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CountyDetailsRequest(
        @NotBlank(message = "terytCode is required")
        @Pattern(regexp = "\\d{4}", message = "terytCode must contain exactly 4 digits")
        String terytCode,

        @NotEmpty(message = "bdlVariableIds cannot be empty")
        @Size(max = 10, message = "bdlVariableIds cannot contain more than 10 values")
        List<@Positive(message = "bdlVariableIds must contain positive numbers") Integer> bdlVariableIds
) {
}
