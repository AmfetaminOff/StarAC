package io.starac.command;

import io.starac.config.StarConfig;
import io.starac.data.DataManager;
import io.starac.task.TaskManager;
import org.bukkit.command.CommandSender;

public final class StatusCommand implements SubCommand {

    private final StarConfig    config;
    private final DataManager   dataManager;
    private final TaskManager   taskManager;
    private final Runnable      healthCheck;

    private volatile boolean aiHealthy   = false;
    private volatile long    lastChecked = 0L;

    public StatusCommand(StarConfig config, DataManager dataManager,
                         TaskManager taskManager, Runnable healthCheck) {
        this.config      = config;
        this.dataManager = dataManager;
        this.taskManager = taskManager;
        this.healthCheck = healthCheck;
    }

    @Override public String getName()        { return "status"; }
    @Override public String getDescription() { return "Статус плагина и AI сервера"; }
    @Override public String getUsage()       { return "/starac status"; }
    @Override public String getPermission()  { return "starac.admin"; }

    public void setAiHealthy(boolean healthy) {
        this.aiHealthy   = healthy;
        this.lastChecked = System.currentTimeMillis();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender)) return;

        String aiStatus = aiHealthy ? "§aONLINE" : "§cOFFLINE";
        String checkedAgo = lastChecked == 0 ? "never"
                : ((System.currentTimeMillis() - lastChecked) / 1000) + "s ago";

        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§bStarAC §7— Status");
        sender.sendMessage("§7AI Server:   " + aiStatus + " §8(" + config.getAi().getApiUrl() + ")");
        sender.sendMessage("§7Checked:     §f" + checkedAgo);
        sender.sendMessage("§7Tracking:    §f" + dataManager.size() + " players");
        sender.sendMessage("§7Tasks:       §f" + taskManager.getRunningCount() + " running");
        sender.sendMessage("§7Ban thresh:  §f" + String.format("%.0f%%", config.getAi().getBanThreshold() * 100));
        sender.sendMessage("§7Flag thresh: §f" + String.format("%.0f%%", config.getAi().getFlagThreshold() * 100));
        sender.sendMessage("§7Debug:       " + (config.isDebug() ? "§aON" : "§7OFF"));
        sender.sendMessage("§8§m----------------------------------------");
    }
}