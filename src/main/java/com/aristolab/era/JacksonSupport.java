package com.aristolab.era;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Centralizes Jackson configuration and ensures LocalDate support even when
 * the optional jackson-datatype-jsr310 module is unavailable at runtime.
 */
final class JacksonSupport {

    private static final ObjectMapper MAPPER = createMapper();

    private JacksonSupport() {
    }

    static ObjectMapper mapper() {
        return MAPPER;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        if (!supportsLocalDate(mapper)) {
            mapper.registerModule(createLocalDateFallbackModule());
        }
        return mapper;
    }

    private static boolean supportsLocalDate(ObjectMapper mapper) {
        JavaType type = mapper.getTypeFactory().constructType(LocalDate.class);
        return mapper.canSerialize(LocalDate.class) && mapper.canDeserialize(type);
    }

    private static SimpleModule createLocalDateFallbackModule() {
        SimpleModule module = new SimpleModule("EraLocalDateFallback");
        module.addSerializer(LocalDate.class, new JsonSerializer<>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value == null) {
                    serializers.defaultSerializeNull(gen);
                    return;
                }
                gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
        module.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
            @Override
            public LocalDate deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
                if (parser.hasToken(JsonToken.VALUE_STRING)) {
                    String text = parser.getValueAsString();
                    if (text == null || text.isBlank()) {
                        return null;
                    }
                    try {
                        return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException ex) {
                        throw ctxt.weirdStringException(text, LocalDate.class, "Expected ISO-8601 local date");
                    }
                }
                if (parser.hasToken(JsonToken.VALUE_NULL)) {
                    return null;
                }
                return (LocalDate) ctxt.handleUnexpectedToken(LocalDate.class, parser);
            }
        });
        return module;
    }
}

