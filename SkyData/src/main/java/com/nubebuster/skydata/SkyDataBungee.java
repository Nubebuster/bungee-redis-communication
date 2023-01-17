package com.nubebuster.skydata;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SkyDataBungee extends Plugin {

    private static SkyDataBungee inst;

    public static SkyDataBungee getInstance() {
        return inst;
    }

    /**
     * @return the core instance of the plugin with all functionality
     **/
    public static SkyDataCore getCore() {
        return SkyDataCore.getInstance();
    }

    @Override
    public void onEnable() {
        inst = this;
        Configuration config;
        try {
            config = loadConfig();
        } catch (IOException e1) {
            e1.printStackTrace();
            getProxy().stop("SkyDataBungee config could not be loaded");
            return;
        }

        RedisMessageStrategy redisMessageStrategy = (message) -> getProxy().getPluginManager().callEvent(new RedisMessageEventBungee(message));

        SkyDataCore.initialize(config.getString("redis.host"), config.getInt("redis.port"),
                config.getString("mysql.host"), config.getInt("mysql.port"), config.getString("mysql.db"),
                config.getString("mysql.user"), config.getString("mysql.password"), redisMessageStrategy);

        //Make sure subscription stays intact using keepalives
        getProxy().getScheduler().runAsync(this, () -> {
            while (true) {
                ScheduledTask task = getProxy().getScheduler().runAsync(this, () -> {
                    try {
                        getCore().subscribe();
                    } catch (JedisConnectionException e) {
                        //Occasionally fails but retries anyway, don't handle
                    }
                });
                while (true) {
                    if (System.currentTimeMillis() - SkyDataCore.getInstance().lastKeepAlive > 15000) {
                        SkyDataCore.getInstance().lastKeepAlive = System.currentTimeMillis();
                        task.cancel();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onDisable() {
        getCore().cleanup();
    }

    private Configuration loadConfig() throws IOException {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
    }

    /**
     * Asynchronously schedule to send a message to all servers on the network
     *
     * @param message payload to publish to all the servers on the network
     */
    public void publish(String message) {
        getProxy().getScheduler().runAsync(this, () -> getCore().publish(message));
    }

}
