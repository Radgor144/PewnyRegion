package com.pewnyregion.region.analytics.service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class JsonFileReader {

    public static <T> T readJson(ObjectMapper objectMapper, String pathToFile, Class<T> resourceType) throws IOException {
        return objectMapper.readValue(new File(pathToFile), resourceType);
    }

}
