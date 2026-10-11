package com.pewnyregion.region.data.service.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestConstants {

    // ===== Fixtures: MAP =====
    public static final String GET_ALL_VARIABLES_RESPONSE_JSON = "src/test/resources/getAllVariablesResponse.json";
    public static final String MAP_COUNTY_SCORES_RESPONSE_JSON = "src/test/resources/postMapCountyScores.json";

    // ===== Fixtures: GUS BDL API responses =====
    public static final String BDL_UNITS_PAGE_0_JSON = "src/test/resources/bdl/units-page-0.json";
    public static final String BDL_BY_UNIT_CRIMES_JSON = "src/test/resources/bdl/by-unit-crimes.json";
    public static final String BDL_TARGETED_BY_UNIT_CRIMES_JSON = "src/test/resources/bdl/targeted-by-unit-crimes.json";

    // ===== Paths: own API =====
    public static final String GET_VARIABLES_API_PATH = "/api/variables";
    public static final String GET_MAP_COUNTY_SCORES_API_PATH = "/api/map/county-scores";
    public static final String GET_COUNTY_DETAILS_API_PATH = "/api/countyDetails";

    public static final String BASE_IMPORT_PATH = "/api/imports";
    public static final String POST_TERYT_IMPORT_API_PATH = BASE_IMPORT_PATH + "/teryt";
    public static final String POST_FULL_IMPORT_API_PATH = BASE_IMPORT_PATH + "/full";
    public static final String POST_TARGETED_IMPORT_API_PATH = BASE_IMPORT_PATH + "/targeted";

    // ===== Paths: BdlApiClient =====
    public static final String BDL_UNITS_PATH = "/Units";
    public static final String BDL_DATA_BY_UNIT_PATH = "/data/by-unit/{id}";
}
