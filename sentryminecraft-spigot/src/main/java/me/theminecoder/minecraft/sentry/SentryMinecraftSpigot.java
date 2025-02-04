package me.theminecoder.minecraft.sentry;

import org.bukkit.command.CommandException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class SentryMinecraftSpigot extends JavaPlugin {

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (getConfig().getString("default-dsn", "").trim().length() > 0) {
            // I really don't get why they removed this....
            Properties properties = new Properties();
            try (FileReader reader = new FileReader(new File("server.properties"))) {
                properties.load(reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Map<String, String> tags = new HashMap<>();
            for (Map.Entry<String, Object> entry : getConfig().getConfigurationSection("tags").getValues(false)
                    .entrySet()) {
                tags.put(entry.getKey(), entry.getValue().toString());
            }

            SentryMinecraft.init(getConfig().getString("default-dsn"), properties
                    .getProperty("server-id", getConfig().getString("server-name", "Unknown Server"))
                    .replace(" ", "+"), tags);
        }
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onServerException(com.destroystokyo.paper.event.server.ServerExceptionEvent event) {
                handleException(event.getException().getCause());
            }
        }, this);
    }

    private void handleException(Throwable cause) {
        Throwable e = cause;
        if (e.getCause() != null) { // Paper wraps the actual exception
            e = e.getCause();
        }
        if (e instanceof CommandException && e.getCause() != null) {
            e = e.getCause();
        }
        Throwable finalE = e;
        getServer().getScheduler().runTaskAsynchronously(SentryMinecraftSpigot.this,
                () -> SentryMinecraft.captureException(finalE));
    }

}
