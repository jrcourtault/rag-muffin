package fr.drjeanjean.ragmuffin.document.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cleanup")
public record CleanupProperties(int errorRetentionDays, String cron) {
}
