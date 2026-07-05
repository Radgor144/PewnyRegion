package com.pewnyregion.region.analytics.service.model;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;

import java.util.List;

public record InitChunk(CountyEntity county, List<Integer> varIds) {}
