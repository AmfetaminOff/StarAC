package io.starac.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class StarCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public void register(SubCommand sub) {
        subCommands.put(sub.getName().toLowerCase(), sub);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String name = args[0].toLowerCase();

        if (name.equals("help")) {
            sendHelp(sender);
            return true;
        }

        SubCommand sub = subCommands.get(name);

        if (sub == null) {
            sender.sendMessage("§c[StarAC] Неизвестная подкоманда: §f" + args[0]);
            sender.sendMessage("§7Используй §f/starac help§7 для списка команд.");
            return true;
        }

        String[] subArgs = new String[args.length - 1];
        System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        sub.execute(sender, subArgs);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            result.add("help");
            subCommands.values().stream()
                    .filter(s -> sender.hasPermission(s.getPermission()))
                    .map(SubCommand::getName)
                    .filter(n -> n.startsWith(args[0].toLowerCase()))
                    .forEach(result::add);
            return result;
        }

        if (args.length >= 2) {
            SubCommand sub = subCommands.get(args[0].toLowerCase());
            if (sub != null && sender.hasPermission(sub.getPermission())) {
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, subArgs.length);
                return sub.tabComplete(sender, subArgs);
            }
        }

        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------------------------------");
        sender.sendMessage("§bStarAC §7— Commands");
        sender.sendMessage("§f/starac help §7— Эта справка");

        subCommands.values().stream()
                .filter(s -> sender.hasPermission(s.getPermission()))
                .forEach(s -> sender.sendMessage(
                        "§f" + s.getUsage() + " §8— §7" + s.getDescription()
                ));

        sender.sendMessage("§8§m----------------------------------------");
    }
}