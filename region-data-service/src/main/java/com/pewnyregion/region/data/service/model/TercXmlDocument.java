package com.pewnyregion.region.data.service.model;

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "teryt")
public record TercXmlDocument(
        @JacksonXmlProperty(localName = "catalog")
        TercCatalog catalog
) {
}