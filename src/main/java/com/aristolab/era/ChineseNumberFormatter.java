package com.aristolab.era;

import java.util.Map;

/**
 * Utility for parsing and formatting common Chinese numerals.
 */
final class ChineseNumberFormatter {

    private static final Map<Character, Integer> DIGITS = Map.ofEntries(
            Map.entry('零', 0),
            Map.entry('〇', 0),
            Map.entry('○', 0),
            Map.entry('一', 1),
            Map.entry('二', 2),
            Map.entry('三', 3),
            Map.entry('四', 4),
            Map.entry('五', 5),
            Map.entry('六', 6),
            Map.entry('七', 7),
            Map.entry('八', 8),
            Map.entry('九', 9),
            Map.entry('兩', 2),
            Map.entry('两', 2),
            Map.entry('十', 10),
            Map.entry('百', 100),
            Map.entry('千', 1000),
            Map.entry('萬', 10000),
            Map.entry('廿', 20),
            Map.entry('卅', 30)
    );

    private ChineseNumberFormatter() {
    }

    static int parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Chinese numeral is empty");
        }
        String trimmed = text.strip();
        if (trimmed.equals("元")) {
            return 1;
        }
        if (trimmed.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(trimmed);
        }
        int result = 0;
        int current = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            Integer value = DIGITS.get(ch);
            if (value == null) {
                if (Character.isDigit(ch)) {
                    current = current * 10 + Character.getNumericValue(ch);
                    continue;
                }
                throw new IllegalArgumentException("Unsupported Chinese numeral: " + ch + " in " + text);
            }
            if (value >= 10) {
                int unit = value;
                if (value == 20 || value == 30) {
                    // 廿 or 卅 express a direct value.
                    result += value;
                    current = 0;
                    continue;
                }
                if (current == 0) {
                    current = 1;
                }
                result += current * unit;
                current = 0;
            } else {
                current = current * 10 + value;
            }
        }
        return result + current;
    }

    static String format(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("number must be positive: " + number);
        }
        if (number < 10) {
            return switch (number) {
                case 1 -> "一";
                case 2 -> "二";
                case 3 -> "三";
                case 4 -> "四";
                case 5 -> "五";
                case 6 -> "六";
                case 7 -> "七";
                case 8 -> "八";
                case 9 -> "九";
                default -> throw new IllegalStateException();
            };
        }
        if (number < 20) {
            if (number == 10) {
                return "十";
            }
            return "十" + format(number - 10);
        }
        if (number < 100) {
            int tens = number / 10;
            int remainder = number % 10;
            String prefix = (tens == 1 ? "十" : format(tens) + "十");
            return remainder == 0 ? prefix : prefix + format(remainder);
        }
        if (number < 1000) {
            int hundreds = number / 100;
            int remainder = number % 100;
            String prefix = format(hundreds) + "百";
            if (remainder == 0) {
                return prefix;
            }
            if (remainder < 10) {
                return prefix + "零" + format(remainder);
            }
            String remainderText = format(remainder);
            if (remainder >= 10 && remainder < 20) {
                remainderText = "一" + remainderText;
            }
            return prefix + remainderText;
        }
        if (number < 10000) {
            int thousands = number / 1000;
            int remainder = number % 1000;
            String prefix = format(thousands) + "千";
            if (remainder == 0) {
                return prefix;
            }
            if (remainder < 100) {
                if (remainder < 10) {
                    return prefix + "零" + format(remainder);
                }
                return prefix + "零" + format(remainder);
            }
            return prefix + format(remainder);
        }
        throw new IllegalArgumentException("number too large to format: " + number);
    }
}
