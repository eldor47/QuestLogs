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
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlockBreakListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final Map<UUID, Set<String>> completedQuests; // Track which quests each player has completed
    
    public BlockBreakListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.completedQuests = new HashMap<>();
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Wrap in try-catch to ensure we never prevent block breaking due to errors
        try {
            // Null safety checks
            if (plugin == null || plugin.getQuestManager() == null || plugin.getProgressManager() == null) {
                return; // Silently return if plugin not fully initialized
            }
            
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            Block block = event.getBlock();
            Material material = block.getType();
            String materialName = material.name();
            
            // Track cumulative stats for ALL blocks broken (ALWAYS, regardless of quests)
            // We'll categorize as "broken" by default, but check if it's mining-related
            boolean isMiningBlock = isMiningOre(materialName);
            if (isMiningBlock) {
                plugin.getStatsManager().getPlayerStats(playerId).addBlockMined(materialName, 1);
                // Track for challenges
                plugin.getChallengeManager().addProgress(player.getName(), "MINE", materialName, 1);
            } else {
                plugin.getStatsManager().getPlayerStats(playerId).addBlockBroken(materialName, 1);
                // Track for challenges
                plugin.getChallengeManager().addProgress(player.getName(), "BREAK", materialName, 1);
            }
            
            Set<String> playerCompletedQuests = completedQuests.computeIfAbsent(playerId, k -> new HashSet<>());
            
            // Update progress for all active mining and break block quests
            for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if ((quest.getType() != QuestType.MINE_BLOCKS && quest.getType() != QuestType.BREAK_BLOCKS) || !quest.isActive()) {
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
                player.sendMessage(ChatColor.GREEN + "✓ Mined " + currentProgress + " " + 
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
            // Log error but don't prevent block breaking
            plugin.getLogger().warning("Error in BlockBreakListener: " + e.getMessage());
            e.printStackTrace();
            // Event is NOT cancelled - blocks can still be broken
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
        
        // Display what was mined/broken
        String label = quest.getType() == com.questlogs.models.QuestType.MINE_BLOCKS ? "Blocks mined:" : "Blocks broken:";
        player.sendMessage(ChatColor.GRAY + label);
        for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
            String material = entry.getKey();
            int required = entry.getValue();
            int amount = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), material);
            player.sendMessage(ChatColor.GRAY + "  - " + formatMaterialName(material) + ": " + 
                             ChatColor.WHITE + amount + ChatColor.GRAY + "/" + required);
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
    
    private boolean isMiningOre(String materialName) {
        // Check if this is an ore or mining-related block
        return materialName.contains("_ORE") || 
               materialName.equals("STONE") || 
               materialName.equals("DEEPSLATE") ||
               materialName.equals("DIORITE") ||
               materialName.equals("ANDESITE") ||
               materialName.equals("GRANITE") ||
               materialName.equals("COBBLESTONE") ||
               materialName.equals("NETHERRACK") ||
               materialName.equals("END_STONE") ||
               materialName.equals("OBSIDIAN");
    }
}

