package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class QuestStatsCommand implements CommandExecutor {
    
    private final QuestLogsPlugin plugin;
    
    public QuestStatsCommand(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // /queststats [player]
        
        Player targetPlayer;
        
        if (args.length == 0) {
            // Show own stats
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player: /queststats <player>");
                return true;
            }
            targetPlayer = (Player) sender;
        } else {
            // Show another player's stats (requires permission)
            if (!sender.hasPermission("questlogs.stats.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to view other players' stats!");
                return true;
            }
            
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return true;
            }
        }
        
        displayStats(sender, targetPlayer);
        return true;
    }
    
    private void displayStats(CommandSender sender, Player targetPlayer) {
        PlayerStats stats = plugin.getStatsManager().getPlayerStats(targetPlayer.getUniqueId());
        
        // Header
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Player Statistics: " + 
                         ChatColor.WHITE + targetPlayer.getName());
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
        
        // Quest stats
        sender.sendMessage(ChatColor.AQUA + "Quest Statistics:");
        sender.sendMessage(ChatColor.GRAY + "  • Quests Completed: " + ChatColor.WHITE + stats.getQuestsCompleted());
        sender.sendMessage("");
        
        // Challenge stats
        sender.sendMessage(ChatColor.AQUA + "Challenge Statistics:");
        sender.sendMessage(ChatColor.GRAY + "  • Challenges Won: " + ChatColor.GOLD + stats.getChallengesWon());
        sender.sendMessage(ChatColor.GRAY + "  • Challenges Participated: " + ChatColor.YELLOW + stats.getChallengesParticipated());
        sender.sendMessage("");
        
        // Playtime stats
        long totalPlaytime = plugin.getPlaytimeListener().getTotalPlaytime(targetPlayer.getUniqueId());
        sender.sendMessage(ChatColor.AQUA + "Playtime:");
        sender.sendMessage(ChatColor.GRAY + "  • Total Time Played: " + ChatColor.WHITE + formatPlaytime(totalPlaytime));
        sender.sendMessage(ChatColor.GRAY + "  • Current Session: " + ChatColor.WHITE + 
                         formatPlaytime(plugin.getPlaytimeListener().getCurrentSessionTime(targetPlayer.getUniqueId())));
        sender.sendMessage("");
        
        // Exploration stats
        sender.sendMessage(ChatColor.AQUA + "Exploration:");
        sender.sendMessage(ChatColor.GRAY + "  • Blocks Explored: " + ChatColor.WHITE + 
                         formatNumber(stats.getTotalBlocksExplored()));
        sender.sendMessage("");
        
        // Mining stats
        sender.sendMessage(ChatColor.AQUA + "Mining:");
        sender.sendMessage(ChatColor.GRAY + "  • Total Blocks Mined: " + ChatColor.WHITE + 
                         formatNumber(stats.getTotalBlocksMined()));
        displayTopStats(sender, stats.getBlocksMined(), "    Top Ores Mined:", 5);
        sender.sendMessage("");
        
        // Breaking stats
        sender.sendMessage(ChatColor.AQUA + "Block Breaking:");
        sender.sendMessage(ChatColor.GRAY + "  • Total Blocks Broken: " + ChatColor.WHITE + 
                         formatNumber(stats.getTotalBlocksBroken()));
        displayTopStats(sender, stats.getBlocksBroken(), "    Top Blocks Broken:", 5);
        sender.sendMessage("");
        
        // Placing stats
        sender.sendMessage(ChatColor.AQUA + "Block Placing:");
        sender.sendMessage(ChatColor.GRAY + "  • Total Blocks Placed: " + ChatColor.WHITE + 
                         formatNumber(stats.getTotalBlocksPlaced()));
        displayTopStats(sender, stats.getBlocksPlaced(), "    Top Blocks Placed:", 5);
        sender.sendMessage("");
        
        // Crafting stats
        sender.sendMessage(ChatColor.AQUA + "Crafting:");
        sender.sendMessage(ChatColor.GRAY + "  • Total Items Crafted: " + ChatColor.WHITE + 
                         formatNumber(stats.getTotalItemsCrafted()));
        displayTopStats(sender, stats.getItemsCrafted(), "    Top Items Crafted:", 5);
        sender.sendMessage("");
        
        // Combat stats
        sender.sendMessage(ChatColor.AQUA + "Combat:");
        sender.sendMessage(ChatColor.GRAY + "  • Total Mobs Killed: " + ChatColor.WHITE + 
                         formatNumber(stats.getTotalMobsKilled()));
        displayTopStats(sender, stats.getMobsKilled(), "    Top Mobs Killed:", 5);
        sender.sendMessage("");
        
        // Activity times
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
        sender.sendMessage(ChatColor.AQUA + "Activity:");
        sender.sendMessage(ChatColor.GRAY + "  • First Seen: " + ChatColor.WHITE + 
                         dateFormat.format(new Date(stats.getFirstSeen())));
        sender.sendMessage(ChatColor.GRAY + "  • Last Seen: " + ChatColor.WHITE + 
                         dateFormat.format(new Date(stats.getLastSeen())));
        
        // Footer
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
    }
    
    private void displayTopStats(CommandSender sender, Map<String, Integer> stats, String header, int limit) {
        if (stats.isEmpty()) {
            return;
        }
        
        // Sort by value (descending)
        List<Map.Entry<String, Integer>> sortedStats = stats.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .collect(Collectors.toList());
        
        if (!sortedStats.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + header);
            for (int i = 0; i < sortedStats.size(); i++) {
                Map.Entry<String, Integer> entry = sortedStats.get(i);
                String formattedName = formatMaterialName(entry.getKey());
                sender.sendMessage(ChatColor.GRAY + "      " + (i + 1) + ". " + 
                                 ChatColor.WHITE + formattedName + ": " + 
                                 ChatColor.YELLOW + formatNumber(entry.getValue()));
            }
        }
    }
    
    private String formatMaterialName(String material) {
        // Convert COAL_ORE to Coal Ore
        String[] parts = material.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
    
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }
    
    /**
     * Format playtime in milliseconds to a human-readable string
     */
    private String formatPlaytime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%dd %dh", days, hours % 24);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}

