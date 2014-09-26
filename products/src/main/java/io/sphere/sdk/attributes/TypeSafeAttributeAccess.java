package io.sphere.sdk.attributes;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.channels.Channel;
import io.sphere.sdk.models.*;
import io.sphere.sdk.products.Product;
import io.sphere.sdk.producttypes.ProductType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Predicate;

import static io.sphere.sdk.models.TypeReferences.*;

public final class TypeSafeAttributeAccess<T> extends Base {
    private final AttributeMapper<T> attributeMapper;
    private final java.util.function.Predicate<AttributeDefinition> canHandle;


    private TypeSafeAttributeAccess(final AttributeMapper<T> attributeMapper, final Predicate<AttributeDefinition> canHandle) {
        this.attributeMapper = attributeMapper;
        this.canHandle = canHandle;
    }

    public static TypeSafeAttributeAccess<Boolean> ofBoolean() {
        return ofPrimitive(booleanTypeReference(), BooleanAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<String> ofString() {
        return ofPrimitive(stringTypeReference(), TextAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<String> ofText() {
        return ofString();
    }

    public static TypeSafeAttributeAccess<LocalizedString> ofLocalizedString() {
        return ofPrimitive(LocalizedString.typeReference(), LocalizedTextAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<PlainEnumValue> ofPlainEnumValue() {
        return ofEnumLike(PlainEnumValue.typeReference(), EnumAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<LocalizedEnumValue> ofLocalizedEnumValue() {
        return ofEnumLike(LocalizedEnumValue.typeReference(), LocalizedEnumAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<Double> ofDouble() {
        return ofPrimitive(doubleTypeReference(), NumberAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<Money> ofMoney() {
        return ofPrimitive(Money.typeReference(), MoneyAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<LocalDate> ofDate() {
        return ofPrimitive(localDateTypeReference(), DateAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<LocalTime> ofTime() {
        return ofPrimitive(localTimeTypeReference(), TimeAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<Instant> ofDateTime() {
        return ofPrimitive(instantTypeReference(), DateTimeAttributeDefinition.class);
    }

    public static TypeSafeAttributeAccess<Reference<Product>> ofProductReference() {
        return OfReferenceType(new TypeReference<Reference<Product>>() {
        }, ReferenceType.ofProduct());
    }

    public static TypeSafeAttributeAccess<Reference<ProductType>> ofProductTypeReference() {
        return OfReferenceType(new TypeReference<Reference<ProductType>>() {
        }, ReferenceType.ofProductType());
    }

    public static TypeSafeAttributeAccess<Reference<Category>> ofCategoryReference() {
        return OfReferenceType(new TypeReference<Reference<Category>>() {
        }, ReferenceType.ofCategory());
    }

    public static TypeSafeAttributeAccess<Reference<Channel>> ofChannelReference() {
        return OfReferenceType(new TypeReference<Reference<Channel>>() {
        }, ReferenceType.ofChannel());
    }

    public <M> AttributeGetterSetter<M, T> getterSetter(final String name) {
        return AttributeGetterSetter.of(name, attributeMapper);
    }

    public <M> AttributeGetter<M, T> getter(final String name) {
        return this.<M>getterSetter(name);
    }

    public <M> AttributeSetter<M, T> setter(final String name) {
        return this.<M>getterSetter(name);
    }

    public AttributeMapper<T> attributeMapper() {
        return attributeMapper;
    }

    public boolean canHandle(final AttributeDefinition attributeDefinition) {
        return canHandle.test(attributeDefinition);
    }

    private static <T extends WithKey> TypeSafeAttributeAccess<T> ofEnumLike(final TypeReference<T> typeReference, final Class<? extends AttributeDefinition> attributeDefinitionClass) {
        final AttributeMapper<T> mapper = new EnumLikeAttributeMapperImpl<>(typeReference);
        return new TypeSafeAttributeAccess<>(mapper, attributeDefinition ->
                attributeDefinitionClass.isAssignableFrom(attributeDefinition.getClass())
        );
    }

    private static <T> TypeSafeAttributeAccess<T> ofPrimitive(final TypeReference<T> typeReference, final Class<? extends AttributeDefinition> attributeDefinitionClass) {
        return new TypeSafeAttributeAccess<>(AttributeMapper.of(typeReference), attributeDefinition -> attributeDefinitionClass.isAssignableFrom(attributeDefinition.getClass()));
    }

    private static <T> TypeSafeAttributeAccess<Reference<T>> OfReferenceType(final TypeReference<Reference<T>> typeReference, final ReferenceType referenceType) {
        final AttributeMapper<Reference<T>> mapper = AttributeMapper.of(typeReference);
        return new TypeSafeAttributeAccess<>(mapper,
                attributeDefinition -> {
                    boolean canHandle = false;
                    if (ReferenceAttributeDefinition.class.isAssignableFrom(attributeDefinition.getClass())) {
                        ReferenceAttributeDefinition casted = (ReferenceAttributeDefinition) attributeDefinition;
                        canHandle = casted.getAttributeType().getReferenceTypeId().equals(referenceType.getReferenceTypeId());
                    }
                    return canHandle;
                });
    }
}
