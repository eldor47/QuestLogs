package com.questlogs.listeners;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CraftItemListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final Map<UUID, Set<String>> completedQuests; // Track which quests each player has completed
    
    public CraftItemListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.completedQuests = new HashMap<>();
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        try {
            // Null safety checks
            if (plugin == null || plugin.getQuestManager() == null || plugin.getProgressManager() == null) {
                return;
            }
            
            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }
            
            Player player = (Player) event.getWhoClicked();
            UUID playerId = player.getUniqueId();
            ItemStack result = event.getCurrentItem();
        
        if (result == null || result.getType() == Material.AIR) {
            return;
        }
        
        Material material = result.getType();
        String materialName = material.name();
        
        // Calculate how many items are being crafted
        // This handles shift-clicking to craft multiple items
        int craftAmount = result.getAmount();
        if (event.isShiftClick()) {
            // For shift-click, calculate max possible crafts
            craftAmount = getMaxCraftAmount(event, result);
        }
        
        // Track cumulative stats for ALL items crafted (ALWAYS, regardless of quests)
        plugin.getStatsManager().getPlayerStats(playerId).addItemCrafted(materialName, craftAmount);
        
        // Track for challenges
        plugin.getChallengeManager().addProgress(player.getName(), "CRAFT", materialName, craftAmount);
        
        Set<String> playerCompletedQuests = completedQuests.computeIfAbsent(playerId, k -> new HashSet<>());
        
        // Update progress for all active craft item quests
        for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if (quest.getType() != QuestType.CRAFT_ITEMS || !quest.isActive()) {
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
            
            // Check if this item is a target for this quest
            if (!quest.hasBlockTargets()) { // We reuse blockTargets for craft items
                continue;
            }
            
            int targetAmount = quest.getBlockTarget(materialName);
            if (targetAmount <= 0) {
                continue; // This item is not a target for this quest
            }
            
            // Get current progress for this specific item type
            int currentProgress = plugin.getProgressManager().getBlockProgress(playerId, questId, materialName);
            
            // Skip if this item type has already reached its target
            if (currentProgress >= targetAmount) {
                continue;
            }
            
            // Calculate how much progress to add (don't exceed target)
            int progressToAdd = Math.min(craftAmount, targetAmount - currentProgress);
            
            // Add progress for this specific item type
            plugin.getProgressManager().addBlockProgress(playerId, questId, materialName, progressToAdd);
            
            // Get updated progress
            currentProgress = plugin.getProgressManager().getBlockProgress(playerId, questId, materialName);
            
            // Check if this specific item target is complete
            if (currentProgress >= targetAmount) {
                player.sendMessage(ChatColor.GREEN + "✓ Crafted " + targetAmount + " " + 
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
            plugin.getLogger().warning("Error in CraftItemListener: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int getMaxCraftAmount(CraftItemEvent event, ItemStack result) {
        // Calculate maximum number of items that can be crafted
        int maxCraftable = Integer.MAX_VALUE;
        
        // Check each ingredient in the crafting matrix
        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                maxCraftable = Math.min(maxCraftable, ingredient.getAmount());
            }
        }
        
        // Multiply by the result amount (some recipes produce multiple items)
        return maxCraftable * result.getAmount();
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
        
        // Display what was crafted
        player.sendMessage(ChatColor.GRAY + "Items crafted:");
        for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
            String material = entry.getKey();
            int required = entry.getValue();
            int crafted = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), material);
            player.sendMessage(ChatColor.GRAY + "  - " + formatMaterialName(material) + ": " + 
                             ChatColor.WHITE + crafted + ChatColor.GRAY + "/" + required);
        }
        
        // Broadcast completion if enabled
        plugin.getRewardManager().broadcastCompletion(player, quest);
    }
    
    private String formatMaterialName(String material) {
        // Convert IRON_SWORD to Iron Sword
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

