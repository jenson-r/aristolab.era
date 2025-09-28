package com.aristolab.era;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonSupportTest {

    @Test
    void mapperHandlesLocalDateWithoutExternalModules() throws Exception {
        ObjectMapper mapper = JacksonSupport.mapper();
        LocalDate expected = LocalDate.of(1644, 1, 1);
        String json = mapper.writeValueAsString(Map.of("date", expected));
        Map<String, LocalDate> decoded = mapper.readValue(json, new TypeReference<>() {});
        assertEquals(expected, decoded.get("date"));
    }
}

