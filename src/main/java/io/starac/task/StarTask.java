package io.starac.task;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class StarTask implements Runnable {

    protected final JavaPlugin plugin;
    private BukkitTask task;

    public StarTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startSync(long delayTicks, long periodTicks) {
        cancel();
        task = plugin.getServer().getScheduler()
                .runTaskTimer(plugin, this, delayTicks, periodTicks);
    }

    public void startAsync(long delayTicks, long periodTicks) {
        cancel();
        task = plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(plugin, this, delayTicks, periodTicks);
    }

    public void runLaterSync(long delayTicks) {
        cancel();
        task = plugin.getServer().getScheduler()
                .runTaskLater(plugin, this, delayTicks);
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }

    public abstract String getName();
}