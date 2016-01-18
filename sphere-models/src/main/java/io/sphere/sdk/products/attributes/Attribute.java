package io.sphere.sdk.products.attributes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.models.EnumValue;
import io.sphere.sdk.models.LocalizedEnumValue;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.models.Reference;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;

@JsonDeserialize(as = AttributeImpl.class)
public interface Attribute {
    String getName();

    <T> T getValue(final AttributeAccess<T> access);

    static Attribute of(final String name, final JsonNode jsonNode) {
        return new AttributeImpl(name, jsonNode);
    }

    static <T> Attribute of(final String name, final AttributeAccess<T> access, final T value) {
        return of(access.ofName(name), value);
    }

    static <T> Attribute of(final NamedAttributeAccess<T> namedAttributeAccess, final T value) {
        final String name = namedAttributeAccess.getName();
        //here is not the attributeMapper used to keep LocalizedEnum values which
        //are transformed to just the key so the attribute could not be read anymore
        final JsonNode jsonNode = SphereJsonUtils.toJsonNode(value);
        if (value instanceof Reference && jsonNode instanceof ObjectNode) {
            final Reference<?> reference = (Reference<?>) value;
            if (reference.getObj() != null) {
                ((ObjectNode) jsonNode).replace("obj", SphereJsonUtils.toJsonNode(reference.getObj()));
            }
        }
        return of(name, jsonNode);
    }

    default Boolean getValueAsBoolean() {
        return getValue(AttributeAccess.ofBoolean());
    }

    default Set<Boolean> getValueAsBooleanSet() {
        return getValue(AttributeAccess.ofBooleanSet());
    }
    
    default String getValueAsString() {
        return getValue(AttributeAccess.ofString());
    }

    default Set<String> getValueAsStringSet() {
        return getValue(AttributeAccess.ofStringSet());
    }

    default LocalizedString getValueAsLocalizedString() {
        return getValue(AttributeAccess.ofLocalizedString());
    }

    default Set<LocalizedString> getValueAsLocalizedStringSet() {
        return getValue(AttributeAccess.ofLocalizedStringSet());
    }

    default EnumValue getValueAsEnumValue() {
        return getValue(AttributeAccess.ofEnumValue());
    }

    default Set<EnumValue> getValueAsEnumValueSet() {
        return getValue(AttributeAccess.ofEnumValueSet());
    }

    default LocalizedEnumValue getValueAsLocalizedEnumValue() {
        return getValue(AttributeAccess.ofLocalizedEnumValue());
    }

    default Set<LocalizedEnumValue> getValueAsLocalizedEnumValueSet() {
        return getValue(AttributeAccess.ofLocalizedEnumValueSet());
    }

    default Double getValueAsDouble() {
        return getValue(AttributeAccess.ofDouble());
    }

    default Set<Double> getValueAsDoubleSet() {
        return getValue(AttributeAccess.ofDoubleSet());
    }

    default Integer getValueAsInteger() {
        return getValue(AttributeAccess.ofInteger());
    }

    default Set<Integer> getValueAsIntegerSet() {
        return getValue(AttributeAccess.ofIntegerSet());
    }

    default Long getValueAsLong() {
        return getValue(AttributeAccess.ofLong());
    }

    default Set<Long> getValueAsLongSet() {
        return getValue(AttributeAccess.ofLongSet());
    }

    default MonetaryAmount getValueAsMoney() {
        return getValue(AttributeAccess.ofMoney());
    }

    default Set<MonetaryAmount> getValueAsMoneySet() {
        return getValue(AttributeAccess.ofMoneySet());
    }

    default LocalDate getValueAsLocalDate() {
        return getValue(AttributeAccess.ofLocalDate());
    }

    default Set<LocalDate> getValueAsLocalDateSet() {
        return getValue(AttributeAccess.ofLocalDateSet());
    }

    default LocalTime getValueAsLocalTime() {
        return getValue(AttributeAccess.ofLocalTime());
    }

    default Set<LocalTime> getValueAsLocalTimeSet() {
        return getValue(AttributeAccess.ofLocalTimeSet());
    }

    default ZonedDateTime getValueAsDateTime() {
        return getValue(AttributeAccess.ofZonedDateTime());
    }

    default Set<ZonedDateTime> getValueAsDateTimeSet() {
        return getValue(AttributeAccess.ofZonedDateTimeSet());
    }

    default JsonNode getValueAsJsonNode() {
        return getValue(AttributeAccess.ofJsonNode());
    }
}
