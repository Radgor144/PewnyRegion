package com.pewnyregion.region.data.service.component;

import com.pewnyregion.region.data.service.model.BdlUnit;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BdlUnitTerytMapper {

    public Optional<String> map(BdlUnit unit) {
        String id = unit.id();

        if (id == null || !id.matches("\\d{12}")) {
            throw new IllegalArgumentException(
                    "Invalid BDL unit ID: " + id
            );
        }

        return switch (unit.level()) {
            case 2 -> Optional.of(id.substring(2, 4));
            case 5 -> Optional.of(
                    id.substring(2, 4) + id.substring(7, 9)
            );
            case 6 -> Optional.of(
                    id.substring(2, 4) + id.substring(7, 12)
            );
            default -> Optional.empty();
        };
    }
}