package com.questlogs.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks cumulative player statistics across all time (independent of quests)
 */
public class PlayerStats {
    
    private final UUID playerId;
    
    // Total blocks explored
    private int totalBlocksExplored;
    
    // Blocks mined by type (e.g., "STONE" -> 1543)
    private Map<String, Integer> blocksMined;
    
    // Blocks broken by type
    private Map<String, Integer> blocksBroken;
    
    // Blocks placed by type
    private Map<String, Integer> blocksPlaced;
    
    // Items crafted by type
    private Map<String, Integer> itemsCrafted;
    
    // Mobs killed by type (e.g., "ZOMBIE" -> 87)
    private Map<String, Integer> mobsKilled;
    
    // Quests completed count
    private int questsCompleted;
    
    // Challenges won count
    private int challengesWon;
    
    // Challenges participated in count
    private int challengesParticipated;
    
    // Total playtime in milliseconds
    private long playtime;
    
    // First recorded activity timestamp
    private long firstSeen;
    
    // Last recorded activity timestamp
    private long lastSeen;
    
    public PlayerStats(UUID playerId) {
        this.playerId = playerId;
        this.totalBlocksExplored = 0;
        this.blocksMined = new HashMap<>();
        this.blocksBroken = new HashMap<>();
        this.blocksPlaced = new HashMap<>();
        this.itemsCrafted = new HashMap<>();
        this.mobsKilled = new HashMap<>();
        this.questsCompleted = 0;
        this.challengesWon = 0;
        this.challengesParticipated = 0;
        this.playtime = 0;
        this.firstSeen = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
    }
    
    // Getters
    public UUID getPlayerId() {
        return playerId;
    }
    
    public int getTotalBlocksExplored() {
        return totalBlocksExplored;
    }
    
    public Map<String, Integer> getBlocksMined() {
        return new HashMap<>(blocksMined);
    }
    
    public Map<String, Integer> getBlocksBroken() {
        return new HashMap<>(blocksBroken);
    }
    
    public Map<String, Integer> getBlocksPlaced() {
        return new HashMap<>(blocksPlaced);
    }
    
    public Map<String, Integer> getItemsCrafted() {
        return new HashMap<>(itemsCrafted);
    }
    
    public Map<String, Integer> getMobsKilled() {
        return new HashMap<>(mobsKilled);
    }
    
    public int getQuestsCompleted() {
        return questsCompleted;
    }
    
    public int getChallengesWon() {
        return challengesWon;
    }
    
    public int getChallengesParticipated() {
        return challengesParticipated;
    }
    
    public long getPlaytime() {
        return playtime;
    }
    
    public long getFirstSeen() {
        return firstSeen;
    }
    
    public long getLastSeen() {
        return lastSeen;
    }
    
    // Get specific stat value
    public int getBlockMinedCount(String blockType) {
        return blocksMined.getOrDefault(blockType, 0);
    }
    
    public int getBlockBrokenCount(String blockType) {
        return blocksBroken.getOrDefault(blockType, 0);
    }
    
    public int getBlockPlacedCount(String blockType) {
        return blocksPlaced.getOrDefault(blockType, 0);
    }
    
    public int getItemCraftedCount(String itemType) {
        return itemsCrafted.getOrDefault(itemType, 0);
    }
    
    public int getMobKilledCount(String mobType) {
        return mobsKilled.getOrDefault(mobType, 0);
    }
    
    // Setters
    public void setTotalBlocksExplored(int totalBlocksExplored) {
        this.totalBlocksExplored = totalBlocksExplored;
    }
    
    public void setBlocksMined(Map<String, Integer> blocksMined) {
        this.blocksMined = new HashMap<>(blocksMined);
    }
    
    public void setBlocksBroken(Map<String, Integer> blocksBroken) {
        this.blocksBroken = new HashMap<>(blocksBroken);
    }
    
    public void setBlocksPlaced(Map<String, Integer> blocksPlaced) {
        this.blocksPlaced = new HashMap<>(blocksPlaced);
    }
    
    public void setItemsCrafted(Map<String, Integer> itemsCrafted) {
        this.itemsCrafted = new HashMap<>(itemsCrafted);
    }
    
    public void setMobsKilled(Map<String, Integer> mobsKilled) {
        this.mobsKilled = new HashMap<>(mobsKilled);
    }
    
    public void setQuestsCompleted(int questsCompleted) {
        this.questsCompleted = questsCompleted;
    }
    
    public void setChallengesWon(int challengesWon) {
        this.challengesWon = challengesWon;
    }
    
    public void setChallengesParticipated(int challengesParticipated) {
        this.challengesParticipated = challengesParticipated;
    }
    
    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }
    
    public void setFirstSeen(long firstSeen) {
        this.firstSeen = firstSeen;
    }
    
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    // Increment methods
    public void addBlocksExplored(int amount) {
        this.totalBlocksExplored += amount;
        updateLastSeen();
    }
    
    public void addBlockMined(String blockType, int amount) {
        blocksMined.put(blockType, blocksMined.getOrDefault(blockType, 0) + amount);
        updateLastSeen();
    }
    
    public void addBlockBroken(String blockType, int amount) {
        blocksBroken.put(blockType, blocksBroken.getOrDefault(blockType, 0) + amount);
        updateLastSeen();
    }
    
    public void addBlockPlaced(String blockType, int amount) {
        blocksPlaced.put(blockType, blocksPlaced.getOrDefault(blockType, 0) + amount);
        updateLastSeen();
    }
    
    public void addItemCrafted(String itemType, int amount) {
        itemsCrafted.put(itemType, itemsCrafted.getOrDefault(itemType, 0) + amount);
        updateLastSeen();
    }
    
    public void addMobKilled(String mobType, int amount) {
        mobsKilled.put(mobType, mobsKilled.getOrDefault(mobType, 0) + amount);
        updateLastSeen();
    }
    
    public void incrementQuestsCompleted() {
        this.questsCompleted++;
        updateLastSeen();
    }
    
    public void incrementChallengesWon() {
        this.challengesWon++;
        updateLastSeen();
    }
    
    public void incrementChallengesParticipated() {
        this.challengesParticipated++;
        updateLastSeen();
    }
    
    public void addPlaytime(long milliseconds) {
        this.playtime += milliseconds;
        updateLastSeen();
    }
    
    private void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }
    
    // Get total counts across all types
    public int getTotalBlocksMined() {
        return blocksMined.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getTotalBlocksBroken() {
        return blocksBroken.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getTotalBlocksPlaced() {
        return blocksPlaced.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getTotalItemsCrafted() {
        return itemsCrafted.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    public int getTotalMobsKilled() {
        return mobsKilled.values().stream().mapToInt(Integer::intValue).sum();
    }
}

