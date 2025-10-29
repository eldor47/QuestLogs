package com.questlogs.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a timed server-wide challenge/mini-game
 */
public class Challenge {
    
    private final String id;
    private final String name;
    private final String description;
    private final ChallengeType type;
    private final String target; // Block type, mob type, or item type
    private final int duration; // Duration in seconds
    private final Map<Integer, Map<String, Integer>> rewards; // Place -> (Item -> Amount)
    private final long startTime;
    private final long endTime;
    
    // Player progress during this challenge (playerId -> amount)
    private final Map<String, Integer> progress;
    
    public Challenge(String id, String name, String description, ChallengeType type, 
                     String target, int duration, Map<Integer, Map<String, Integer>> rewards) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target;
        this.duration = duration;
        this.rewards = rewards;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (duration * 1000L);
        this.progress = new HashMap<>();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public ChallengeType getType() {
        return type;
    }
    
    public String getTarget() {
        return target;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public Map<Integer, Map<String, Integer>> getRewards() {
        return rewards;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public Map<String, Integer> getProgress() {
        return new HashMap<>(progress);
    }
    
    // Progress tracking
    public void addProgress(String playerName, int amount) {
        progress.put(playerName, progress.getOrDefault(playerName, 0) + amount);
    }
    
    public int getPlayerProgress(String playerName) {
        return progress.getOrDefault(playerName, 0);
    }
    
    // Check if challenge is active
    public boolean isActive() {
        return System.currentTimeMillis() < endTime;
    }
    
    // Get remaining time in seconds
    public long getRemainingSeconds() {
        long remaining = (endTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    // Get elapsed time in seconds
    public long getElapsedSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    
    // Get reward for a specific place
    public Map<String, Integer> getRewardForPlace(int place) {
        return rewards.getOrDefault(place, new HashMap<>());
    }
    
    // Check if there are rewards for a specific place
    public boolean hasRewardForPlace(int place) {
        return rewards.containsKey(place) && !rewards.get(place).isEmpty();
    }
}


