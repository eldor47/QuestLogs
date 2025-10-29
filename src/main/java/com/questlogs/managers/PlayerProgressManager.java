package com.questlogs.managers;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.PlayerQuestProgress;
import com.questlogs.models.Quest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerProgressManager {
    
    private final QuestLogsPlugin plugin;
    private final Map<UUID, PlayerQuestProgress> playerProgress;
    private File progressFile;
    
    public PlayerProgressManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.playerProgress = new HashMap<>();
        this.progressFile = new File(plugin.getDataFolder(), "progress.yml");
    }
    
    public void loadProgress() {
        if (!progressFile.exists()) {
            return;
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(progressFile);
        playerProgress.clear();
        
        if (config.contains("players")) {
            for (String uuidString : config.getConfigurationSection("players").getKeys(false)) {
                UUID playerId = UUID.fromString(uuidString);
                String path = "players." + uuidString;
                
                PlayerQuestProgress progress = new PlayerQuestProgress(playerId);
                Map<String, Integer> questProgress = new HashMap<>();
                
                if (config.contains(path + ".quests")) {
                    for (String questId : config.getConfigurationSection(path + ".quests").getKeys(false)) {
                        int value = config.getInt(path + ".quests." + questId);
                        questProgress.put(questId, value);
                    }
                }
                
                progress.setQuestProgress(questProgress);
                
                // Load block-specific progress for mining quests
                Map<String, Map<String, Integer>> blockProgress = new HashMap<>();
                if (config.contains(path + ".blockProgress")) {
                    for (String questId : config.getConfigurationSection(path + ".blockProgress").getKeys(false)) {
                        Map<String, Integer> blocks = new HashMap<>();
                        String blockPath = path + ".blockProgress." + questId;
                        
                        if (config.contains(blockPath)) {
                            for (String blockType : config.getConfigurationSection(blockPath).getKeys(false)) {
                                int count = config.getInt(blockPath + "." + blockType);
                                blocks.put(blockType, count);
                            }
                        }
                        
                        blockProgress.put(questId, blocks);
                    }
                }
                progress.setBlockProgress(blockProgress);
                
                // Load mob kill progress
                Map<String, Map<String, Integer>> mobProgress = new HashMap<>();
                if (config.contains(path + ".mobProgress")) {
                    for (String questId : config.getConfigurationSection(path + ".mobProgress").getKeys(false)) {
                        Map<String, Integer> mobs = new HashMap<>();
                        String mobPath = path + ".mobProgress." + questId;
                        
                        if (config.contains(mobPath)) {
                            for (String mobType : config.getConfigurationSection(mobPath).getKeys(false)) {
                                int count = config.getInt(mobPath + "." + mobType);
                                mobs.put(mobType, count);
                            }
                        }
                        
                        mobProgress.put(questId, mobs);
                    }
                }
                progress.setMobProgress(mobProgress);
                
                playerProgress.put(playerId, progress);
            }
        }
    }
    
    public void saveProgress() {
        FileConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, PlayerQuestProgress> entry : playerProgress.entrySet()) {
            String uuidString = entry.getKey().toString();
            PlayerQuestProgress progress = entry.getValue();
            
            for (Map.Entry<String, Integer> questEntry : progress.getQuestProgress().entrySet()) {
                config.set("players." + uuidString + ".quests." + questEntry.getKey(), questEntry.getValue());
            }
            
            // Save block-specific progress for mining quests
            for (Map.Entry<String, Map<String, Integer>> questBlockEntry : progress.getBlockProgress().entrySet()) {
                String questId = questBlockEntry.getKey();
                for (Map.Entry<String, Integer> blockEntry : questBlockEntry.getValue().entrySet()) {
                    String blockType = blockEntry.getKey();
                    int count = blockEntry.getValue();
                    config.set("players." + uuidString + ".blockProgress." + questId + "." + blockType, count);
                }
            }
            
            // Save mob-specific progress for kill quests
            for (Map.Entry<String, Map<String, Integer>> questMobEntry : progress.getMobProgress().entrySet()) {
                String questId = questMobEntry.getKey();
                for (Map.Entry<String, Integer> mobEntry : questMobEntry.getValue().entrySet()) {
                    String mobType = mobEntry.getKey();
                    int count = mobEntry.getValue();
                    config.set("players." + uuidString + ".mobProgress." + questId + "." + mobType, count);
                }
            }
        }
        
        try {
            config.save(progressFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save progress file!");
            e.printStackTrace();
        }
    }
    
    public PlayerQuestProgress getPlayerProgress(UUID playerId) {
        return playerProgress.computeIfAbsent(playerId, PlayerQuestProgress::new);
    }
    
    public void setPlayerProgress(UUID playerId, String questId, int amount) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        progress.setProgress(questId, amount);
        saveProgress();
    }
    
    public void addPlayerProgress(UUID playerId, String questId, int amount) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        progress.addProgress(questId, amount);
        saveProgress();
    }
    
    public int getPlayerProgress(UUID playerId, String questId) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        return progress.getProgress(questId);
    }
    
    // Block-specific progress methods for mining quests
    public void addBlockProgress(UUID playerId, String questId, String blockType, int amount) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        progress.addBlockProgress(questId, blockType, amount);
        saveProgress();
    }
    
    public void setBlockProgress(UUID playerId, String questId, String blockType, int amount) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        progress.setBlockProgress(questId, blockType, amount);
        saveProgress();
    }
    
    public int getBlockProgress(UUID playerId, String questId, String blockType) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        return progress.getBlockProgress(questId, blockType);
    }
    
    public Map<String, Integer> getQuestBlockProgress(UUID playerId, String questId) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        return progress.getQuestBlockProgress(questId);
    }
    
    // Mob-specific progress methods for kill quests
    public void addMobProgress(UUID playerId, String questId, String mobType, int amount) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        progress.addMobProgress(questId, mobType, amount);
        saveProgress();
    }
    
    public void setMobProgress(UUID playerId, String questId, String mobType, int amount) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        progress.setMobProgress(questId, mobType, amount);
        saveProgress();
    }
    
    public int getMobProgress(UUID playerId, String questId, String mobType) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        return progress.getMobProgress(questId, mobType);
    }
    
    public Map<String, Integer> getQuestMobProgress(UUID playerId, String questId) {
        PlayerQuestProgress progress = getPlayerProgress(playerId);
        return progress.getQuestMobProgress(questId);
    }
    
    /**
     * Check if a quest is complete for a player
     * @param playerId Player's UUID
     * @param questId Quest ID to check
     * @return true if quest is complete, false otherwise
     */
    public boolean isQuestComplete(UUID playerId, String questId) {
        Quest quest = plugin.getQuestManager().getQuest(questId);
        if (quest == null) {
            return false;
        }
        
        // Check based on quest type
        if ((quest.getType() == com.questlogs.models.QuestType.MINE_BLOCKS || 
             quest.getType() == com.questlogs.models.QuestType.BREAK_BLOCKS ||
             quest.getType() == com.questlogs.models.QuestType.PLACE_BLOCKS ||
             quest.getType() == com.questlogs.models.QuestType.CRAFT_ITEMS) && quest.hasBlockTargets()) {
            // Block-based quest (mining, breaking, placing, crafting) - check all targets
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                int current = getBlockProgress(playerId, questId, blockType);
                
                if (current < required) {
                    return false;
                }
            }
            return true;
        } else if (quest.getType() == com.questlogs.models.QuestType.KILL_MOBS && quest.hasMobTargets()) {
            // Mob kill quest - check all mob targets
            for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
                String mobType = entry.getKey();
                int required = entry.getValue();
                int current = getMobProgress(playerId, questId, mobType);
                
                if (current < required) {
                    return false;
                }
            }
            return true;
        } else {
            // Regular quest (exploration, etc.) - check target amount
            int progress = getPlayerProgress(playerId, questId);
            return progress >= quest.getTargetAmount();
        }
    }
}




