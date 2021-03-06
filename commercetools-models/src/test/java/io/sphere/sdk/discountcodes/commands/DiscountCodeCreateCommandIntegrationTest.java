package io.sphere.sdk.discountcodes.commands;

import io.sphere.sdk.cartdiscounts.CartDiscount;
import io.sphere.sdk.cartdiscounts.CartPredicate;
import io.sphere.sdk.discountcodes.DiscountCode;
import io.sphere.sdk.discountcodes.DiscountCodeDraft;
import io.sphere.sdk.discountcodes.queries.DiscountCodeQuery;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.test.IntegrationTest;
import io.sphere.sdk.test.JsonNodeReferenceResolver;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.sphere.sdk.cartdiscounts.CartDiscountFixtures.withCartDiscount;
import static io.sphere.sdk.cartdiscounts.CartDiscountFixtures.withPersistentCartDiscount;
import static io.sphere.sdk.discountcodes.DiscountCodeFixtures.withDiscountCode;
import static io.sphere.sdk.test.SphereTestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DiscountCodeCreateCommandIntegrationTest extends IntegrationTest {

    @BeforeClass
    public static void clean() {
        client().executeBlocking(DiscountCodeQuery.of()
                .withPredicates(m -> m.code().is("demo-discount-code")))
                .getResults()
                .forEach(code -> client().executeBlocking(DiscountCodeDeleteCommand.of(code)));
    }

    @Test
    public void execution() throws Exception {
        withPersistentCartDiscount(client(), cartDiscount -> {
            final String code = randomKey();
            final DiscountCodeDraft draft = DiscountCodeDraft.of(code, cartDiscount)
                    .withName(en(DiscountCodeCreateCommandIntegrationTest.class.getName()))
                    .withDescription(en("sample discount code descr."))
                    .withCartPredicate(CartPredicate.of("1 = 1"))
                    .withIsActive(false)
                    .withMaxApplications(5L)
                    .withMaxApplicationsPerCustomer(1L);
            final DiscountCodeCreateCommand createCommand = DiscountCodeCreateCommand.of(draft)
                    .plusExpansionPaths(m -> m.cartDiscounts());
            final DiscountCode discountCode = client().executeBlocking(createCommand);
            assertThat(discountCode.getCode()).isEqualTo(code);
            assertThat(discountCode.getName()).isEqualTo(en(DiscountCodeCreateCommandIntegrationTest.class.getName()));
            assertThat(discountCode.getDescription()).isEqualTo(en("sample discount code descr."));
            final Reference<CartDiscount> cartDiscountReference = discountCode.getCartDiscounts().get(0);
            assertThat(cartDiscountReference)
                    .isEqualTo(cartDiscount.toReference())
                    .is(expanded());
            assertThat(discountCode.getCartPredicate()).contains("1 = 1");
            assertThat(discountCode.isActive()).isEqualTo(false);
            assertThat(discountCode.getMaxApplications()).isEqualTo(5L);
            assertThat(discountCode.getMaxApplicationsPerCustomer()).isEqualTo(1L);
            //clean up
            client().executeBlocking(DiscountCodeDeleteCommand.of(discountCode));
        });
    }

    @Test
    public void createByJson() {
        final JsonNodeReferenceResolver referenceResolver = new JsonNodeReferenceResolver();
        withCartDiscount(client(), builder -> builder.requiresDiscountCode(true), cartDiscount -> {
            referenceResolver.addResourceByKey("demo-cart-discount", cartDiscount);
            final DiscountCodeDraft draft = draftFromJsonResource("drafts-tests/discountCode.json", DiscountCodeDraft.class, referenceResolver);
            withDiscountCode(client(), draft, discountCode -> {
                assertThat(discountCode.getCode()).isEqualTo("demo-discount-code");
                assertThat(discountCode.getCartDiscounts().get(0)).isEqualTo(cartDiscount.toReference());
                assertThat(discountCode.getCartPredicate()).isEqualTo("lineItemTotal(1 = 1) >  \"10.00 EUR\"");
                assertThat(discountCode.isActive()).isTrue();
            });
        });
    }
}