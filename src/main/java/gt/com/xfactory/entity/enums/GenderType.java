package gt.com.xfactory.entity.enums;

import java.util.*;

public enum GenderType {
    male("masculino"),
    female("femenino"),
    other("otro"),
    prefer_not_to_say("prefiero_no_decir");

    private final String spanishAlias;

    private static final Map<String, GenderType> LOOKUP = new HashMap<>();

    static {
        for (GenderType type : values()) {
            LOOKUP.put(type.name().toLowerCase(), type);
            LOOKUP.put(type.spanishAlias.toLowerCase(), type);
        }
    }

    GenderType(String spanishAlias) {
        this.spanishAlias = spanishAlias;
    }

    public static GenderType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        GenderType result = LOOKUP.get(value.trim().toLowerCase());
        if (result == null) {
            throw new IllegalArgumentException("Invalid gender value: " + value);
        }
        return result;
    }
}
