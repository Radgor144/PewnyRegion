package com.pewnyregion.region.analytics.service.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MapRequest(
        @NotEmpty(message = "apiNames cannot be empty")
        @Size(max = 5, message = "Maximum 5 variables allowed")
        List<String> apiNames,

        @NotNull(message = "yearFrom is required")
        @Min(value = 2012, message = "yearFrom cannot be earlier than 2012")
        @Max(value = 2026, message = "yearFrom cannot be later than 2026")
        Integer yearFrom,

        @NotNull(message = "yearTo is required")
        @Min(value = 2012, message = "yearTo cannot be earlier than 2012")
        @Max(value = 2026, message = "yearTo cannot be later than 2026")
        Integer yearTo
) {
        @AssertTrue(message = "yearFrom must be less than or equal to yearTo")
        public boolean isYearRangeValid() {
                return yearFrom == null || yearTo == null || yearFrom <= yearTo;
        }
}