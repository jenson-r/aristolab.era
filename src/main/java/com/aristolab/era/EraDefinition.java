package com.aristolab.era;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Immutable representation of a historical era definition.
 */
public final class EraDefinition {

    private final String dynasty;
    private final String emperor;
    private final String eraName;
    private final List<String> aliases;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String notes;

    @JsonCreator
    public EraDefinition(
            @JsonProperty(value = "dynasty", required = true) String dynasty,
            @JsonProperty(value = "emperor", required = true) String emperor,
            @JsonProperty(value = "eraName", required = true) String eraName,
            @JsonProperty(value = "aliases") List<String> aliases,
            @JsonProperty(value = "startDate", required = true) LocalDate startDate,
            @JsonProperty(value = "endDate", required = true) LocalDate endDate,
            @JsonProperty(value = "notes") String notes) {
        this.dynasty = Objects.requireNonNull(dynasty, "dynasty");
        this.emperor = Objects.requireNonNull(emperor, "emperor");
        this.eraName = Objects.requireNonNull(eraName, "eraName");
        this.startDate = Objects.requireNonNull(startDate, "startDate");
        this.endDate = Objects.requireNonNull(endDate, "endDate");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before startDate");
        }
        if (aliases == null || aliases.isEmpty()) {
            List<String> fallback = new ArrayList<>();
            fallback.add(eraName);
            this.aliases = Collections.unmodifiableList(fallback);
        } else {
            List<String> normalized = new ArrayList<>();
            normalized.add(eraName);
            for (String alias : aliases) {
                if (alias != null && !alias.isBlank()) {
                    normalized.add(alias.trim());
                }
            }
            this.aliases = Collections.unmodifiableList(normalized);
        }
        this.notes = notes == null ? "" : notes.trim();
    }

    @JsonProperty("dynasty")
    public String dynasty() {
        return dynasty;
    }

    @JsonProperty("emperor")
    public String emperor() {
        return emperor;
    }

    @JsonProperty("eraName")
    public String eraName() {
        return eraName;
    }

    @JsonProperty("aliases")
    public List<String> aliases() {
        return aliases;
    }

    @JsonProperty("startDate")
    public LocalDate startDate() {
        return startDate;
    }

    @JsonProperty("endDate")
    public LocalDate endDate() {
        return endDate;
    }

    @JsonProperty("notes")
    public String notes() {
        return notes;
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public boolean matchesName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = normalize(name);
        return aliases.stream().map(EraDefinition::normalize).anyMatch(normalized::equals);
    }

    public String displayName() {
        return dynasty + eraName;
    }

    public int yearOffset(LocalDate date) {
        if (!contains(date)) {
            throw new IllegalArgumentException("Date is outside of era range: " + date);
        }
        return date.getYear() - startDate.getYear();
    }

    private static String normalize(String name) {
        return name.strip().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "EraDefinition{" +
               "dynasty='" + dynasty + '\'' +
               ", emperor='" + emperor + '\'' +
               ", eraName='" + eraName + '\'' +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EraDefinition that)) return false;
        return dynasty.equals(that.dynasty)
                && emperor.equals(that.emperor)
                && eraName.equals(that.eraName)
                && startDate.equals(that.startDate)
                && endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dynasty, emperor, eraName, startDate, endDate);
    }
}
