package com.pewnyregion.region.analytics.service.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TargetedImportRequest (
        @NotEmpty(message = "apiNames cannot be empty")
        @Size(max = 10, message = "Maximum 10 variables allowed for a single import")
        List<String> apiNames,

        @NotEmpty(message = "years cannot be empty")
        List<@NotNull(message = "year cannot be null")
        @Min(value = 2012, message = "year cannot be earlier than 2012")
        @Max(value = 2026, message = "year cannot be later than 2026")
                Integer> years
) {
}
