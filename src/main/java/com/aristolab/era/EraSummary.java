package com.aristolab.era;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Lightweight summary of an {@link EraDefinition} suitable for data extraction APIs.
 */
public final class EraSummary {

    private final String displayName;
    private final String dynasty;
    private final String emperor;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int totalYears;
    private final String notes;

    private EraSummary(String displayName, String dynasty, String emperor, LocalDate startDate, LocalDate endDate,
                       int totalYears, String notes) {
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.dynasty = Objects.requireNonNull(dynasty, "dynasty");
        this.emperor = Objects.requireNonNull(emperor, "emperor");
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        this.totalYears = totalYears;
        this.notes = notes == null ? "" : notes;
    }

    public static EraSummary fromDefinition(EraDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        int years = definition.endDate().getYear() - definition.startDate().getYear() + 1;
        return new EraSummary(definition.displayName(), definition.dynasty(), definition.emperor(),
                definition.startDate(), definition.endDate(), years, definition.notes());
    }

    public String displayName() {
        return displayName;
    }

    public String dynasty() {
        return dynasty;
    }

    public String emperor() {
        return emperor;
    }

    public LocalDate startDate() {
        return startDate;
    }

    public LocalDate endDate() {
        return endDate;
    }

    public int totalYears() {
        return totalYears;
    }

    public String notes() {
        return notes;
    }
}
