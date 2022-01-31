package me.theminecoder.minecraft.sentry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import io.github.waterfallmc.waterfall.event.ProxyExceptionEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public final class SentryMinecraftBungeecord extends Plugin implements Listener {

    private boolean enabled = true;

    private Configuration config;

    @Override
    public void onLoad() {
        try {
            Class.forName("io.github.waterfallmc.waterfall.event.ProxyExceptionEvent");
        } catch (ClassNotFoundException e) {
            getLogger().severe(
                    "SentryMinecraft no longer supports BungeeCord plugin manager wrapping. Please use Waterfall for global exception handling");
            this.enabled = false;
            return;
        }

        loadConfig();

        SentryMinecraft.init(ClassLoader.getSystemClassLoader(), getConfig().getString("default-dsn"),
                SentryConfigurationOptions.Builder.create()
                        .withServerName(getConfig().getString("server-name", "Unknown Server")
                                .replace(" ", "+"))
                        .asDefaultClient()
                        .build());
    }

    @Override
    public void onEnable() {
        if (!this.enabled)
            return;

        this.getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void exceptionEvent(ProxyExceptionEvent event) {
        this.handleException(event.getException());
    }

    private void handleException(Throwable cause) {
        Throwable e = cause;
        if (e.getCause() != null) { // Waterfall wraps the actual exception
            e = e.getCause();
        }
        Throwable finalE = e;
        getProxy().getScheduler().runAsync(this, () -> SentryMinecraft.sendIfActive(finalE));
    }

    private void loadConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Configuration getConfig() {
        return this.config;
    }
}
