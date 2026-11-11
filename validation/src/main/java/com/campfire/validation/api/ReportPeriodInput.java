package com.campfire.validation.api;

import java.time.LocalDate;

/** The date range of a report request. Either date may be null when the caller left it out. */
public record ReportPeriodInput(LocalDate start, LocalDate end) {
}
