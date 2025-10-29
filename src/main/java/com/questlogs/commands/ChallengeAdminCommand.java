package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Challenge;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChallengeAdminCommand implements CommandExecutor {
    
    private final QuestLogsPlugin plugin;
    
    public ChallengeAdminCommand(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("questlogs.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "start":
                return handleStart(sender, args);
            case "stop":
                return handleStop(sender);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleStart(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /challengeadmin start <challenge_id>");
            return true;
        }
        
        String challengeId = args[1];
        
        // Check if challenge already active
        if (plugin.getChallengeManager().isChallengeActive()) {
            Challenge active = plugin.getChallengeManager().getActiveChallenge();
            sender.sendMessage(ChatColor.RED + "A challenge is already active: " + active.getName());
            sender.sendMessage(ChatColor.YELLOW + "Use '/challengeadmin stop' first to end it");
            return true;
        }
        
        // Try to start the challenge
        if (plugin.getChallengeManager().startChallenge(challengeId)) {
            sender.sendMessage(ChatColor.GREEN + "✓ Successfully started challenge: " + challengeId);
        } else {
            sender.sendMessage(ChatColor.RED + "✗ Failed to start challenge: " + challengeId);
            sender.sendMessage(ChatColor.YELLOW + "Use '/challengeadmin list' to see available challenges");
        }
        
        return true;
    }
    
    private boolean handleStop(CommandSender sender) {
        if (!plugin.getChallengeManager().isChallengeActive()) {
            sender.sendMessage(ChatColor.YELLOW + "No active challenge to stop");
            return true;
        }
        
        Challenge active = plugin.getChallengeManager().getActiveChallenge();
        String challengeName = active.getName();
        
        plugin.getChallengeManager().endChallenge();
        sender.sendMessage(ChatColor.GREEN + "✓ Stopped challenge: " + challengeName);
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        plugin.getChallengeManager().loadChallenges();
        
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Available Challenges");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Get challenge IDs from config
        org.bukkit.configuration.ConfigurationSection poolSection = 
            plugin.getChallengeManager().getChallengesConfig().getConfigurationSection("challenge-pool");
        
        if (poolSection == null || poolSection.getKeys(false).isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "No challenges configured");
        } else {
            for (String challengeId : poolSection.getKeys(false)) {
                String name = poolSection.getString(challengeId + ".name", "Unknown");
                String description = poolSection.getString(challengeId + ".description", "");
                String type = poolSection.getString(challengeId + ".type", "");
                String target = poolSection.getString(challengeId + ".target", "");
                int duration = poolSection.getInt(challengeId + ".duration", 300);
                
                sender.sendMessage(ChatColor.AQUA + "• " + ChatColor.WHITE + challengeId);
                sender.sendMessage(ChatColor.GRAY + "  " + name + " - " + description);
                sender.sendMessage(ChatColor.GRAY + "  Type: " + type + " | Target: " + target + 
                                 " | Duration: " + duration + "s");
                sender.sendMessage("");
            }
            
            sender.sendMessage(ChatColor.YELLOW + "Use: " + ChatColor.WHITE + 
                             "/challengeadmin start <challenge_id>");
        }
        
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        plugin.getChallengeManager().loadChallenges();
        sender.sendMessage(ChatColor.GREEN + "✓ Reloaded challenges configuration");
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Challenge Admin Commands");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage(ChatColor.AQUA + "/challengeadmin start <id>" + ChatColor.GRAY + " - Start a specific challenge");
        sender.sendMessage(ChatColor.AQUA + "/challengeadmin stop" + ChatColor.GRAY + " - Stop the active challenge");
        sender.sendMessage(ChatColor.AQUA + "/challengeadmin list" + ChatColor.GRAY + " - List all available challenges");
        sender.sendMessage(ChatColor.AQUA + "/challengeadmin reload" + ChatColor.GRAY + " - Reload challenges config");
        sender.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("");
    }
}


