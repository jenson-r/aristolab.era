package com.aristolab.era;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.ibm.icu.util.ChineseCalendar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Facade that mirrors the rich helper functions offered by the original JavaScript module.
 */
public final class EraToolkit {

    private static final String NEED_SPLIT_CHARS = "a-zA-Z\\d\\-,'\"\\u0E00-\\u0E7F";
    public static final Pattern NEED_SPLIT_PREFIX = Pattern.compile("^[" + NEED_SPLIT_CHARS + "]");
    public static final Pattern NEED_SPLIT_POSTFIX = Pattern.compile("[" + NEED_SPLIT_CHARS.replace("\\d", "") + "]$");
    private static final Pattern REDUCE_PATTERN = Pattern.compile("([^" + NEED_SPLIT_CHARS
            + "]) ([^" + NEED_SPLIT_CHARS.replace("\\d", "") + "])");
    public static final Pattern PERIOD_PATTERN = Pattern.compile("(\\d{1,4})(?:[年/.-](\\d{1,2})(?:[月/.-](\\d{1,2}))?)?");
    public static final String MINUTE_OFFSET_KEY = "minuteOffset";

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build();

    private final EraRegistry registry;
    private final EraConverter converter;

    public EraToolkit(EraRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.converter = new EraConverter(registry);
    }

    public EraRegistry set(Collection<EraDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        return EraRegistry.of(new ArrayList<>(definitions));
    }

    public String pack(Collection<EraDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(definitions);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to pack era definitions", ex);
        }
    }

    public Optional<EraSummary> extract(String eraName) {
        return registry.findByName(eraName).map(EraSummary::fromDefinition);
    }

    public Map<String, List<EraPeriod>> periods() {
        Map<String, List<EraPeriod>> grouped = new LinkedHashMap<>();
        for (EraDefinition definition : registry.definitions()) {
            grouped.computeIfAbsent(definition.dynasty(), key -> new ArrayList<>())
                    .add(new EraPeriod(definition.displayName(), definition.startDate(), definition.endDate()));
        }
        grouped.replaceAll((key, value) -> Collections.unmodifiableList(value));
        return Collections.unmodifiableMap(grouped);
    }

    public List<EraDefinition> getCandidate(String text, int limit) {
        return converter.candidates(text, limit);
    }

    public List<LocalDate> dates(String eraName) {
        Optional<EraDefinition> definition = registry.findByName(eraName);
        if (definition.isEmpty()) {
            return List.of();
        }
        LocalDate start = definition.get().startDate();
        LocalDate end = definition.get().endDate();
        List<LocalDate> result = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            result.add(cursor);
            cursor = cursor.plusYears(1);
        }
        return Collections.unmodifiableList(result);
    }

    public Pattern eraListPattern() {
        Set<String> aliases = registry.definitions().stream()
                .flatMap(def -> def.aliases().stream())
                .collect(Collectors.toSet());
        String joined = aliases.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        return Pattern.compile(joined);
    }

    public List<EraDefinition> forDynasty(String dynasty) {
        if (dynasty == null || dynasty.isBlank()) {
            return List.of();
        }
        String normalized = dynasty.strip();
        return registry.definitions().stream()
                .filter(def -> def.dynasty().equals(normalized))
                .collect(Collectors.toUnmodifiableList());
    }

    public List<EraDefinition> forMonarch(String monarch) {
        if (monarch == null || monarch.isBlank()) {
            return List.of();
        }
        String normalized = monarch.strip().toLowerCase(Locale.ROOT);
        return registry.definitions().stream()
                .filter(def -> def.emperor().toLowerCase(Locale.ROOT).equals(normalized))
                .collect(Collectors.toUnmodifiableList());
    }

    public int numeralize(String text) {
        return ChineseNumberFormatter.parse(text);
    }

    public String chineseCalendar(LocalDate date) {
        Objects.requireNonNull(date, "date");
        ChineseCalendar calendar = new ChineseCalendar();
        calendar.setTimeInMillis(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        int lunarYear = calendar.get(ChineseCalendar.EXTENDED_YEAR) - 2637;
        int lunarMonth = calendar.get(ChineseCalendar.MONTH) + 1;
        int lunarDay = calendar.get(ChineseCalendar.DAY_OF_MONTH);
        StringBuilder builder = new StringBuilder();
        builder.append(ChineseNumberFormatter.format(lunarYear)).append("年");
        if (calendar.get(ChineseCalendar.IS_LEAP_MONTH) == 1) {
            builder.append("閏");
        }
        builder.append(ChineseNumberFormatter.format(lunarMonth)).append("月");
        builder.append(ChineseNumberFormatter.format(lunarDay)).append("日");
        return builder.toString();
    }

    public String concatName(List<String> parts) {
        StringBuilder builder = new StringBuilder();
        String previous = "";
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (!previous.isEmpty() && NEED_SPLIT_PREFIX.matcher(trimmed).find()
                    && NEED_SPLIT_POSTFIX.matcher(previous).find()) {
                builder.append(' ');
            }
            builder.append(trimmed);
            previous = trimmed;
        }
        return builder.toString();
    }

    public String reduceName(String name) {
        return REDUCE_PATTERN.matcher(name.trim()).replaceAll("$1$2");
    }

    public Comparator<EraDefinition> compareStart() {
        return Comparator.comparing(EraDefinition::startDate);
    }

    public List<EraDefinition> dateOfCEYear(int year) {
        LocalDate january = LocalDate.of(year, 1, 1);
        LocalDate december = LocalDate.of(year, 12, 31);
        return registry.definitions().stream()
                .filter(def -> !def.endDate().isBefore(january) && !def.startDate().isAfter(december))
                .sorted(compareStart())
                .collect(Collectors.toUnmodifiableList());
    }

    public EraDate nodeEra(String text) {
        return converter.parse(text);
    }

    public List<EraDate> setupNodes(Collection<String> texts) {
        Objects.requireNonNull(texts, "texts");
        List<EraDate> result = new ArrayList<>();
        for (String text : texts) {
            result.add(nodeEra(text));
        }
        return Collections.unmodifiableList(result);
    }

    public String toHtml(EraDate date) {
        Objects.requireNonNull(date, "date");
        String base = date.toText();
        return "<span class=\"era\" data-dynasty=\"" + date.definition().dynasty()
                + "\" data-era=\"" + date.definition().eraName() + "\">" + base + "</span>";
    }

    public Optional<String> noteNode(EraDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        String notes = definition.notes();
        if (notes == null || notes.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(notes);
    }

    public EraDate toEraDate(String text) {
        return nodeEra(text);
    }

    public LocalDate toGregorian(EraDate date) {
        return converter.toGregorianDate(date);
    }

    public EraConverter converter() {
        return converter;
    }
}
