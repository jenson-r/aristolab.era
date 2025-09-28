package com.aristolab.era;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EraConverterTest {

    private final EraRegistry registry = EraRegistry.defaultRegistry();
    private final EraConverter converter = new EraConverter(registry);

    @Test
    void parseEraDate() {
        EraDate date = converter.parse("明永樂五年二月初十日");
        assertEquals("明", date.definition().dynasty());
        assertEquals("永樂", date.definition().eraName());
        assertEquals(5, date.year());
        assertTrue(date.month().isPresent());
        assertEquals(2, date.month().orElseThrow());
        assertTrue(date.day().isPresent());
    }

    @Test
    void convertToGregorianYear() {
        EraDate eraDate = converter.parse("清乾隆四十五年");
        int year = converter.toGregorianYear(eraDate);
        assertEquals(1780, year);
    }

    @Test
    void convertToEra() {
        LocalDate date = LocalDate.of(1875, 10, 10);
        EraDate eraDate = converter.toEra(date).orElseThrow();
        assertEquals("光緒", eraDate.definition().eraName());
        assertEquals(1, eraDate.year());
    }

    @Test
    void chineseNumberParsing() {
        assertEquals(1, ChineseNumberFormatter.parse("元"));
        assertEquals(12, ChineseNumberFormatter.parse("十二"));
        assertEquals(21, ChineseNumberFormatter.parse("二十一"));
        assertEquals(30, ChineseNumberFormatter.parse("卅"));
        assertEquals("一百零二", ChineseNumberFormatter.format(102));
    }

    @Test
    void searchByName() {
        assertTrue(registry.findByName("康熙").isPresent());
        assertFalse(registry.findByName("不存在").isPresent());
    }
}
