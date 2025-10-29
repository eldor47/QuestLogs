package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChallengeAdminTabCompleter implements TabCompleter {
    
    private final QuestLogsPlugin plugin;
    
    public ChallengeAdminTabCompleter(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("questlogs.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // First argument: subcommand
            completions = Arrays.asList("start", "stop", "list", "reload");
            
            // Filter by what user has typed
            String partial = args[0].toLowerCase();
            return completions.stream()
                .filter(cmd -> cmd.startsWith(partial))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            // Second argument for 'start': challenge ID
            ConfigurationSection poolSection = 
                plugin.getChallengeManager().getChallengesConfig().getConfigurationSection("challenge-pool");
            
            if (poolSection != null) {
                String partial = args[1].toLowerCase();
                return poolSection.getKeys(false).stream()
                    .filter(id -> id.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
            }
        }
        
        return completions;
    }
}


