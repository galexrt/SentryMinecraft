package me.theminecoder.minecraft.sentry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import io.github.waterfallmc.waterfall.event.ProxyExceptionEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
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

        Map<String, String> tags = new HashMap<>();
        Configuration tagsSection = getConfig().getSection("tags");
        for (String key : getConfig().getSection("tags").getKeys()) {
            tags.put(key, tagsSection.getString(key));
        }

        SentryMinecraft.init(getConfig().getString("default-dsn"),
                getConfig().getString("server-name", "Unknown Server").replace(" ", "+"),
                tags);
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
        getProxy().getScheduler().runAsync(this, () -> SentryMinecraft.captureException(finalE));
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

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException exc) {
            getLogger().severe(exc.getMessage());
            this.enabled = false;
        }
    }

    public Configuration getConfig() {
        return this.config;
    }
}
