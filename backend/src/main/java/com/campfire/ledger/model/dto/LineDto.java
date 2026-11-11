package com.campfire.ledger.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;

/** Must match Line in api/openapi.yaml. */
public record LineDto(
        String accountNumber,
        String accountName,
        @JsonFormat(shape = JsonFormat.Shape.STRING) BigDecimal amount) {
}
