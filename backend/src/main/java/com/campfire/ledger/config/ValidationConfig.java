package com.campfire.ledger.config;

import com.campfire.ledger.validation.ValidationRegistry;
import com.campfire.validation.api.ValidationRule;
import com.campfire.validation.rules.ValidationRules;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfig {

    private static final Logger log = LoggerFactory.getLogger(ValidationConfig.class);

    @Bean
    public ValidationRegistry validationRegistry() {
        List<ValidationRule<?>> rules = ValidationRules.all();
        log.info("Loaded {} validation rules: {}", rules.size(), rules.stream().map(ValidationRule::id).toList());
        return new ValidationRegistry(rules);
    }
}
