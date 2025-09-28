# Era Converter

A modern Java library for converting between Gregorian dates and traditional Chinese historical era dates.

## Features

- Immutable domain model for dynasties, emperors and era definitions.
- Registry backed by JSON resources for easy data maintenance.
- Conversion helpers to map between `LocalDate` and textual era expressions.
- Extensible parser that understands common Chinese numerals and idioms.
- Toolkit facade that mirrors the historic JavaScript module utilities (candidate lookups, packing, calendar extraction, HTML helpers).
- Comprehensive unit tests covering parsing, lookups and conversions.

## Getting Started

Add the library to your Maven project:

```xml
<dependency>
  <groupId>com.aristolab</groupId>
  <artifactId>era-converter</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Example

```java
EraRegistry registry = EraRegistry.defaultRegistry();
EraConverter converter = new EraConverter(registry);

EraDate eraDate = converter.parse("明永樂五年");
int gregorianYear = converter.toGregorianYear(eraDate);
// gregorianYear == 1407

Optional<EraDate> reverse = converter.toEra(LocalDate.of(1407, 5, 1));
reverse.ifPresent(date -> System.out.println(date.toText()));

EraToolkit toolkit = new EraToolkit(registry);
toolkit.getCandidate("永樂五年", 3).forEach(System.out::println);
toolkit.chineseCalendar(LocalDate.of(1407, 5, 1));
```

The default registry now ships with a comprehensive catalogue of nearly one thousand era definitions spanning the Western Han through late Qing regimes (including short-lived claimants). To extend or replace the data set, provide your own JSON file and construct an `EraRegistry` with `EraRegistry.fromResource`.

### Mirroring the Legacy JavaScript API

The legacy project exposed a large collection of helper functions such as `to_era_Date`, `get_candidate`, `pack_era`, `era_list`, `node_era`, and HTML rendering utilities. The new `EraToolkit` class provides first-class Java equivalents for each of those capabilities so that downstream consumers can continue to:

- Parse era strings, enumerate candidates, and convert to Gregorian dates.
- Pack or extract registry definitions as JSON payloads.
- Group eras by dynasty/monarch and query periods or CE-year intersections.
- Render annotated HTML spans, notes, and Chinese lunisolar calendar labels.

See `EraToolkit` and the accompanying tests for end-to-end examples of each ported function.

## Building

```bash
mvn clean verify
```

## License

This project is licensed under the MIT License.
