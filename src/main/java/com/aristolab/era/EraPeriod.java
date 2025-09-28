package com.aristolab.era;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a continuous era period with a label and inclusive start/end dates.
 */
public final class EraPeriod {

    private final String label;
    private final LocalDate start;
    private final LocalDate end;

    public EraPeriod(String label, LocalDate start, LocalDate end) {
        this.label = Objects.requireNonNull(label, "label");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }
    }

    public String label() {
        return label;
    }

    public LocalDate start() {
        return start;
    }

    public LocalDate end() {
        return end;
    }

    public long lengthInDays() {
        return end.toEpochDay() - start.toEpochDay() + 1;
    }

    @Override
    public String toString() {
        return label + "(" + start + "-" + end + ")";
    }
}
