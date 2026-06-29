package io.starac.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class AlertConfig {

    private final boolean enabled;
    private final boolean broadcastToOps;
    private final String  permission;
    private final boolean logToConsole;
    private final boolean logToFile;
    private final int     alertCooldownMs;

    private AlertConfig(Builder b) {
        this.enabled          = b.enabled;
        this.broadcastToOps   = b.broadcastToOps;
        this.permission       = b.permission;
        this.logToConsole     = b.logToConsole;
        this.logToFile        = b.logToFile;
        this.alertCooldownMs  = b.alertCooldownMs;
    }

    public static AlertConfig load(FileConfiguration cfg) {
        return new Builder()
                .enabled(cfg.getBoolean("alerts.enabled", true))
                .broadcastToOps(cfg.getBoolean("alerts.broadcast-to-ops", true))
                .permission(cfg.getString("alerts.permission", "starac.alerts"))
                .logToConsole(cfg.getBoolean("alerts.log-to-console", true))
                .logToFile(cfg.getBoolean("alerts.log-to-file", false))
                .alertCooldownMs(cfg.getInt("alerts.cooldown-ms", 5000))
                .build();
    }

    public boolean isEnabled()         { return enabled; }
    public boolean isBroadcastToOps()  { return broadcastToOps; }
    public String  getPermission()     { return permission; }
    public boolean isLogToConsole()    { return logToConsole; }
    public boolean isLogToFile()       { return logToFile; }
    public int     getAlertCooldown()  { return alertCooldownMs; }

    public static final class Builder {
        private boolean enabled         = true;
        private boolean broadcastToOps  = true;
        private String  permission      = "starac.alerts";
        private boolean logToConsole    = true;
        private boolean logToFile       = false;
        private int     alertCooldownMs = 5000;

        public Builder enabled(boolean v)         { enabled = v; return this; }
        public Builder broadcastToOps(boolean v)  { broadcastToOps = v; return this; }
        public Builder permission(String v)       { permission = v; return this; }
        public Builder logToConsole(boolean v)    { logToConsole = v; return this; }
        public Builder logToFile(boolean v)       { logToFile = v; return this; }
        public Builder alertCooldownMs(int v)     { alertCooldownMs = v; return this; }
        public AlertConfig build()                { return new AlertConfig(this); }
    }
}