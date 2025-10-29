package com.questlogs.managers;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager {
    
    private final QuestLogsPlugin plugin;
    private final Map<String, Quest> quests;
    private File questsFile;
    
    public QuestManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.quests = new HashMap<>();
        this.questsFile = new File(plugin.getDataFolder(), "quests.yml");
    }
    
    public void loadQuests() {
        if (!questsFile.exists()) {
            createDefaultQuests();
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(questsFile);
        quests.clear();
        
        if (config.contains("quests")) {
            for (String key : config.getConfigurationSection("quests").getKeys(false)) {
                String path = "quests." + key;
                
                String id = key;
                String name = config.getString(path + ".name", "Unknown Quest");
                String description = config.getString(path + ".description", "");
                String typeString = config.getString(path + ".type", "EXPLORE_BLOCKS");
                int targetAmount = config.getInt(path + ".targetAmount", 1000);
                String reward = config.getString(path + ".reward", "");
                boolean active = config.getBoolean(path + ".active", true);
                
                QuestType type = QuestType.fromString(typeString);
                if (type == null) {
                    type = QuestType.EXPLORE_BLOCKS;
                }
                
                Quest quest;
                
                // Check if this is a quest with block targets (mining, breaking, placing, crafting)
                if ((type == QuestType.MINE_BLOCKS || type == QuestType.BREAK_BLOCKS || 
                     type == QuestType.PLACE_BLOCKS || type == QuestType.CRAFT_ITEMS) && 
                    (config.contains(path + ".blocks") || config.contains(path + ".items"))) {
                    Map<String, Integer> blockTargets = new HashMap<>();
                    
                    // Check for .blocks section (used for MINE_BLOCKS, BREAK_BLOCKS, PLACE_BLOCKS)
                    if (config.contains(path + ".blocks")) {
                        for (String blockType : config.getConfigurationSection(path + ".blocks").getKeys(false)) {
                            int amount = config.getInt(path + ".blocks." + blockType);
                            blockTargets.put(blockType, amount);
                        }
                    }
                    // Check for .items section (used for CRAFT_ITEMS)
                    else if (config.contains(path + ".items")) {
                        for (String itemType : config.getConfigurationSection(path + ".items").getKeys(false)) {
                            int amount = config.getInt(path + ".items." + itemType);
                            blockTargets.put(itemType, amount);
                        }
                    }
                    
                    quest = new Quest(id, name, description, type, reward, blockTargets);
                } else if (type == QuestType.KILL_MOBS && config.contains(path + ".mobs")) {
                    // Mob kill quest with mob targets
                    Map<String, Integer> mobTargets = new HashMap<>();
                    for (String mobType : config.getConfigurationSection(path + ".mobs").getKeys(false)) {
                        int amount = config.getInt(path + ".mobs." + mobType);
                        mobTargets.put(mobType, amount);
                    }
                    quest = new Quest(id, name, description, reward, mobTargets);
                } else {
                    quest = new Quest(id, name, description, type, targetAmount, reward);
                }
                
                // Load structured rewards if present (new format)
                if (config.contains(path + ".rewards")) {
                    Map<String, Integer> structuredRewards = new HashMap<>();
                    for (String rewardItem : config.getConfigurationSection(path + ".rewards").getKeys(false)) {
                        int amount = config.getInt(path + ".rewards." + rewardItem);
                        structuredRewards.put(rewardItem, amount);
                    }
                    quest.setStructuredRewards(structuredRewards);
                }
                
                // Load prerequisite if present
                if (config.contains(path + ".prerequisite")) {
                    quest.setPrerequisite(config.getString(path + ".prerequisite"));
                }
                
                // Load GUI customization if present
                if (config.contains(path + ".gui_slot")) {
                    quest.setGuiSlot(config.getInt(path + ".gui_slot", -1));
                }
                if (config.contains(path + ".gui_icon")) {
                    quest.setGuiIcon(config.getString(path + ".gui_icon"));
                }
                
                // Load XP reward if present
                if (config.contains(path + ".xp_reward")) {
                    quest.setXpReward(config.getInt(path + ".xp_reward", 0));
                }
                
                // Load enchantments if present
                if (config.contains(path + ".enchantments")) {
                    Map<String, Map<String, Integer>> enchantments = new HashMap<>();
                    org.bukkit.configuration.ConfigurationSection enchantSection = config.getConfigurationSection(path + ".enchantments");
                    if (enchantSection != null) {
                        for (String itemMaterial : enchantSection.getKeys(false)) {
                            Map<String, Integer> itemEnchants = new HashMap<>();
                            org.bukkit.configuration.ConfigurationSection itemSection = enchantSection.getConfigurationSection(itemMaterial);
                            if (itemSection != null) {
                                for (String enchantName : itemSection.getKeys(false)) {
                                    int level = itemSection.getInt(enchantName);
                                    itemEnchants.put(enchantName, level);
                                }
                            }
                            enchantments.put(itemMaterial, itemEnchants);
                        }
                    }
                    quest.setItemEnchantments(enchantments);
                }
                
                quest.setActive(active);
                quests.put(id, quest);
            }
        }
    }
    
    public void saveQuests() {
        FileConfiguration config = new YamlConfiguration();
        
        for (Quest quest : quests.values()) {
            String key = "quests." + quest.getId();
            config.set(key + ".name", quest.getName());
            config.set(key + ".description", quest.getDescription());
            config.set(key + ".type", quest.getType().name());
            config.set(key + ".reward", quest.getReward());
            config.set(key + ".active", quest.isActive());
            
            // Save block targets for mining, breaking, and placing quests
            if ((quest.getType() == QuestType.MINE_BLOCKS || quest.getType() == QuestType.BREAK_BLOCKS || 
                 quest.getType() == QuestType.PLACE_BLOCKS) && quest.hasBlockTargets()) {
                for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                    config.set(key + ".blocks." + entry.getKey(), entry.getValue());
                }
            } else if (quest.getType() == QuestType.CRAFT_ITEMS && quest.hasBlockTargets()) {
                // Save item targets for crafting quests (uses blockTargets internally)
                for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                    config.set(key + ".items." + entry.getKey(), entry.getValue());
                }
            } else if (quest.getType() == QuestType.KILL_MOBS && quest.hasMobTargets()) {
                // Save mob targets for kill quests
                for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
                    config.set(key + ".mobs." + entry.getKey(), entry.getValue());
                }
            } else {
                config.set(key + ".targetAmount", quest.getTargetAmount());
            }
            
            // Save structured rewards if present
            if (quest.hasStructuredRewards()) {
                for (Map.Entry<String, Integer> entry : quest.getStructuredRewards().entrySet()) {
                    config.set(key + ".rewards." + entry.getKey(), entry.getValue());
                }
            }
            
            // Save prerequisite if present
            if (quest.hasPrerequisite()) {
                config.set(key + ".prerequisite", quest.getPrerequisite());
            }
            
            // Save GUI customization if present
            if (quest.hasCustomGuiSlot()) {
                config.set(key + ".gui_slot", quest.getGuiSlot());
            }
            if (quest.hasCustomGuiIcon()) {
                config.set(key + ".gui_icon", quest.getGuiIcon());
            }
            
            // Save XP reward if present
            if (quest.getXpReward() > 0) {
                config.set(key + ".xp_reward", quest.getXpReward());
            }
            
            // Save enchantments if present
            if (quest.hasEnchantments()) {
                for (Map.Entry<String, Map<String, Integer>> entry : quest.getItemEnchantments().entrySet()) {
                    String itemMaterial = entry.getKey();
                    for (Map.Entry<String, Integer> enchant : entry.getValue().entrySet()) {
                        config.set(key + ".enchantments." + itemMaterial + "." + enchant.getKey(), enchant.getValue());
                    }
                }
            }
        }
        
        try {
            config.save(questsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save quests file!");
            e.printStackTrace();
        }
    }
    
    private void createDefaultQuests() {
        FileConfiguration config = new YamlConfiguration();
        
        // ===== QUEST CHAIN 1: Exploration =====
        // Level 1: First Steps (No prerequisite - starting quest)
        config.set("quests.first_steps.name", "First Steps");
        config.set("quests.first_steps.description", "Begin your adventure by exploring 500 blocks");
        config.set("quests.first_steps.type", "EXPLORE_BLOCKS");
        config.set("quests.first_steps.targetAmount", 500);
        config.set("quests.first_steps.reward", "Beginner's Reward");
        config.set("quests.first_steps.active", true);
        config.set("quests.first_steps.rewards.DIAMOND", 3);
        config.set("quests.first_steps.rewards.COOKED_BEEF", 16);
        config.set("quests.first_steps.rewards.TORCH", 32);
        // No prerequisite - this is the starting quest
        
        // Level 2: World Traveler (Requires First Steps)
        config.set("quests.world_traveler.name", "World Traveler");
        config.set("quests.world_traveler.description", "Continue exploring - reach 2,500 blocks");
        config.set("quests.world_traveler.type", "EXPLORE_BLOCKS");
        config.set("quests.world_traveler.targetAmount", 2500);
        config.set("quests.world_traveler.reward", "Explorer's Reward");
        config.set("quests.world_traveler.active", true);
        config.set("quests.world_traveler.prerequisite", "first_steps");
        config.set("quests.world_traveler.rewards.DIAMOND", 10);
        config.set("quests.world_traveler.rewards.EMERALD", 5);
        config.set("quests.world_traveler.rewards.COMPASS", 1);
        config.set("quests.world_traveler.rewards.MAP", 3);
        
        // ===== QUEST CHAIN 2: Mining =====
        // Level 1: Beginner Miner (Requires First Steps)
        config.set("quests.beginner_miner.name", "Beginner Miner");
        config.set("quests.beginner_miner.description", "Start mining basic ores and blocks");
        config.set("quests.beginner_miner.type", "MINE_BLOCKS");
        config.set("quests.beginner_miner.reward", "Mining Starter Kit");
        config.set("quests.beginner_miner.active", true);
        config.set("quests.beginner_miner.prerequisite", "first_steps");
        config.set("quests.beginner_miner.blocks.STONE", 50);
        config.set("quests.beginner_miner.blocks.COAL_ORE", 16);
        config.set("quests.beginner_miner.blocks.IRON_ORE", 8);
        config.set("quests.beginner_miner.rewards.IRON_PICKAXE", 1);
        config.set("quests.beginner_miner.rewards.IRON_SHOVEL", 1);
        config.set("quests.beginner_miner.rewards.TORCH", 64);
        
        // Level 2: Expert Miner (Requires Beginner Miner)
        config.set("quests.expert_miner.name", "Expert Miner");
        config.set("quests.expert_miner.description", "Mine rare and valuable ores");
        config.set("quests.expert_miner.type", "MINE_BLOCKS");
        config.set("quests.expert_miner.reward", "Expert Mining Rewards");
        config.set("quests.expert_miner.active", true);
        config.set("quests.expert_miner.prerequisite", "beginner_miner");
        config.set("quests.expert_miner.blocks.GOLD_ORE", 5);
        config.set("quests.expert_miner.blocks.DIAMOND_ORE", 3);
        config.set("quests.expert_miner.blocks.EMERALD_ORE", 2);
        config.set("quests.expert_miner.rewards.DIAMOND", 15);
        config.set("quests.expert_miner.rewards.DIAMOND_PICKAXE", 1);
        config.set("quests.expert_miner.rewards.ENCHANTING_TABLE", 1);
        
        // ===== QUEST CHAIN 3: Combat =====
        // Level 1: Monster Slayer I (Requires First Steps)
        config.set("quests.monster_slayer_1.name", "Monster Slayer I");
        config.set("quests.monster_slayer_1.description", "Begin your combat training by defeating basic mobs");
        config.set("quests.monster_slayer_1.type", "KILL_MOBS");
        config.set("quests.monster_slayer_1.reward", "Combat Training Rewards");
        config.set("quests.monster_slayer_1.active", true);
        config.set("quests.monster_slayer_1.prerequisite", "first_steps");
        config.set("quests.monster_slayer_1.mobs.ZOMBIE", 8);
        config.set("quests.monster_slayer_1.mobs.SKELETON", 8);
        config.set("quests.monster_slayer_1.mobs.SPIDER", 4);
        config.set("quests.monster_slayer_1.rewards.IRON_SWORD", 1);
        config.set("quests.monster_slayer_1.rewards.COOKED_BEEF", 16);
        config.set("quests.monster_slayer_1.rewards.ARROW", 32);
        
        // Level 2: Undead Hunter (Requires Monster Slayer I)
        config.set("quests.undead_hunter.name", "Undead Hunter");
        config.set("quests.undead_hunter.description", "Specialize in eliminating undead threats");
        config.set("quests.undead_hunter.type", "KILL_MOBS");
        config.set("quests.undead_hunter.reward", "Undead Slayer Rewards");
        config.set("quests.undead_hunter.active", true);
        config.set("quests.undead_hunter.prerequisite", "monster_slayer_1");
        config.set("quests.undead_hunter.mobs.ZOMBIE", 15);
        config.set("quests.undead_hunter.mobs.SKELETON", 12);
        config.set("quests.undead_hunter.mobs.ZOMBIE_VILLAGER", 3);
        config.set("quests.undead_hunter.rewards.DIAMOND_SWORD", 1);
        config.set("quests.undead_hunter.rewards.DIAMOND", 8);
        config.set("quests.undead_hunter.rewards.GOLDEN_APPLE", 2);
        
        // ====================
        // QUEST CHAIN 4: Breaking Blocks
        // ====================
        config.set("quests.block_breaker.name", "Block Breaker");
        config.set("quests.block_breaker.description", "Break various blocks");
        config.set("quests.block_breaker.type", "BREAK_BLOCKS");
        config.set("quests.block_breaker.reward", "Block Breaker Rewards");
        config.set("quests.block_breaker.active", true);
        config.set("quests.block_breaker.prerequisite", "first_steps");
        config.set("quests.block_breaker.blocks.DIRT", 100);
        config.set("quests.block_breaker.blocks.GRASS_BLOCK", 50);
        config.set("quests.block_breaker.blocks.SAND", 25);
        config.set("quests.block_breaker.rewards.IRON_SHOVEL", 1);
        config.set("quests.block_breaker.rewards.COOKED_BEEF", 16);
        
        // ====================
        // QUEST CHAIN 5: Placing Blocks
        // ====================
        config.set("quests.builder.name", "Builder");
        config.set("quests.builder.description", "Place blocks to build structures");
        config.set("quests.builder.type", "PLACE_BLOCKS");
        config.set("quests.builder.reward", "Builder Rewards");
        config.set("quests.builder.active", true);
        config.set("quests.builder.prerequisite", "first_steps");
        config.set("quests.builder.blocks.OAK_PLANKS", 64);
        config.set("quests.builder.blocks.COBBLESTONE", 64);
        config.set("quests.builder.blocks.GLASS", 32);
        config.set("quests.builder.rewards.OAK_LOG", 32);
        config.set("quests.builder.rewards.STONE", 64);
        config.set("quests.builder.rewards.IRON_INGOT", 16);
        
        // ====================
        // QUEST CHAIN 6: Crafting Items
        // ====================
        config.set("quests.crafter.name", "Crafter");
        config.set("quests.crafter.description", "Craft various items");
        config.set("quests.crafter.type", "CRAFT_ITEMS");
        config.set("quests.crafter.reward", "Crafter Rewards");
        config.set("quests.crafter.active", true);
        config.set("quests.crafter.prerequisite", "first_steps");
        config.set("quests.crafter.items.STICK", 16);
        config.set("quests.crafter.items.WOODEN_PICKAXE", 1);
        config.set("quests.crafter.items.CHEST", 4);
        config.set("quests.crafter.rewards.IRON_INGOT", 8);
        config.set("quests.crafter.rewards.DIAMOND", 2);
        
        try {
            config.save(questsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create default quests file!");
            e.printStackTrace();
        }
    }
    
    public Collection<Quest> getAllQuests() {
        return quests.values();
    }
    
    public Quest getQuest(String id) {
        return quests.get(id);
    }
    
    public void addQuest(Quest quest) {
        quests.put(quest.getId(), quest);
        saveQuests();
    }
    
    public void removeQuest(String id) {
        quests.remove(id);
        saveQuests();
    }
    
    public List<Quest> getActiveQuests() {
        List<Quest> active = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.isActive()) {
                active.add(quest);
            }
        }
        return active;
    }
    
    /**
     * Check if a quest is available to a player (prerequisite is completed)
     * @param playerId Player's UUID
     * @param questId Quest ID to check
     * @return true if quest has no prerequisite or prerequisite is completed
     */
    public boolean isQuestAvailable(UUID playerId, String questId) {
        Quest quest = getQuest(questId);
        if (quest == null || !quest.isActive()) {
            return false;
        }
        
        // If prerequisites are disabled, all quests are available
        if (!plugin.getConfig().getBoolean("enable-prerequisites", true)) {
            return true;
        }
        
        // If quest has no prerequisite, it's available
        if (!quest.hasPrerequisite()) {
            return true;
        }
        
        // Check if prerequisite quest is completed
        String prerequisiteId = quest.getPrerequisite();
        return plugin.getProgressManager().isQuestComplete(playerId, prerequisiteId);
    }
    
    /**
     * Get all quests available to a player (active and prerequisites met)
     * @param playerId Player's UUID
     * @return List of available quests
     */
    public List<Quest> getAvailableQuests(UUID playerId) {
        List<Quest> available = new ArrayList<>();
        for (Quest quest : getActiveQuests()) {
            if (isQuestAvailable(playerId, quest.getId())) {
                available.add(quest);
            }
        }
        return available;
    }
}




