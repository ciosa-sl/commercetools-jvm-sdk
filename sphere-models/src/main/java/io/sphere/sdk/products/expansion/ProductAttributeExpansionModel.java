package io.sphere.sdk.products.expansion;

import io.sphere.sdk.expansion.ExpansionModel;
import io.sphere.sdk.expansion.ExpansionPath;

import java.util.Optional;

public final class ProductAttributeExpansionModel<T> extends ExpansionModel<T> {
    ProductAttributeExpansionModel(final Optional<String> parentPath, final String path) {
        super(parentPath, Optional.of(path));
    }

    /**
     * {@link ExpansionPath} for flat attribute values like {@link String} and {@link io.sphere.sdk.models.LocalizedStrings}.
     * @return expansion path
     */
    public ExpansionPath<T> value() {
        return expansionPath("value");
    }

    /**
     * {@link ExpansionPath} for set (collection) attribute values.
     * @return expansion path
     */
    public ExpansionPath<T> valueSet() {
        return expansionPath("value[*]");
    }
}
