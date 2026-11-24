package fr.drjeanjean.ragmuffin.document.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.cleanup")
public record CleanupProperties(int errorRetentionDays, String cron, Duration pendingTimeout, String pendingCron) {
}
