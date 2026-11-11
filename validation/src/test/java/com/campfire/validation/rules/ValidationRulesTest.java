package com.campfire.validation.rules;

import static org.assertj.core.api.Assertions.assertThat;

import com.campfire.validation.api.ValidationRule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Guards "wrote a rule but forgot to add it to ValidationRules.all()". */
class ValidationRulesTest {

    @Test
    void everyRuleInThePackageIsListed() throws Exception {
        assertThat(ValidationRules.all().stream().map(Object::getClass).toList())
                .containsExactlyInAnyOrderElementsOf(ruleClassesInPackage());
    }

    @Test
    void ruleIdsAreTheClassNames() {
        assertThat(ValidationRules.all().stream().map(ValidationRule::id).toList())
                .contains("DateRangePresentRule", "EntryBalancedRule");
    }

    /** Enums implementing ValidationRule among the compiled classes next to ValidationRules. */
    private static List<Class<?>> ruleClassesInPackage() throws Exception {
        Path classesRoot = Path.of(ValidationRules.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path packageDir = classesRoot.resolve("com/campfire/validation/rules");
        try (Stream<Path> files = Files.list(packageDir)) {
            List<String> names = files.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".class") && !n.contains("$"))
                    .map(n -> "com.campfire.validation.rules." + n.substring(0, n.length() - ".class".length()))
                    .toList();
            List<Class<?>> rules = new ArrayList<>();
            for (String name : names) {
                Class<?> type = Class.forName(name);
                if (type.isEnum() && ValidationRule.class.isAssignableFrom(type)) {
                    rules.add(type);
                }
            }
            return rules;
        }
    }
}
