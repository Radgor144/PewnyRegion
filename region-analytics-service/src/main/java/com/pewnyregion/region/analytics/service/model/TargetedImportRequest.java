package com.pewnyregion.region.analytics.service.model;

import java.util.List;

public record TargetedImportRequest (
        List<String> apiNames,
        List<Integer> years) {
}
