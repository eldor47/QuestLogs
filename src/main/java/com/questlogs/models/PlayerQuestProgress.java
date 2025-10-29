package com.questlogs.models;

import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class PlayerQuestProgress {
    
    private UUID playerId;
    private Map<String, Integer> questProgress; // Quest ID -> Progress
    private Map<String, Map<String, Integer>> blockProgress; // Quest ID -> (Block Material -> Count)
    private Map<String, Map<String, Integer>> mobProgress; // Quest ID -> (Mob Type -> Count)
    
    public PlayerQuestProgress(UUID playerId) {
        this.playerId = playerId;
        this.questProgress = new HashMap<>();
        this.blockProgress = new HashMap<>();
        this.mobProgress = new HashMap<>();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }
    
    public Map<String, Integer> getQuestProgress() {
        return questProgress;
    }
    
    public void setQuestProgress(Map<String, Integer> questProgress) {
        this.questProgress = questProgress;
    }
    
    public int getProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }
    
    public void setProgress(String questId, int progress) {
        questProgress.put(questId, progress);
    }
    
    public void addProgress(String questId, int amount) {
        questProgress.put(questId, getProgress(questId) + amount);
    }
    
    public boolean hasQuest(String questId) {
        return questProgress.containsKey(questId);
    }
    
    // Block-specific progress methods for mining quests
    public Map<String, Map<String, Integer>> getBlockProgress() {
        return blockProgress;
    }
    
    public void setBlockProgress(Map<String, Map<String, Integer>> blockProgress) {
        this.blockProgress = blockProgress;
    }
    
    public Map<String, Integer> getQuestBlockProgress(String questId) {
        return blockProgress.computeIfAbsent(questId, k -> new HashMap<>());
    }
    
    public int getBlockProgress(String questId, String blockType) {
        return getQuestBlockProgress(questId).getOrDefault(blockType, 0);
    }
    
    public void addBlockProgress(String questId, String blockType, int amount) {
        Map<String, Integer> questBlocks = getQuestBlockProgress(questId);
        questBlocks.put(blockType, questBlocks.getOrDefault(blockType, 0) + amount);
    }
    
    public void setBlockProgress(String questId, String blockType, int amount) {
        getQuestBlockProgress(questId).put(blockType, amount);
    }
    
    // Mob-specific progress methods for kill quests
    public Map<String, Map<String, Integer>> getMobProgress() {
        return mobProgress;
    }
    
    public void setMobProgress(Map<String, Map<String, Integer>> mobProgress) {
        this.mobProgress = mobProgress;
    }
    
    public Map<String, Integer> getQuestMobProgress(String questId) {
        return mobProgress.computeIfAbsent(questId, k -> new HashMap<>());
    }
    
    public int getMobProgress(String questId, String mobType) {
        return getQuestMobProgress(questId).getOrDefault(mobType, 0);
    }
    
    public void addMobProgress(String questId, String mobType, int amount) {
        Map<String, Integer> questMobs = getQuestMobProgress(questId);
        questMobs.put(mobType, questMobs.getOrDefault(mobType, 0) + amount);
    }
    
    public void setMobProgress(String questId, String mobType, int amount) {
        getQuestMobProgress(questId).put(mobType, amount);
    }
}




