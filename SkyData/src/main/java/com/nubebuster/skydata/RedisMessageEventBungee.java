package com.nubebuster.skydata;

import net.md_5.bungee.api.plugin.Event;

public class RedisMessageEventBungee extends Event {

    private final String message;

    public RedisMessageEventBungee(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
