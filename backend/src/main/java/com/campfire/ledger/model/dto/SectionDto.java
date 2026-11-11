package com.campfire.ledger.model.dto;

import com.campfire.ledger.model.StatementSection;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.List;

/** Must match Section in api/openapi.yaml. */
public record SectionDto(
        StatementSection key,
        String title,
        List<LineDto> lines,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal total) {
}
