package io.starac.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

    String getName();
    String getDescription();
    String getUsage();
    String getPermission();

    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    default boolean checkPermission(CommandSender sender) {
        if (sender.hasPermission(getPermission())) return true;
        sender.sendMessage("§c[StarAC] У вас нет прав: §f" + getPermission());
        return false;
    }
}