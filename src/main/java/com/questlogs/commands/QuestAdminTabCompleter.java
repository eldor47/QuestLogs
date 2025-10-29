package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class QuestAdminTabCompleter implements TabCompleter {
    
    private final QuestLogsPlugin plugin;
    
    public QuestAdminTabCompleter(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: subcommands
            completions.add("reload");
            completions.add("complete");
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2) {
            // Second argument depends on subcommand
            if (args[0].equalsIgnoreCase("complete")) {
                // Show player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    completions.add(player.getName());
                }
                return filterCompletions(completions, args[1]);
            }
        }
        
        if (args.length == 3) {
            // Third argument: quest ID for complete command
            if (args[0].equalsIgnoreCase("complete")) {
                for (Quest quest : plugin.getQuestManager().getAllQuests()) {
                    if (quest.isActive()) {
                        completions.add(quest.getId());
                    }
                }
                return filterCompletions(completions, args[2]);
            }
        }
        
        return completions;
    }
    
    private List<String> filterCompletions(List<String> completions, String input) {
        List<String> filtered = new ArrayList<>();
        String lowercaseInput = input.toLowerCase();
        
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(lowercaseInput)) {
                filtered.add(completion);
            }
        }
        
        return filtered;
    }
}


