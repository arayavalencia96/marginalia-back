package com.marginalia.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.exception.InvalidContentBlockException;

public final class StructuredBlockContent {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StructuredBlockContent() {
    }

    public static JsonNode parse(ContentBlockType type, String content) {
        try {
            JsonNode data = MAPPER.readTree(content);
            if (data == null || !data.isArray() || data.isEmpty() || data.size() > 100) {
                throw new IllegalArgumentException();
            }
            int columns = type == ContentBlockType.TABLE ? data.get(0).size() : 0;
            if (type == ContentBlockType.TABLE && (columns < 1 || columns > 20)) {
                throw new IllegalArgumentException();
            }
            for (JsonNode item : data) {
                if (type == ContentBlockType.TABLE) {
                    if (!item.isArray() || item.size() != columns) {
                        throw new IllegalArgumentException();
                    }
                    for (JsonNode cell : item) {
                        if (!cell.isTextual() || cell.textValue().length() > 10000) {
                            throw new IllegalArgumentException();
                        }
                    }
                } else if (!item.path("text").isTextual() || item.path("text").asText().isBlank()
                        || !item.path("checked").isBoolean()) {
                    throw new IllegalArgumentException();
                }
            }
            return data;
        } catch (Exception exception) {
            throw new InvalidContentBlockException("Invalid " + type + " content");
        }
    }
}
