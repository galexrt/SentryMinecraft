package me.theminecoder.minecraft.sentry;

import java.util.Map;
import java.util.Map.Entry;

import io.sentry.Sentry;
import io.sentry.SentryLevel;

public class SentryMinecraft {

    public enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL
    }

    public static void init(String dsn, String serverName, Map<String, String> tags) {
        Sentry.init(options -> {
            options.setDsn(dsn);
            options.setAttachServerName(true);
            options.setDiagnosticLevel(SentryLevel.ERROR);
            options.setTag("server-name", serverName);
            for (Entry<String, String> tag : tags.entrySet()) {
                options.setTag(tag.getKey(), tag.getValue());
            }
        });
    }

    public static void captureException(Throwable e) {
        Sentry.captureException(e);
    }

    public static void captureException(Throwable e, Object hint) {
        Sentry.captureException(e, hint);
    }

    public static void captureMessage(String message) {
        Sentry.captureMessage(message);
    }

    public static void captureMessage(String message, Level level) {
        Sentry.captureMessage(message, SentryLevel.valueOf(level.name()));
    }

}
