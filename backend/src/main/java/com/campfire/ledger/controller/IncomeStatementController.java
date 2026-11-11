package com.campfire.ledger.controller;

import com.campfire.ledger.exception.InvalidRequestException;
import com.campfire.ledger.model.dto.IncomeStatementResponse;
import com.campfire.ledger.service.IncomeStatementService;
import com.campfire.ledger.validation.ValidationRegistry;
import com.campfire.validation.api.ReportPeriodInput;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncomeStatementController {

    private static final Logger log = LoggerFactory.getLogger(IncomeStatementController.class);

    private final IncomeStatementService incomeStatementService;
    private final ValidationRegistry validationRegistry;

    public IncomeStatementController(IncomeStatementService incomeStatementService,
                                     ValidationRegistry validationRegistry) {
        this.incomeStatementService = incomeStatementService;
        this.validationRegistry = validationRegistry;
    }

    /** Params are optional at the binding level so the validation rules own the "missing" message. */
    @GetMapping("/income-statement")
    public IncomeStatementResponse getIncomeStatement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("GET /income-statement start={} end={}", start, end);

        List<String> errors = validationRegistry.validate(new ReportPeriodInput(start, end));
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(errors);
        }
        return incomeStatementService.getIncomeStatement(start, end);
    }
}
