package com.aristolab.era;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a specific date within a historical era.
 */
public final class EraDate {

    private final EraDefinition definition;
    private final int year;
    private final Integer month;
    private final Integer day;

    public EraDate(EraDefinition definition, int year) {
        this(definition, year, null, null);
    }

    public EraDate(EraDefinition definition, int year, Integer month, Integer day) {
        if (year < 1) {
            throw new IllegalArgumentException("Era year must be >= 1");
        }
        this.definition = Objects.requireNonNull(definition, "definition");
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public EraDefinition definition() {
        return definition;
    }

    public int year() {
        return year;
    }

    public Optional<Integer> month() {
        return Optional.ofNullable(month);
    }

    public Optional<Integer> day() {
        return Optional.ofNullable(day);
    }

    public String toText() {
        StringBuilder builder = new StringBuilder();
        builder.append(definition.dynasty()).append(definition.eraName()).append(ChineseNumberFormatter.format(year)).append("年");
        month().ifPresent(m -> builder.append(ChineseNumberFormatter.format(m)).append("月"));
        day().ifPresent(d -> builder.append(ChineseNumberFormatter.format(d)).append("日"));
        return builder.toString();
    }

    @Override
    public String toString() {
        return toText();
    }
}
