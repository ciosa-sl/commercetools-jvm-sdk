package io.sphere.sdk.categories;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.categories.commands.CategoryCreateCommand;
import io.sphere.sdk.categories.commands.CategoryDeleteCommand;
import io.sphere.sdk.categories.commands.CategoryUpdateCommand;
import io.sphere.sdk.categories.commands.updateactions.SetCustomField;
import io.sphere.sdk.categories.commands.updateactions.SetCustomType;
import io.sphere.sdk.categories.queries.CategoryByIdGet;
import io.sphere.sdk.client.ErrorResponseException;
import io.sphere.sdk.expansion.ExpansionPath;
import io.sphere.sdk.json.TypeReferences;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.models.TextInputHint;
import io.sphere.sdk.models.errors.RequiredField;
import io.sphere.sdk.test.IntegrationTest;
import io.sphere.sdk.types.*;
import io.sphere.sdk.types.commands.TypeCreateCommand;
import io.sphere.sdk.types.commands.TypeDeleteCommand;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static io.sphere.sdk.categories.CategoryFixtures.withCategory;
import static io.sphere.sdk.test.SphereTestUtils.*;
import static io.sphere.sdk.types.TypeFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThatThrownBy;

public class CategoriesCustomFieldsTest extends IntegrationTest {
    public static final Map<String, Object> CUSTOM_FIELDS_MAP = Collections.singletonMap(STRING_FIELD_NAME, "hello");
    public static final TypeReference<Reference<Category>> TYPE_REFERENCE = new TypeReference<Reference<Category>>() {
    };

    @Test
    public void createCategoryWithCustomType() {
        withUpdateableType(client(), type -> {
            final CustomFieldsDraft customFieldsDraft = CustomFieldsDraftBuilder.ofType(type).addObject(STRING_FIELD_NAME, "a value").build();
            final CategoryDraft categoryDraft = CategoryDraftBuilder.of(randomSlug(), randomSlug()).custom(customFieldsDraft).build();
            final Category category = execute(CategoryCreateCommand.of(categoryDraft));
            assertThat(category.getCustom().getField(STRING_FIELD_NAME, TypeReferences.stringTypeReference())).isEqualTo("a value");

            final Category updatedCategory = execute(CategoryUpdateCommand.of(category, SetCustomField.ofObject(STRING_FIELD_NAME, "a new value")));
            assertThat(updatedCategory.getCustom().getField(STRING_FIELD_NAME, TypeReferences.stringTypeReference())).isEqualTo("a new value");

            //clean up
            execute(CategoryDeleteCommand.of(updatedCategory));
            return type;
        });
    }

    @Test
    public void setCustomType() {
        withUpdateableType(client(), type -> {
           withCategory(client(), category -> {
               final Category updatedCategory = execute(CategoryUpdateCommand.of(category, SetCustomType.ofTypeIdAndObjects(type.getId(), CUSTOM_FIELDS_MAP)));
               assertThat(updatedCategory.getCustom().getType()).isEqualTo(type.toReference());
               assertThat(updatedCategory.getCustom().getField(STRING_FIELD_NAME, TypeReferences.stringTypeReference())).isEqualTo("hello");

               final Category updated2 = execute(CategoryUpdateCommand.of(updatedCategory, SetCustomType.ofRemoveType()));
               assertThat(updated2.getCustom()).isNull();
           });
            return type;
        });
    }

    @Test
    public void referenceExpansion() {
        withUpdateableType(client(), type -> {
           withCategory(client(), referencedCategory -> {
               withCategory(client(), category -> {
                   final Map<String, Object> fields = Collections.singletonMap(CAT_REFERENCE_FIELD_NAME, referencedCategory.toReference());
                   final CategoryUpdateCommand categoryUpdateCommand = CategoryUpdateCommand.of(category, SetCustomType.ofTypeIdAndObjects(type.getId(), fields));
                   final ExpansionPath<Category> expansionPath = ExpansionPath.of("custom.fields." + CAT_REFERENCE_FIELD_NAME);
                   final Category updatedCategory = execute(categoryUpdateCommand.withExpansionPaths(expansionPath));

                   final Reference<Category> createdReference = updatedCategory.getCustom().getField(CAT_REFERENCE_FIELD_NAME, TYPE_REFERENCE);
                   assertThat(createdReference).isEqualTo(referencedCategory.toReference());
                   assertThat(createdReference.getObj()).isNotNull();

                   final Category loadedCat = execute(CategoryByIdGet.of(updatedCategory)
                           .withExpansionPaths(expansionPath));

                   assertThat(loadedCat.getCustom().getField(CAT_REFERENCE_FIELD_NAME, TYPE_REFERENCE).getObj())
                           .overridingErrorMessage("is expanded")
                           .isNotNull();


                   final Category updated2 = execute(CategoryUpdateCommand.of(updatedCategory, SetCustomType.ofRemoveType()));
                   assertThat(updated2.getCustom()).isNull();
               });
           });
            return type;
        });
    }

    @Test
    public void requiredValidation() {
        final FieldDefinition stringFieldDefinition =
                FieldDefinition.of(StringType.of(), STRING_FIELD_NAME, en("label"), true, TextInputHint.SINGLE_LINE);
        final String typeKey = randomKey();
        final TypeDraft typeDraft = TypeDraftBuilder.of(typeKey, en("name of the custom type"), TYPE_IDS)
                .description(en("description"))
                .fieldDefinitions(asList(stringFieldDefinition))
                .build();
        final Type type = execute(TypeCreateCommand.of(typeDraft));

        withCategory(client(), category -> {
            assertThatThrownBy(() -> execute(CategoryUpdateCommand.of(category, SetCustomType.ofTypeIdAndObjects(type.getId(), Collections.emptyMap()))))
            .isInstanceOf(ErrorResponseException.class)
                    .matches(e -> {
                        final ErrorResponseException errorResponseException = (ErrorResponseException) e;
                        final String errorCode = RequiredField.CODE;
                        return errorResponseException.hasErrorCode(errorCode)
                                && errorResponseException.getErrors().stream()
                                .filter(err -> err.getCode().equals(errorCode))
                                .anyMatch(err -> STRING_FIELD_NAME.equals(err.as(RequiredField.class).getField()));
                    });
        });

        execute(TypeDeleteCommand.of(type));
    }
}
