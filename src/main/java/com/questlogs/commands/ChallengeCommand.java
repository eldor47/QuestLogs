package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Challenge;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ChallengeCommand implements CommandExecutor {
    
    private final QuestLogsPlugin plugin;
    
    public ChallengeCommand(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Challenge activeChallenge = plugin.getChallengeManager().getActiveChallenge();
        
        if (activeChallenge == null || !activeChallenge.isActive()) {
            sender.sendMessage(ChatColor.YELLOW + "No active challenge at the moment!");
            sender.sendMessage(ChatColor.GRAY + "Challenges start automatically. Stay tuned!");
            return true;
        }
        
        // Display challenge info
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Active Challenge");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.AQUA + activeChallenge.getName());
        sender.sendMessage(ChatColor.GRAY + activeChallenge.getDescription());
        sender.sendMessage("");
        sender.sendMessage(ChatColor.YELLOW + "Time Remaining: " + ChatColor.WHITE + 
                         formatTime(activeChallenge.getRemainingSeconds()));
        sender.sendMessage("");
        
        // Show leaderboard
        List<Map.Entry<String, Integer>> leaderboard = plugin.getChallengeManager().getLeaderboard();
        if (leaderboard.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No participants yet!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Current Leaderboard:");
            for (int i = 0; i < Math.min(10, leaderboard.size()); i++) {
                Map.Entry<String, Integer> entry = leaderboard.get(i);
                String medal = getMedal(i + 1);
                ChatColor color = getPlaceColor(i + 1);
                sender.sendMessage(color + "  " + medal + " " + entry.getKey() + 
                                 ChatColor.GRAY + " - " + ChatColor.YELLOW + entry.getValue());
            }
            
            // Show player's position if not in top 10
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String playerName = player.getName();
                int playerPosition = -1;
                int playerScore = 0;
                
                for (int i = 0; i < leaderboard.size(); i++) {
                    if (leaderboard.get(i).getKey().equals(playerName)) {
                        playerPosition = i + 1;
                        playerScore = leaderboard.get(i).getValue();
                        break;
                    }
                }
                
                if (playerPosition > 10) {
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GRAY + "Your position: " + ChatColor.WHITE + 
                                     "#" + playerPosition + ChatColor.GRAY + " - " + 
                                     ChatColor.YELLOW + playerScore);
                } else if (playerPosition == -1) {
                    sender.sendMessage("");
                    sender.sendMessage(ChatColor.GRAY + "You haven't participated yet!");
                }
            }
        }
        
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
        
        return true;
    }
    
    private String getMedal(int place) {
        switch (place) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return String.valueOf(place) + ".";
        }
    }
    
    private ChatColor getPlaceColor(int place) {
        switch (place) {
            case 1: return ChatColor.GOLD;
            case 2: return ChatColor.GRAY;
            case 3: return ChatColor.YELLOW;
            default: return ChatColor.WHITE;
        }
    }
    
    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0) {
            return minutes + "m " + secs + "s";
        }
        return secs + "s";
    }
}


