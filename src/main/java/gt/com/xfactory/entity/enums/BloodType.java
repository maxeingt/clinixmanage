package gt.com.xfactory.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BloodType {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    UNKNOWN("unknown");

    private final String value;

    BloodType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static BloodType fromValue(String value) {
        for (BloodType type : BloodType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
