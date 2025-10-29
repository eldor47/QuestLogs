package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class QuestTabCompleter implements TabCompleter {
    
    private final QuestLogsPlugin plugin;
    
    public QuestTabCompleter(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // First argument: subcommands
            completions.add("view");
            completions.add("list");
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2) {
            // Second argument: quest IDs for view command
            if (args[0].equalsIgnoreCase("view")) {
                for (Quest quest : plugin.getQuestManager().getAllQuests()) {
                    if (quest.isActive()) {
                        completions.add(quest.getId());
                    }
                }
                return filterCompletions(completions, args[1]);
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


