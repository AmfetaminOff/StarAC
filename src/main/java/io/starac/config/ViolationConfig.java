package io.starac.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class ViolationConfig {

    private final int    maxVl;
    private final int    vlDecayTicks;
    private final String punishCommand;
    private final boolean banEnabled;
    private final boolean kickEnabled;
    private final String  kickMessage;

    private ViolationConfig(Builder b) {
        this.maxVl          = b.maxVl;
        this.vlDecayTicks   = b.vlDecayTicks;
        this.punishCommand  = b.punishCommand;
        this.banEnabled     = b.banEnabled;
        this.kickEnabled    = b.kickEnabled;
        this.kickMessage    = b.kickMessage;
    }

    public static ViolationConfig load(FileConfiguration cfg) {
        return new Builder()
                .maxVl(cfg.getInt("violations.max-vl", 20))
                .vlDecayTicks(cfg.getInt("violations.vl-decay-ticks", 100))
                .punishCommand(cfg.getString("violations.punish-command", "ban %player% [StarAC] Cheat detected"))
                .banEnabled(cfg.getBoolean("violations.ban-enabled", false))
                .kickEnabled(cfg.getBoolean("violations.kick-enabled", true))
                .kickMessage(cfg.getString("violations.kick-message", "§c[StarAC] You have been removed for cheating."))
                .build();
    }

    public int    getMaxVl()          { return maxVl; }
    public int    getVlDecayTicks()   { return vlDecayTicks; }
    public String getPunishCommand()  { return punishCommand; }
    public boolean isBanEnabled()     { return banEnabled; }
    public boolean isKickEnabled()    { return kickEnabled; }
    public String  getKickMessage()   { return kickMessage; }

    public static final class Builder {
        private int    maxVl          = 20;
        private int    vlDecayTicks   = 100;
        private String punishCommand  = "ban %player% [StarAC] Cheat detected";
        private boolean banEnabled    = false;
        private boolean kickEnabled   = true;
        private String  kickMessage   = "§c[StarAC] You have been removed for cheating.";

        public Builder maxVl(int v)            { maxVl = v; return this; }
        public Builder vlDecayTicks(int v)     { vlDecayTicks = v; return this; }
        public Builder punishCommand(String v) { punishCommand = v; return this; }
        public Builder banEnabled(boolean v)   { banEnabled = v; return this; }
        public Builder kickEnabled(boolean v)  { kickEnabled = v; return this; }
        public Builder kickMessage(String v)   { kickMessage = v; return this; }
        public ViolationConfig build()         { return new ViolationConfig(this); }
    }
}