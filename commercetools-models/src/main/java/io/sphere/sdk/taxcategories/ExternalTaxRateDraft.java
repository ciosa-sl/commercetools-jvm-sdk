package io.sphere.sdk.taxcategories;

import com.neovisionaries.i18n.CountryCode;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

public interface ExternalTaxRateDraft {
    String getName();

    /**
     * Percentage in the range of [0..1]. Must be supplied if no `subRates` are specified.
     * @return amount or null
     */
    @Nullable
    BigDecimal getAmount();

    CountryCode getCountry();

    @Nullable
    String getState();

    /**
     * For countries (e.g. the US) where the total tax is a combination of multiple taxes (e.g. state and local taxes).
     * @return sub rates or null
     */
    @Nullable
    List<SubRate> getSubRates();
}
