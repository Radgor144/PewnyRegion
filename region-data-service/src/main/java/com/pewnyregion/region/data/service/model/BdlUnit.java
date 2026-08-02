package com.pewnyregion.region.data.service.model;

public record BdlUnit(String id,
                      String name,
                      String parentId,
                      int level,
                      String kind,
                      boolean hasDescription
) {
}
