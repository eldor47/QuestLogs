package com.questlogs.listeners;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.managers.PlayerProgressManager;
import com.questlogs.managers.QuestManager;
import com.questlogs.models.PlayerQuestProgress;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import com.questlogs.rewards.RewardManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class MobKillListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final QuestManager questManager;
    private final PlayerProgressManager progressManager;
    private final RewardManager rewardManager;
    
    // Track completed quests to prevent duplicate rewards
    private final Map<String, Map<String, Boolean>> completedQuests = new HashMap<>();
    
    public MobKillListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.questManager = plugin.getQuestManager();
        this.progressManager = plugin.getProgressManager();
        this.rewardManager = plugin.getRewardManager();
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Check if the killer is a player
        if (event.getEntity().getKiller() == null) {
            return;
        }
        
        Player player = event.getEntity().getKiller();
        Entity killedEntity = event.getEntity();
        EntityType entityType = killedEntity.getType();
        String mobType = entityType.name();
        
        // Track cumulative stats for ALL mobs killed (ALWAYS, regardless of quests)
        plugin.getStatsManager().getPlayerStats(player.getUniqueId()).addMobKilled(mobType, 1);
        
        // Track for challenges
        plugin.getChallengeManager().addProgress(player.getName(), "KILL", mobType, 1);
        
        // Process all active mob kill quests
        for (Quest quest : questManager.getAllQuests()) {
            if (quest.getType() != QuestType.KILL_MOBS || !quest.isActive() || !quest.hasMobTargets()) {
                continue;
            }
            
            String questId = quest.getId();
            String playerUUID = player.getUniqueId().toString();
            
            // Skip if quest is locked (prerequisites not met)
            if (!questManager.isQuestAvailable(player.getUniqueId(), questId)) {
                continue;
            }
            
            // Check if quest is already completed (check both memory and saved data)
            if (completedQuests.containsKey(playerUUID) && 
                completedQuests.get(playerUUID).getOrDefault(questId, false)) {
                continue;
            }
            
            // Check if quest is already complete in saved data (prevents overfilling after restart)
            if (progressManager.isQuestComplete(player.getUniqueId(), questId)) {
                if (!completedQuests.containsKey(playerUUID)) {
                    completedQuests.put(playerUUID, new HashMap<>());
                }
                completedQuests.get(playerUUID).put(questId, true);
                continue;
            }
            
            // Check if this mob type is part of the quest
            if (!quest.getMobTargets().containsKey(mobType)) {
                continue;
            }
            
            // Increment progress
            progressManager.addMobProgress(player.getUniqueId(), questId, mobType, 1);
            
            // Get updated progress
            PlayerQuestProgress progress = progressManager.getPlayerProgress(player.getUniqueId());
            int newProgress = progress.getMobProgress(questId, mobType);
            int targetAmount = quest.getMobTarget(mobType);
            
            // Show progress if enabled
            if (plugin.getConfig().getBoolean("show-mob-progress", true)) {
                String mobName = formatMobName(mobType);
                player.sendMessage(ChatColor.YELLOW + "Quest Progress: " + 
                    ChatColor.AQUA + quest.getName() + 
                    ChatColor.GRAY + " - " + mobName + ": " +
                    ChatColor.GREEN + newProgress + ChatColor.GRAY + "/" + 
                    ChatColor.GREEN + targetAmount);
            }
            
            // Check if all mob targets in this quest are complete
            if (isQuestComplete(quest, progress)) {
                markQuestComplete(player, quest, questId, playerUUID);
            }
        }
    }
    
    private boolean isQuestComplete(Quest quest, PlayerQuestProgress progress) {
        for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
            String mobType = entry.getKey();
            int targetAmount = entry.getValue();
            int currentProgress = progress.getMobProgress(quest.getId(), mobType);
            
            if (currentProgress < targetAmount) {
                return false;
            }
        }
        return true;
    }
    
    private void markQuestComplete(Player player, Quest quest, String questId, String playerUUID) {
        // Mark quest as completed
        if (!completedQuests.containsKey(playerUUID)) {
            completedQuests.put(playerUUID, new HashMap<>());
        }
        completedQuests.get(playerUUID).put(questId, true);
        
        // Track quest completion in stats
        plugin.getStatsManager().getPlayerStats(player.getUniqueId()).incrementQuestsCompleted();
        
        // Give rewards and get formatted reward string
        String rewardText = rewardManager.giveRewards(player, quest);
        
        // Send completion message with rewards
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.GREEN + "✓ " + ChatColor.BOLD + "Quest Complete!");
        player.sendMessage(ChatColor.YELLOW + quest.getName());
        player.sendMessage(ChatColor.GRAY + quest.getDescription());
        player.sendMessage(ChatColor.YELLOW + "Rewards: " + ChatColor.GREEN + rewardText);
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("");
        
        // Broadcast completion
        rewardManager.broadcastCompletion(player, quest);
    }
    
    private String formatMobName(String mobType) {
        // Convert ZOMBIE_VILLAGER to Zombie Villager
        String[] words = mobType.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (formatted.length() > 0) {
                formatted.append(" ");
            }
            formatted.append(word.substring(0, 1).toUpperCase())
                     .append(word.substring(1));
        }
        return formatted.toString();
    }
}

