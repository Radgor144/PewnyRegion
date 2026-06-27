package com.pewnyregion.region.analytics.service.model;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;

import java.util.List;

public record TargetedChunk(CountyEntity county, List<Integer> varIds, List<Integer> years) {}
