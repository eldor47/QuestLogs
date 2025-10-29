package com.questlogs.listeners;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ExplorationListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final Map<UUID, Location> lastLocations;
    private final Map<UUID, Set<String>> completedQuests; // Track which quests each player has completed
    
    public ExplorationListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.lastLocations = new HashMap<>();
        this.completedQuests = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Only process if the player actually moved to a different block
        if (to == null || to.getBlockX() == from.getBlockX() && 
            to.getBlockY() == from.getBlockY() && 
            to.getBlockZ() == from.getBlockZ()) {
            return;
        }
        
        Location lastLocation = lastLocations.get(playerId);
        
        // Check if this is a new block
        if (lastLocation == null || !isSameBlock(lastLocation, to)) {
            lastLocations.put(playerId, to);
            
            // Update quest progress for explore blocks quests
            updateExplorationProgress(player, to);
        }
    }
    
    private boolean isSameBlock(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
    
    private void updateExplorationProgress(Player player, Location location) {
        UUID playerId = player.getUniqueId();
        Set<String> playerCompletedQuests = completedQuests.computeIfAbsent(playerId, k -> new HashSet<>());
        
        // Track cumulative stats
        plugin.getStatsManager().getPlayerStats(playerId).addBlocksExplored(1);
        
        // Track for challenges
        plugin.getChallengeManager().addProgress(player.getName(), "EXPLORE", "ANY", 1);
        
        // Loop through all active EXPLORE_BLOCKS quests
        for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if (quest.getType() != QuestType.EXPLORE_BLOCKS || !quest.isActive()) {
                continue;
            }
            
            String questId = quest.getId();
            
            // Skip if quest is locked (prerequisites not met)
            if (!plugin.getQuestManager().isQuestAvailable(playerId, questId)) {
                continue;
            }
            
            // Skip if player has already completed this quest
            if (playerCompletedQuests.contains(questId)) {
                continue;
            }
            
            int currentProgress = plugin.getProgressManager().getPlayerProgress(playerId, questId);
            
            // Skip if already completed
            if (currentProgress >= quest.getTargetAmount()) {
                playerCompletedQuests.add(questId);
                continue;
            }
            
            // Add 1 to progress
            plugin.getProgressManager().addPlayerProgress(playerId, questId, 1);
            currentProgress++;
            
            // Send progress notifications at intervals
            int notificationInterval = plugin.getConfig().getInt("notification-interval", 100);
            if (currentProgress % notificationInterval == 0 && currentProgress < quest.getTargetAmount()) {
                player.sendMessage("§7[Quest] §e" + quest.getName() + ": §a" + currentProgress + "§7/§f" + quest.getTargetAmount() + " blocks explored");
            }
            
            // Check if quest is complete
            if (currentProgress >= quest.getTargetAmount()) {
                playerCompletedQuests.add(questId);
                // Call quest completion asynchronously to not block the main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    onQuestComplete(player, quest);
                });
            }
        }
    }
    
    private void onQuestComplete(Player player, Quest quest) {
        // Track quest completion in stats
        plugin.getStatsManager().getPlayerStats(player.getUniqueId()).incrementQuestsCompleted();
        
        // Give rewards to player and get formatted reward string
        String rewardText = plugin.getRewardManager().giveRewards(player, quest);
        
        // Show completion message with actual rewards
        player.sendMessage("§6§l=========================");
        player.sendMessage("§6§l  Quest Complete! §a§l✓");
        player.sendMessage("§6§l=========================");
        player.sendMessage("§e" + quest.getName());
        player.sendMessage("§aRewards: §f" + rewardText);
        player.sendMessage("§6§l=========================");
        
        // Broadcast completion if enabled
        plugin.getRewardManager().broadcastCompletion(player, quest);
    }
}




