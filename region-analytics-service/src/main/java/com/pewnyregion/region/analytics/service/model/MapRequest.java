package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record MapRequest(
        List<String> apiNames,
        Integer yearFrom,
        Integer yearTo
) {}