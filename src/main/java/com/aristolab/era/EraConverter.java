package com.aristolab.era;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High level API for converting between textual era expressions and Gregorian dates.
 */
public final class EraConverter {

    private static final Pattern YEAR_PATTERN = Pattern.compile("([元〇○零一二三四五六七八九十百千兩两廿卅0-9]+)年");
    private static final Pattern MONTH_PATTERN = Pattern.compile("([閏闰]?[元〇○零一二三四五六七八九十百千兩两廿卅0-9]+)月");
    private static final Pattern DAY_PATTERN = Pattern.compile("([初〇○零一二三四五六七八九十百千兩两廿卅0-9]+)[日号]");

    private final EraRegistry registry;

    public EraConverter(EraRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    public EraDate parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text");
        }
        String normalized = text.strip();
        EraDefinition match = findMatchingDefinition(normalized);
        int year = extractYear(normalized);
        Integer month = extractOptional(normalized, MONTH_PATTERN);
        Integer day = extractOptional(normalized, DAY_PATTERN);
        return new EraDate(match, year, month, day);
    }

    public int toGregorianYear(EraDate eraDate) {
        Objects.requireNonNull(eraDate, "eraDate");
        return eraDate.definition().startDate().getYear() + eraDate.year() - 1;
    }

    public Optional<EraDate> toEra(LocalDate date) {
        Objects.requireNonNull(date, "date");
        return registry.findByDate(date).map(def -> {
            int year = def.yearOffset(date) + 1;
            return new EraDate(def, year);
        });
    }

    public LocalDate toGregorianDate(EraDate eraDate) {
        Objects.requireNonNull(eraDate, "eraDate");
        EraDefinition definition = eraDate.definition();
        int yearOffset = eraDate.year() - 1;
        LocalDate base = definition.startDate().plusYears(yearOffset);
        int month = eraDate.month().orElse(base.getMonthValue());
        LocalDate monthAdjusted = LocalDate.of(base.getYear(), month, 1);
        int day = eraDate.day().orElse(Math.min(base.getDayOfMonth(), monthAdjusted.lengthOfMonth()));
        if (day > monthAdjusted.lengthOfMonth()) {
            day = monthAdjusted.lengthOfMonth();
        }
        return LocalDate.of(base.getYear(), month, day);
    }

    public List<EraDefinition> candidates(String text, int limit) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        int max = limit <= 0 ? Integer.MAX_VALUE : limit;
        String normalized = text.strip();
        return registry.definitions().stream()
                .map(def -> new Candidate(def, bestMatchLength(def, normalized)))
                .filter(candidate -> candidate.score > 0)
                .sorted()
                .limit(max)
                .map(candidate -> candidate.definition)
                .toList();
    }

    private EraDefinition findMatchingDefinition(String text) {
        return candidates(text, 1).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to locate era information in text: " + text));
    }

    static int bestMatchLength(EraDefinition def, String text) {
        int best = 0;
        for (String alias : def.aliases()) {
            String candidate = def.dynasty() + alias;
            if (text.contains(candidate) && candidate.length() > best) {
                best = candidate.length();
            }
            if (text.contains(alias) && alias.length() > best) {
                best = alias.length();
            }
        }
        String displayName = def.displayName();
        if (text.contains(displayName) && displayName.length() > best) {
            best = displayName.length();
        }
        return best;
    }

    private record Candidate(EraDefinition definition, int score) implements Comparable<Candidate> {
        @Override
        public int compareTo(Candidate other) {
            int comparison = Integer.compare(other.score, score);
            if (comparison != 0) {
                return comparison;
            }
            comparison = definition.startDate().compareTo(other.definition.startDate());
            if (comparison != 0) {
                return comparison;
            }
            return definition.displayName().compareTo(other.definition.displayName());
        }
    }

    private static int extractYear(String text) {
        Matcher matcher = YEAR_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Era year not found in text: " + text);
        }
        return ChineseNumberFormatter.parse(matcher.group(1));
    }

    private static Integer extractOptional(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1);
            value = value.replace("閏", "").replace("闰", "").replace("初", "");
            try {
                return ChineseNumberFormatter.parse(value);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }
}
