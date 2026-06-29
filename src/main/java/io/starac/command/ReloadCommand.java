package io.starac.command;

import io.starac.config.StarConfig;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class ReloadCommand implements SubCommand {

    private final StarConfig config;
    private final Runnable onReload;

    public ReloadCommand(StarConfig config, Runnable onReload) {
        this.config   = config;
        this.onReload = onReload;
    }

    @Override public String getName()        { return "reload"; }
    @Override public String getDescription() { return "Перезагрузить конфиг плагина"; }
    @Override public String getUsage()       { return "/starac reload"; }
    @Override public String getPermission()  { return "starac.admin"; }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender)) return;

        sender.sendMessage("§7[StarAC] Перезагружаю конфиг...");

        try {
            config.load();
            onReload.run();
            sender.sendMessage("§a[StarAC] Конфиг перезагружен успешно.");
        } catch (Exception e) {
            sender.sendMessage("§c[StarAC] Ошибка при перезагрузке: §f" + e.getMessage());
        }
    }
}