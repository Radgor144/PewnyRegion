package com.pewnyregion.region.data.service.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestConstants {

    public static final String GET_ALL_VARIABLES_RESPONSE_JSON = "src/test/resources/getAllVariablesResponse.json";
    public static final String GET_POST_MAP_COUNTY_SCORES_RESPONSE_JSON = "src/test/resources/postMapCountyScores.json";

    public static final String GET_VARIABLES_API_PATH = "/api/variables";
    public static final String GET_MAP_COUNTY_SCORES_API_PATH = "/api/map/county-scores";

    public static final String BASE_IMPORT_PATH = "/api/imports";
    public static final String POST_TERYT_IMPORT_API_PATH = BASE_IMPORT_PATH + "/teryt";
    public static final String POST_FULL_IMPORT_API_PATH = BASE_IMPORT_PATH + "/full";
    public static final String POST_TARGETED_IMPORT_API_PATH = BASE_IMPORT_PATH + "/targeted";

}
