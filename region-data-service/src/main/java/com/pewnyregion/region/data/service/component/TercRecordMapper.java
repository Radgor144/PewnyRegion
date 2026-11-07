package com.pewnyregion.region.data.service.component;

import com.pewnyregion.region.data.service.model.TercRecord;
import com.pewnyregion.region.data.service.model.TerritorialUnitImportRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class TercRecordMapper {

    private static final String VOIVODESHIP = "VOIVODESHIP";
    private static final String COUNTY = "COUNTY";
    private static final String MUNICIPALITY = "MUNICIPALITY";

    private static final List<String> MUNICIPALITY_TYPES = List.of("1", "2", "3");

    public Optional<TerritorialUnitImportRecord> map(TercRecord record) {
        Optional<String> unitLevel = resolveUnitLevel(record);

        if (unitLevel.isEmpty()) {
            return Optional.empty();
        }

        String level = unitLevel.get();

        return Optional.of(new TerritorialUnitImportRecord(
                buildTerytCode(record, level),
                buildParentTerytCode(record, level),
                record.nazwa(),
                record.nazDod(),
                level,
                normalizeNullable(record.rodz()),
                parseDate(record.stanNa())
        ));
    }

    private Optional<String> resolveUnitLevel(TercRecord record) {
        if (isBlank(record.woj()) || isBlank(record.nazwa())) {
            return Optional.empty();
        }

        if (isBlank(record.pow())
                && isBlank(record.gmi())
                && isBlank(record.rodz())) {
            return Optional.of(VOIVODESHIP);
        }

        if (!isBlank(record.pow())
                && isBlank(record.gmi())
                && isBlank(record.rodz())) {
            return Optional.of(COUNTY);
        }

        if (!isBlank(record.pow())
                && !isBlank(record.gmi())
                && MUNICIPALITY_TYPES.contains(record.rodz())) {
            return Optional.of(MUNICIPALITY);
        }

        return Optional.empty();
    }

    private String buildTerytCode(TercRecord record, String unitLevel) {
        return switch (unitLevel) {
            case VOIVODESHIP -> record.woj();
            case COUNTY -> record.woj() + record.pow();
            case MUNICIPALITY ->
                    record.woj() + record.pow() + record.gmi() + record.rodz();
            default -> throw new IllegalArgumentException(
                    "Unsupported territorial unit level: " + unitLevel
            );
        };
    }

    private String buildParentTerytCode(TercRecord record, String unitLevel) {
        return switch (unitLevel) {
            case VOIVODESHIP -> null;
            case COUNTY -> record.woj();
            case MUNICIPALITY -> record.woj() + record.pow();
            default -> throw new IllegalArgumentException(
                    "Unsupported territorial unit level: " + unitLevel
            );
        };
    }

    private String normalizeNullable(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDate parseDate(String value) {
        return isBlank(value) ? null : LocalDate.parse(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}