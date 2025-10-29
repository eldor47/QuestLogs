package com.questlogs.models;

import java.util.HashMap;
import java.util.Map;

public class Quest {
    
    private String id;
    private String name;
    private String description;
    private QuestType type;
    private int targetAmount;
    private String reward;
    private boolean active;
    
    // For mining quests: maps block material name to required amount
    private Map<String, Integer> blockTargets;
    
    // For mob kill quests: maps entity type to required amount
    private Map<String, Integer> mobTargets;
    
    // For structured rewards: maps item material to quantity
    private Map<String, Integer> structuredRewards;
    
    // Quest prerequisite: quest ID that must be completed before this quest is available
    private String prerequisite;
    
    // GUI customization
    private int guiSlot; // -1 means auto-assign
    private String guiIcon; // null means use default based on quest type
    
    // XP reward
    private int xpReward; // Experience points to give on completion
    
    // Enchantments for rewards: item -> (enchantment -> level)
    private Map<String, Map<String, Integer>> itemEnchantments;
    
    public Quest(String id, String name, String description, QuestType type, int targetAmount, String reward) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.targetAmount = targetAmount;
        this.reward = reward;
        this.active = true;
        this.blockTargets = new HashMap<>();
        this.mobTargets = new HashMap<>();
        this.structuredRewards = new HashMap<>();
        this.guiSlot = -1; // Auto-assign by default
        this.guiIcon = null; // Use type-based default
        this.xpReward = 0; // No XP by default
        this.itemEnchantments = new HashMap<>();
    }
    
    public Quest(String id, String name, String description, QuestType type, String reward, Map<String, Integer> blockTargets) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.reward = reward;
        this.active = true;
        this.blockTargets = blockTargets != null ? blockTargets : new HashMap<>();
        this.mobTargets = new HashMap<>();
        this.structuredRewards = new HashMap<>();
        this.targetAmount = 0;
        this.guiSlot = -1; // Auto-assign by default
        this.guiIcon = null; // Use type-based default
        this.xpReward = 0;
        this.itemEnchantments = new HashMap<>();
    }
    
    // Constructor for mob kill quests
    public Quest(String id, String name, String description, String reward, Map<String, Integer> mobTargets) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = QuestType.KILL_MOBS;
        this.reward = reward;
        this.active = true;
        this.blockTargets = new HashMap<>();
        this.mobTargets = mobTargets != null ? mobTargets : new HashMap<>();
        this.structuredRewards = new HashMap<>();
        this.targetAmount = 0;
        this.guiSlot = -1; // Auto-assign by default
        this.guiIcon = null; // Use type-based default
        this.xpReward = 0;
        this.itemEnchantments = new HashMap<>();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public QuestType getType() {
        return type;
    }
    
    public void setType(QuestType type) {
        this.type = type;
    }
    
    public int getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }
    
    public String getReward() {
        return reward;
    }
    
    public void setReward(String reward) {
        this.reward = reward;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Map<String, Integer> getBlockTargets() {
        return blockTargets;
    }
    
    public void setBlockTargets(Map<String, Integer> blockTargets) {
        this.blockTargets = blockTargets != null ? blockTargets : new HashMap<>();
    }
    
    public void addBlockTarget(String material, int amount) {
        this.blockTargets.put(material, amount);
    }
    
    public int getBlockTarget(String material) {
        return blockTargets.getOrDefault(material, 0);
    }
    
    public boolean hasBlockTargets() {
        return blockTargets != null && !blockTargets.isEmpty();
    }
    
    public Map<String, Integer> getStructuredRewards() {
        return structuredRewards;
    }
    
    public void setStructuredRewards(Map<String, Integer> structuredRewards) {
        this.structuredRewards = structuredRewards != null ? structuredRewards : new HashMap<>();
    }
    
    public boolean hasStructuredRewards() {
        return structuredRewards != null && !structuredRewards.isEmpty();
    }
    
    public Map<String, Integer> getMobTargets() {
        return mobTargets;
    }
    
    public void setMobTargets(Map<String, Integer> mobTargets) {
        this.mobTargets = mobTargets != null ? mobTargets : new HashMap<>();
    }
    
    public void addMobTarget(String entityType, int amount) {
        this.mobTargets.put(entityType, amount);
    }
    
    public int getMobTarget(String entityType) {
        return mobTargets.getOrDefault(entityType, 0);
    }
    
    public boolean hasMobTargets() {
        return mobTargets != null && !mobTargets.isEmpty();
    }
    
    public String getPrerequisite() {
        return prerequisite;
    }
    
    public void setPrerequisite(String prerequisite) {
        this.prerequisite = prerequisite;
    }
    
    public boolean hasPrerequisite() {
        return prerequisite != null && !prerequisite.isEmpty();
    }
    
    public int getGuiSlot() {
        return guiSlot;
    }
    
    public void setGuiSlot(int guiSlot) {
        this.guiSlot = guiSlot;
    }
    
    public String getGuiIcon() {
        return guiIcon;
    }
    
    public void setGuiIcon(String guiIcon) {
        this.guiIcon = guiIcon;
    }
    
    public boolean hasCustomGuiSlot() {
        return guiSlot >= 0 && guiSlot < 45;
    }
    
    public boolean hasCustomGuiIcon() {
        return guiIcon != null && !guiIcon.isEmpty();
    }
    
    public int getXpReward() {
        return xpReward;
    }
    
    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }
    
    public Map<String, Map<String, Integer>> getItemEnchantments() {
        return itemEnchantments;
    }
    
    public void setItemEnchantments(Map<String, Map<String, Integer>> itemEnchantments) {
        this.itemEnchantments = itemEnchantments != null ? itemEnchantments : new HashMap<>();
    }
    
    public Map<String, Integer> getEnchantmentsForItem(String itemMaterial) {
        return itemEnchantments.getOrDefault(itemMaterial, new HashMap<>());
    }
    
    public void setEnchantmentsForItem(String itemMaterial, Map<String, Integer> enchantments) {
        if (enchantments != null && !enchantments.isEmpty()) {
            itemEnchantments.put(itemMaterial, enchantments);
        }
    }
    
    public boolean hasEnchantments() {
        return itemEnchantments != null && !itemEnchantments.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("Quest{name='%s', type=%s, target=%d}", name, type, targetAmount);
    }
}




