package io.starac.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlertsToggleCommand implements SubCommand {

    private final Set<UUID> muted = ConcurrentHashMap.newKeySet();

    @Override public String getName()        { return "alerts"; }
    @Override public String getDescription() { return "Включить/выключить алерты"; }
    @Override public String getUsage()       { return "/starac alerts"; }
    @Override public String getPermission()  { return "starac.alerts"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender)) return;

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c[StarAC] Только для игроков.");
            return;
        }

        UUID uuid = player.getUniqueId();

        if (muted.contains(uuid)) {
            muted.remove(uuid);
            player.sendMessage("§a[StarAC] Алерты §fвключены§a.");
        } else {
            muted.add(uuid);
            player.sendMessage("§7[StarAC] Алерты §fвыключены§7.");
        }
    }

    public boolean isMuted(UUID uuid) {
        return muted.contains(uuid);
    }
}