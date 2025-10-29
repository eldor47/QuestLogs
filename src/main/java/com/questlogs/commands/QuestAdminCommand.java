package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class QuestAdminCommand implements CommandExecutor {
    
    private final QuestLogsPlugin plugin;
    
    public QuestAdminCommand(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("questlogs.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                plugin.getQuestManager().loadQuests();
                plugin.getProgressManager().loadProgress();
                sender.sendMessage(ChatColor.GREEN + "Quest data reloaded successfully!");
                break;
            case "complete":
                handleCompleteCommand(sender, args);
                break;
            default:
                sendHelpMessage(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Quest Admin Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin reload - Reload quest configuration");
        sender.sendMessage(ChatColor.YELLOW + "/questadmin complete <player> <quest_id> - Complete a quest for a player");
    }
    
    private void handleCompleteCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /questadmin complete <player> <quest_id>");
            return;
        }
        
        String playerName = args[1];
        String questId = args[2];
        
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + playerName);
            return;
        }
        
        Quest quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null || !quest.isActive()) {
            sender.sendMessage(ChatColor.RED + "Quest not found: " + questId);
            return;
        }
        
        // Check if quest is already complete using the proper method
        if (plugin.getProgressManager().isQuestComplete(target.getUniqueId(), questId)) {
            sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s quest is already completed!");
            return;
        }
        
        // Complete the quest based on type
        if (quest.getType() == QuestType.MINE_BLOCKS && quest.hasBlockTargets()) {
            // Set all block targets to complete
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                plugin.getProgressManager().setBlockProgress(target.getUniqueId(), questId, blockType, required);
            }
        } else if (quest.getType() == QuestType.BREAK_BLOCKS && quest.hasBlockTargets()) {
            // Set all block targets to complete
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                plugin.getProgressManager().setBlockProgress(target.getUniqueId(), questId, blockType, required);
            }
        } else if (quest.getType() == QuestType.PLACE_BLOCKS && quest.hasBlockTargets()) {
            // Set all block targets to complete
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                plugin.getProgressManager().setBlockProgress(target.getUniqueId(), questId, blockType, required);
            }
        } else if (quest.getType() == QuestType.CRAFT_ITEMS && quest.hasBlockTargets()) {
            // Set all item targets to complete (uses blockTargets for items)
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String itemType = entry.getKey();
                int required = entry.getValue();
                plugin.getProgressManager().setBlockProgress(target.getUniqueId(), questId, itemType, required);
            }
        } else if (quest.getType() == QuestType.KILL_MOBS && quest.hasMobTargets()) {
            // Set all mob targets to complete
            for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
                String mobType = entry.getKey();
                int required = entry.getValue();
                plugin.getProgressManager().setMobProgress(target.getUniqueId(), questId, mobType, required);
            }
        } else {
            // Regular quest (exploration, etc.) - set progress to target amount
            plugin.getProgressManager().setPlayerProgress(target.getUniqueId(), questId, quest.getTargetAmount());
        }
        
        // Give rewards and get formatted reward string
        String rewardText = plugin.getRewardManager().giveRewards(target, quest);
        
        // Notify admin
        sender.sendMessage(ChatColor.GREEN + "✓ Completed quest '" + quest.getName() + "' for " + target.getName());
        sender.sendMessage(ChatColor.GRAY + "Rewards given: " + ChatColor.WHITE + rewardText);
        
        // Notify player
        target.sendMessage(ChatColor.GOLD + "=========================");
        target.sendMessage(ChatColor.GOLD + "  Quest Completed! " + ChatColor.GREEN + "✓");
        target.sendMessage(ChatColor.GOLD + "=========================");
        target.sendMessage(ChatColor.YELLOW + quest.getName());
        target.sendMessage(ChatColor.GRAY + "Completed by an administrator");
        target.sendMessage(ChatColor.YELLOW + "Rewards: " + ChatColor.GREEN + rewardText);
        target.sendMessage(ChatColor.GOLD + "=========================");
    }
    
}




