package com.pewnyregion.region.analytics.service.model;

public record BdlUnit(String id,
                      String name,
                      String parentId,
                      int level,
                      String kind,
                      boolean hasDescription
) {
}
