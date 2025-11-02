package com.questlogs.listeners;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlockPlaceListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final Map<UUID, Set<String>> completedQuests; // Track which quests each player has completed
    
    public BlockPlaceListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.completedQuests = new HashMap<>();
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        try {
            // Null safety checks
            if (plugin == null || plugin.getQuestManager() == null || plugin.getProgressManager() == null) {
                return;
            }
            
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            Block block = event.getBlock();
            Material material = block.getType();
            String materialName = material.name();
            
            // Track cumulative stats for ALL blocks placed (ALWAYS, regardless of quests)
            plugin.getStatsManager().getPlayerStats(playerId).addBlockPlaced(materialName, 1);
            
            // Track for challenges
            plugin.getChallengeManager().addProgress(player.getName(), "PLACE", materialName, 1);
            
            Set<String> playerCompletedQuests = completedQuests.computeIfAbsent(playerId, k -> new HashSet<>());
        
        // Update progress for all active block place quests
        for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if (quest.getType() != QuestType.PLACE_BLOCKS || !quest.isActive()) {
                continue;
            }
            
            String questId = quest.getId();
            
            // Skip if quest is locked (prerequisites not met)
            if (!plugin.getQuestManager().isQuestAvailable(playerId, questId)) {
                continue;
            }
            
            // Skip if player has already completed this quest (check both memory and saved data)
            if (playerCompletedQuests.contains(questId)) {
                continue;
            }
            
            // Check if quest is already complete in saved data (prevents overfilling after restart)
            if (plugin.getProgressManager().isQuestComplete(playerId, questId)) {
                playerCompletedQuests.add(questId);
                continue;
            }
            
            // Check if this block is a target for this quest
            if (!quest.hasBlockTargets()) {
                continue;
            }
            
            int targetAmount = quest.getBlockTarget(materialName);
            if (targetAmount <= 0) {
                continue; // This block is not a target for this quest
            }
            
            // Get current progress for this specific block type
            int currentProgress = plugin.getProgressManager().getBlockProgress(playerId, questId, materialName);
            
            // Skip if this block type has already reached its target
            if (currentProgress >= targetAmount) {
                continue;
            }
            
            // Add progress for this specific block type
            plugin.getProgressManager().addBlockProgress(playerId, questId, materialName, 1);
            
            // Get updated progress
            currentProgress = plugin.getProgressManager().getBlockProgress(playerId, questId, materialName);
            
            // Check if this specific block target is complete
            if (currentProgress == targetAmount) {
                player.sendMessage(ChatColor.GREEN + "✓ Placed " + currentProgress + " " + 
                                 formatMaterialName(materialName) + " for quest: " + ChatColor.GOLD + quest.getName());
            }
            
            // Check if the entire quest is complete
            if (isQuestComplete(player, quest)) {
                playerCompletedQuests.add(questId);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onQuestComplete(player, quest);
                });
            }
        }
        } catch (Exception e) {
            plugin.getLogger().warning("Error in BlockPlaceListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private boolean isQuestComplete(Player player, Quest quest) {
        Map<String, Integer> targets = quest.getBlockTargets();
        
        for (Map.Entry<String, Integer> entry : targets.entrySet()) {
            String material = entry.getKey();
            int required = entry.getValue();
            int current = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), material);
            
            if (current < required) {
                return false;
            }
        }
        
        return true;
    }
    
    private void onQuestComplete(Player player, Quest quest) {
        // Double-check to avoid duplicate completion messages
        if (!isQuestComplete(player, quest)) {
            return;
        }
        
        // Track quest completion in stats
        plugin.getStatsManager().getPlayerStats(player.getUniqueId()).incrementQuestsCompleted();
        
        // Give rewards to player and get formatted reward string
        String rewardText = plugin.getRewardManager().giveRewards(player, quest);
        
        player.sendMessage(ChatColor.GOLD + "=========================");
        player.sendMessage(ChatColor.GOLD + "  Quest Complete! " + ChatColor.GREEN + "✓");
        player.sendMessage(ChatColor.GOLD + "=========================");
        player.sendMessage(ChatColor.YELLOW + "Quest: " + ChatColor.WHITE + quest.getName());
        player.sendMessage(ChatColor.YELLOW + "Rewards: " + ChatColor.GREEN + rewardText);
        player.sendMessage(ChatColor.GOLD + "=========================");
        
        // Display what was placed
        player.sendMessage(ChatColor.GRAY + "Blocks placed:");
        for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
            String material = entry.getKey();
            int required = entry.getValue();
            int placed = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), material);
            player.sendMessage(ChatColor.GRAY + "  - " + formatMaterialName(material) + ": " + 
                             ChatColor.WHITE + placed + ChatColor.GRAY + "/" + required);
        }
        
        // Broadcast completion if enabled
        plugin.getRewardManager().broadcastCompletion(player, quest);
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
}

