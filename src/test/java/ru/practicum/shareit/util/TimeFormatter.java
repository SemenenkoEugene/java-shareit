package ru.practicum.shareit.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeFormatter {

    public static final DateTimeFormatter JSON_TEST_DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static LocalDateTime getCurrentTimeWithoutNano() {
        return LocalDateTime.now().withNano(0);
    }
}
