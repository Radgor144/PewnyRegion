package com.pewnyregion.region.data.service.model;

import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record TercRecord(
        @JacksonXmlProperty(localName = "WOJ")
        String woj,

        @JacksonXmlProperty(localName = "POW")
        String pow,

        @JacksonXmlProperty(localName = "GMI")
        String gmi,

        @JacksonXmlProperty(localName = "RODZ")
        String rodz,

        @JacksonXmlProperty(localName = "NAZWA")
        String nazwa,

        @JacksonXmlProperty(localName = "NAZWA_DOD")
        String nazDod,

        @JacksonXmlProperty(localName = "STAN_NA")
        String stanNa
) {
}