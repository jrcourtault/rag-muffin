package fr.drjeanjean.ragmuffin;

import fr.drjeanjean.ragmuffin.infra.config.TestcontainersConfig;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.EntityType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie la cohérence bidirectionnelle entre les annotations @Column(nullable)
 * des entités JPA et les contraintes NOT NULL du schéma DB (Liquibase).
 *
 * Hibernate validate ne vérifie que dans un sens (DB NOT NULL → entité nullable = erreur).
 * Ce test couvre l'autre sens (entité nullable=false → DB nullable = erreur).
 */
@SpringBootTest
@Import(TestcontainersConfig.class)
@ActiveProfiles("test")
class SchemaValidationTest {

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void nullabilityMustMatchBetweenEntityAndSchema() throws SQLException {
        var mismatches = new ArrayList<String>();

        for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
            Class<?> javaType = entityType.getJavaType();
            String tableName = resolveTableName(javaType);

            for (Field field : javaType.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }

                String columnName = resolveColumnName(field, column);
                Boolean dbNullable = isColumnNullableInDb(tableName, columnName);
                if (dbNullable == null) {
                    continue; // colonne non trouvée — Hibernate validate s'en charge
                }

                boolean entityNullable = column.nullable();
                if (entityNullable != dbNullable) {
                    mismatches.add(String.format(
                            "%s.%s → @Column(nullable=%s) mais la colonne %s.%s est %s en DB",
                            javaType.getSimpleName(), field.getName(), entityNullable,
                            tableName, columnName, dbNullable ? "nullable" : "NOT NULL"));
                }
            }
        }

        assertThat(mismatches)
                .as("Incohérences nullable entre entités JPA et schéma DB")
                .isEmpty();
    }

    private String resolveTableName(Class<?> javaType) {
        Table table = javaType.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        return javaType.getSimpleName().toLowerCase();
    }

    private String resolveColumnName(Field field, Column column) {
        if (!column.name().isEmpty()) {
            return column.name();
        }
        // Convention JPA : camelCase → snake_case
        return field.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    private Boolean isColumnNullableInDb(String tableName, String columnName) throws SQLException {
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            // PostgreSQL stocke les noms en minuscules
            try (ResultSet rs = metaData.getColumns(null, null,
                    tableName.toLowerCase(), columnName.toLowerCase())) {
                if (rs.next()) {
                    return rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                }
            }
        }
        return null;
    }
}
