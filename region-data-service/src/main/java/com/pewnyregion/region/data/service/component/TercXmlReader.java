package com.pewnyregion.region.data.service.component;

import com.pewnyregion.region.data.service.model.TercXmlDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class TercXmlReader {

    private final XmlMapper xmlMapper;

    public TercXmlDocument read(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            TercXmlDocument document = xmlMapper.readValue(inputStream, TercXmlDocument.class);

            if (!"TERC".equals(document.catalog().name())) {
                throw new IllegalArgumentException("Invalid TERC catalog");
            }

            return document;
        }
    }
}