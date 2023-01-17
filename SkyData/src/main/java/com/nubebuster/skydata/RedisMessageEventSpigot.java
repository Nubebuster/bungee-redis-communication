package com.nubebuster.skydata;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RedisMessageEventSpigot extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String message;

    public RedisMessageEventSpigot(String message) {
        this.message = message;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
