package com.pewnyregion.region.analytics.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {
    private String first;
    private String last;
    private String next;
    private String prev;
    private String self;
}
