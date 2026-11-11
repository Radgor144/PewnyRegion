package com.pewnyregion.region.data.service.model;

import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public record TercCatalog(
        @JacksonXmlProperty(isAttribute = true)
        String name,

        @JacksonXmlProperty(isAttribute = true)
        String type,

        @JacksonXmlProperty(isAttribute = true)
        String date,

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "row")
        List<TercRecord> rows
) {
}