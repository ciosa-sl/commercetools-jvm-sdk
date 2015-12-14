package io.sphere.sdk.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.type.TypeReference;

public class EnumValue extends Base implements WithKey {
    private final String key;
    private final String label;

    @JsonCreator
    private EnumValue(final String key, final String label) {
        this.key = key;
        this.label = label;
    }

    @Override
    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    /**
     *
     * @param key The key of the value used as a programmatic identifier, e.g. in facets &amp; filters.
     * @param label A descriptive label of the value.
     * @return EnumValue
     */
    public static EnumValue of(final String key, final String label) {
        return new EnumValue(key, label);
    }

    public static TypeReference<EnumValue> typeReference() {
        return new TypeReference<EnumValue>() {
            @Override
            public String toString() {
                return "TypeReference<EnumValue>";
            }
        };
    }
}