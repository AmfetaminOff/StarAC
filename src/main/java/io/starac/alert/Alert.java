package io.starac.alert;

import java.util.List;
import java.util.UUID;

public final class Alert {

    private final UUID       playerUuid;
    private final String     playerName;
    private final AlertType  type;
    private final double     score;
    private final List<String> flags;
    private final String     checkName;
    private final long       timestamp;

    private Alert(Builder b) {
        this.playerUuid  = b.playerUuid;
        this.playerName  = b.playerName;
        this.type        = b.type;
        this.score       = b.score;
        this.flags       = List.copyOf(b.flags);
        this.checkName   = b.checkName;
        this.timestamp   = System.currentTimeMillis();
    }

    public UUID         getPlayerUuid() { return playerUuid; }
    public String       getPlayerName() { return playerName; }
    public AlertType    getType()       { return type; }
    public double       getScore()      { return score; }
    public List<String> getFlags()      { return flags; }
    public String       getCheckName()  { return checkName; }
    public long         getTimestamp()  { return timestamp; }

    public static final class Builder {
        private UUID         playerUuid;
        private String       playerName;
        private AlertType    type       = AlertType.INFO;
        private double       score      = 0.0;
        private List<String> flags      = List.of();
        private String       checkName  = "Unknown";

        public Builder player(UUID uuid, String name) {
            this.playerUuid = uuid;
            this.playerName = name;
            return this;
        }

        public Builder type(AlertType v)        { this.type = v; return this; }
        public Builder score(double v)          { this.score = v; return this; }
        public Builder flags(List<String> v)    { this.flags = v; return this; }
        public Builder checkName(String v)      { this.checkName = v; return this; }

        public Alert build() {
            if (playerUuid == null || playerName == null)
                throw new IllegalStateException("player uuid/name required");
            return new Alert(this);
        }
    }
}