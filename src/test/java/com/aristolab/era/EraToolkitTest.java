package com.aristolab.era;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class EraToolkitTest {

    private EraToolkit toolkit;
    private EraDefinition hongwu;
    private EraDefinition jianwen;

    @BeforeEach
    void setUp() {
        hongwu = new EraDefinition(
                "明",
                "朱元璋",
                "洪武",
                List.of("洪武", "太祖洪武"),
                LocalDate.of(1368, 1, 23),
                LocalDate.of(1398, 2, 12),
                "開國之年"
        );
        jianwen = new EraDefinition(
                "明",
                "朱允炆",
                "建文",
                List.of("建文"),
                LocalDate.of(1399, 7, 30),
                LocalDate.of(1402, 7, 25),
                "燕王靖難"
        );
        EraRegistry registry = EraRegistry.of(List.of(hongwu, jianwen));
        toolkit = new EraToolkit(registry);
    }

    @Test
    void parsesEraDates() {
        EraDate date = toolkit.toEraDate("明洪武十五年八月初三日");
        assertEquals(15, date.year());
        assertEquals(Optional.of(8), date.month());
        assertEquals(Optional.of(3), date.day());
        LocalDate gregorian = toolkit.toGregorian(date);
        assertEquals(1382, gregorian.getYear());
    }

    @Test
    void packsAndExtractsData() {
        String packed = toolkit.pack(List.of(hongwu));
        assertTrue(packed.contains("洪武"));
        Optional<EraSummary> summary = toolkit.extract("洪武");
        assertTrue(summary.isPresent());
        assertEquals("明洪武", summary.get().displayName());
    }

    @Test
    void providesCandidatesAndSearches() {
        List<EraDefinition> candidates = toolkit.getCandidate("洪武十五年", 5);
        assertEquals(1, candidates.size());
        assertEquals(hongwu, candidates.getFirst());
        List<EraDefinition> dynastyEras = toolkit.forDynasty("明");
        assertEquals(2, dynastyEras.size());
        List<EraDefinition> monarchEras = toolkit.forMonarch("朱允炆");
        assertEquals(List.of(jianwen), monarchEras);
    }

    @Test
    void enumeratesPeriodsAndDates() {
        Map<String, List<EraPeriod>> periods = toolkit.periods();
        assertEquals(1, periods.size());
        assertTrue(periods.containsKey("明"));
        List<LocalDate> dates = toolkit.dates("洪武");
        assertFalse(dates.isEmpty());
        assertEquals(LocalDate.of(1368, 1, 23), dates.getFirst());
    }

    @Test
    void supportsNameFormattingUtilities() {
        String joined = toolkit.concatName(List.of("大明", "洪武", "十五年"));
        assertTrue(joined.contains("洪武"));
        String reduced = toolkit.reduceName("大明 洪武 十五年");
        assertEquals("大明洪武十五年", reduced);
        assertEquals(15, toolkit.numeralize("十五"));
    }

    @Test
    void exposesPatternsAndComparators() {
        Pattern pattern = toolkit.eraListPattern();
        assertTrue(pattern.matcher("洪武").find());
        List<EraDefinition> yearResults = toolkit.dateOfCEYear(1399);
        assertEquals(1, yearResults.size());
        assertEquals(jianwen, yearResults.getFirst());
    }

    @Test
    void rendersHtmlAndNotes() {
        EraDate date = new EraDate(hongwu, 10);
        String html = toolkit.toHtml(date);
        assertTrue(html.contains("data-era=\"洪武\""));
        assertEquals("開國之年", toolkit.noteNode(hongwu).orElse(null));
    }

    @Test
    void convertsToChineseCalendar() {
        String lunar = toolkit.chineseCalendar(LocalDate.of(2024, 2, 10));
        assertTrue(lunar.contains("年"));
        assertTrue(lunar.contains("月"));
    }

    @Test
    void setupNodesParsesBatch() {
        List<EraDate> nodes = toolkit.setupNodes(List.of("明洪武二年", "明建文二年"));
        assertEquals(2, nodes.size());
        assertEquals(2, nodes.get(1).year());
    }
}
