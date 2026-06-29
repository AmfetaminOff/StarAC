package io.starac.task;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class TaskManager {

    private final JavaPlugin plugin;
    private final List<StarTask> tasks = new ArrayList<>();

    public TaskManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(StarTask task) {
        tasks.add(task);
    }

    public void cancelAll() {
        for (StarTask task : tasks) {
            if (task.isRunning()) {
                task.cancel();
                plugin.getLogger().info("[TaskManager] Остановлена задача: " + task.getName());
            }
        }
        tasks.clear();
    }

    public int getRunningCount() {
        return (int) tasks.stream().filter(StarTask::isRunning).count();
    }
}