package com.aristolab.era;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Central registry maintaining {@link EraDefinition} instances.
 */
public final class EraRegistry {

    private static final String DEFAULT_RESOURCE = "/era/era-definitions.json";
    private static final ObjectMapper MAPPER = JacksonSupport.mapper();

    private final List<EraDefinition> definitions;

    private EraRegistry(List<EraDefinition> definitions) {
        this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
    }

    public static EraRegistry defaultRegistry() {
        try (InputStream input = EraRegistry.class.getResourceAsStream(DEFAULT_RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource: " + DEFAULT_RESOURCE);
            }
            List<EraDefinition> data = MAPPER.readValue(input, new TypeReference<List<EraDefinition>>() {});
            data.sort(Comparator.comparing(EraDefinition::startDate));
            return new EraRegistry(data);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load default era definitions", ex);
        }
    }

    public static EraRegistry fromResource(String resourcePath) {
        Objects.requireNonNull(resourcePath, "resourcePath");
        try (InputStream input = EraRegistry.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            List<EraDefinition> data = MAPPER.readValue(input, new TypeReference<List<EraDefinition>>() {});
            data.sort(Comparator.comparing(EraDefinition::startDate));
            return new EraRegistry(data);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to load era definitions from resource", ex);
        }
    }

    public static EraRegistry of(List<EraDefinition> definitions) {
        Objects.requireNonNull(definitions, "definitions");
        List<EraDefinition> copy = new ArrayList<>(definitions);
        copy.sort(Comparator.comparing(EraDefinition::startDate));
        return new EraRegistry(copy);
    }

    public List<EraDefinition> definitions() {
        return definitions;
    }

    public Optional<EraDefinition> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        String normalized = name.strip().toLowerCase(Locale.ROOT);
        return definitions.stream()
                .filter(def -> def.aliases().stream()
                        .map(alias -> alias.toLowerCase(Locale.ROOT))
                        .anyMatch(normalized::equals))
                .findFirst();
    }

    public Optional<EraDefinition> findByDate(LocalDate date) {
        return definitions.stream().filter(def -> def.contains(date)).findFirst();
    }

    public List<EraDefinition> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        return definitions.stream()
                .filter(def -> def.displayName().toLowerCase(Locale.ROOT).contains(normalized)
                        || def.aliases().stream().map(alias -> alias.toLowerCase(Locale.ROOT)).anyMatch(normalized::contains)
                        || def.emperor().toLowerCase(Locale.ROOT).contains(normalized))
                .collect(Collectors.toUnmodifiableList());
    }
}
