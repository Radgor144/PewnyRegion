package com.pewnyregion.region.data.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Links(String first,
                    String self,
                    String prev,
                    String next,
                    String last) {
}
