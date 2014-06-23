package io.sphere.sdk.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

final public class JsonUtils {
    private static ObjectMapper objectMapper = newObjectMapper();

    private JsonUtils() {
    }

    public static ObjectMapper newObjectMapper() {
        return new ObjectMapper().registerModule(new GuavaModule()).registerModule(new Iso8601DateTimeJacksonModule());
    }

    public static String toJson(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /** Pretty prints given JSON string, replacing passwords by {@code 'xxxxx'}.
     * @param json JSON code as String which should be formatted
     * @return <code>json</code> formatted
     * @throws java.io.IOException if <code>json</code> is invalid JSON
     */
    public static String prettyPrintJsonStringSecure(String json) {
        try {
            ObjectMapper jsonParser = new ObjectMapper();
            JsonNode jsonTree = jsonParser.readValue(json, JsonNode.class);
            secure(jsonTree);
            ObjectWriter writer = jsonParser.writerWithDefaultPrettyPrinter();
            return writer.writeValueAsString(jsonTree);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }
    }

    public static <T> T readObjectFromJsonFileInClasspath(final String resourcePath, final TypeReference<T> typeReference) {
        final URL url = Resources.getResource(resourcePath);
        try {
            String jsonAsString = Resources.toString(url, Charsets.UTF_8);
            return readObjectFromJsonString(typeReference, jsonAsString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readObjectFromJsonString(TypeReference<T> typeReference, String jsonAsString) {
        try {
            return objectMapper.readValue(jsonAsString, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /** Very simple way to "erase" passwords -
     *  replaces all field values whose names contains {@code 'pass'} by {@code 'xxxxx'}. */
    private static JsonNode secure(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode)node;
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue().isTextual() && field.getKey().toLowerCase().contains("pass")) {
                    objectNode.put(field.getKey(), "xxxxx");
                } else {
                    secure(field.getValue());
                }
            }
            return objectNode;
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode)node;
            Iterator<JsonNode> elements = arrayNode.elements();
            while (elements.hasNext()) {
                secure(elements.next());
            }
            return arrayNode;
        } else {
            return node;
        }
    }
}
