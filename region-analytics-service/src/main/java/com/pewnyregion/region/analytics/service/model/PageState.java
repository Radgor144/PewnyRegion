package com.pewnyregion.region.analytics.service.model;

import com.pewnyregion.region.analytics.service.entity.CountyEntity;

import java.util.List;

public record PageState(int page, List<CountyEntity> content) {}
