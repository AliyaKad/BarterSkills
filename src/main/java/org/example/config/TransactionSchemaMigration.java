package org.example.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hibernate ddl-auto=update не обновляет NOT NULL и CHECK-ограничения на существующих таблицах.
 * Старая схема transactions могла требовать deal_id, не допускать BONUS в type и т.д.
 */
@Component
public class TransactionSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    public TransactionSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void migrate() {
        allowNullableDealId();
        dropObsoleteCheckConstraints();
    }

    private void allowNullableDealId() {
        try {
            jdbcTemplate.execute("ALTER TABLE transactions ALTER COLUMN deal_id SET NULL");
        } catch (Exception ignored) {
            // уже nullable
        }
    }

    private void dropObsoleteCheckConstraints() {
        dropConstraintByName("CONSTRAINT_FE");

        List<String> checkNames = findCheckConstraintNames();
        for (String name : checkNames) {
            dropConstraintByName(name);
        }
    }

    private List<String> findCheckConstraintNames() {
        try {
            return jdbcTemplate.queryForList(
                    """
                    SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.CONSTRAINTS
                    WHERE CONSTRAINT_TYPE = 'CHECK' AND UPPER(TABLE_NAME) = 'TRANSACTIONS'
                    """,
                    String.class);
        } catch (Exception e) {
            try {
                return jdbcTemplate.queryForList(
                        """
                        SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
                        WHERE CONSTRAINT_TYPE = 'CHECK' AND UPPER(TABLE_NAME) = 'TRANSACTIONS'
                        """,
                        String.class);
            } catch (Exception e2) {
                return List.of();
            }
        }
    }

    private void dropConstraintByName(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        try {
            jdbcTemplate.execute("ALTER TABLE transactions DROP CONSTRAINT \"" + name + "\"");
        } catch (Exception ignored) {
            try {
                jdbcTemplate.execute("ALTER TABLE transactions DROP CONSTRAINT " + name);
            } catch (Exception ignored2) {
                // ограничение уже удалено
            }
        }
    }
}
