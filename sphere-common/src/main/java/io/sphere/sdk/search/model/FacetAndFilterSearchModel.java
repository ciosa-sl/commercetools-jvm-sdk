package io.sphere.sdk.search.model;

import io.sphere.sdk.search.FilterExpression;
import io.sphere.sdk.search.TermFacetAndFilterExpression;
import io.sphere.sdk.search.TermFacetExpression;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;

abstract class FacetAndFilterSearchModel<T, V> extends SearchModelImpl<T> {
    private final TermFacetExpression<T> facetExpression;
    private final TermFilterSearchModel<T, V> filterSearchModel;

    FacetAndFilterSearchModel(@Nullable final SearchModel<T> parent, final Function<V, String> typeSerializer) {
        super(parent, null);
        this.facetExpression = new TermFacetSearchModel<>(parent, typeSerializer).allTerms();
        this.filterSearchModel = new TermFilterSearchModel<>(parent, typeSerializer);
    }

    /**
     * Generates an expression to select all elements with the given attribute value.
     * For example: filtering by "red" would select only those elements with "red" value.
     * @param value the value to filter by
     * @return a filter expression for the given value
     */
    public TermFacetAndFilterExpression<T> by(final V value) {
        return buildExpression(filterSearchModel.by(value));
    }

    /**
     * Generates an expression to select all elements with attributes matching any of the given values.
     * For example: filtering by ["red", "blue"] would select only those elements with either "red" or "blue" value.
     * @param values the values to filter by
     * @return a filter expression for the given values
     */
    public TermFacetAndFilterExpression<T> byAny(final Iterable<V> values) {
        return validateAndBuildExpression(values, v -> filterSearchModel.byAny(v));
    }

    /**
     * Generates an expression to select all elements with attributes matching all the given values.
     * For example: filtering by ["red", "blue"] would select only those elements with both "red" and "blue" values.
     * @param values the values to filter by
     * @return a filter expression for the given values
     */
    public TermFacetAndFilterExpression<T> byAll(final Iterable<V> values) {
        return validateAndBuildExpression(values, v -> filterSearchModel.byAll(v));
    }

    /**
     * Generates an expression to select all elements with attributes matching any of the given values.
     * For example: filtering by ["red", "blue"] would select only those elements with either "red" or "blue" value.
     * @param values the values to filter by
     * @return a filter expression for the given values
     */
    public TermFacetAndFilterExpression<T> byAnyAsString(final Iterable<String> values) {
        return validateAndBuildExpression(values, v -> filterSearchModel.byAnyAsString(v));
    }

    /**
     * Generates an expression to select all elements with attributes matching all the given values.
     * For example: filtering by ["red", "blue"] would select only those elements with both "red" and "blue" values.
     * @param values the values to filter by
     * @return a filter expression for the given values
     */
    public TermFacetAndFilterExpression<T> byAllAsString(final Iterable<String> values) {
        return validateAndBuildExpression(values, v -> filterSearchModel.byAllAsString(v));
    }

    private TermFacetAndFilterExpression<T> buildExpression(final List<FilterExpression<T>> filterExpressions) {
        return TermFacetAndFilterExpression.of(facetExpression, filterExpressions);
    }

    private <R> TermFacetAndFilterExpression<T> validateAndBuildExpression(final Iterable<R> values,
                                                                           final Function<Iterable<R>, List<FilterExpression<T>>> f) {
        final List<FilterExpression<T>> filterExpressions;
        if (values.iterator().hasNext()) {
            filterExpressions = f.apply(values);
        } else {
            filterExpressions = emptyList();
        }
        return buildExpression(filterExpressions);
    }
}
