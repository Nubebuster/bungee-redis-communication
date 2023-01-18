package com.nubebuster.skydata;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SkyDataSpigot extends JavaPlugin {

    private static SkyDataSpigot inst;

    public static SkyDataSpigot getInstance() {
        return inst;
    }

    public static SkyDataCore getCore() {
        return SkyDataCore.getInstance();
    }

    @Override
    public void onEnable() {
        inst = this;
        FileConfiguration config = loadConfig();

        RedisMessageStrategy redisMessageStrategy = (message) -> getServer().getPluginManager().callEvent(new RedisMessageEventSpigot(message));

        if (config.getBoolean("mysql.enable")) {
            SkyDataCore.initialize(config.getString("redis.host"), config.getInt("redis.port"),
                    config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.db"),
                    config.getString("mysql.user"), config.getString("mysql.password"), redisMessageStrategy);
        } else {
            SkyDataCore.initialize(config.getString("redis.host"), config.getInt("redis.port"), redisMessageStrategy);
        }

        new BukkitRunnable() {
            public void run() {
                getCore().subscribe();
            }
        }.runTaskAsynchronously(this);
    }

    private FileConfiguration loadConfig() {
        saveDefaultConfig();
        return getConfig();
    }

    /**
     * Asynchronously schedule to send a message to all servers on the network
     *
     * @param message payload to publish to all the servers on the network
     */
    public void publish(String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                getCore().publish(message);
            }
        }.runTaskAsynchronously(this);
    }

    @Override
    public void onDisable() {
        getCore().cleanup();
    }
}
