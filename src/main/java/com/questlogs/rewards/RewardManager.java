package com.questlogs.rewards;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardManager {
    
    private final QuestLogsPlugin plugin;
    
    public RewardManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Give rewards to a player and return a formatted string of what was given
     * @param player The player to give rewards to
     * @param quest The quest that was completed
     * @return Formatted string of rewards given (e.g., "5x Diamond, 1x Iron Sword")
     */
    public String giveRewards(Player player, Quest quest) {
        if (!plugin.getConfig().getBoolean("auto-give-rewards", true)) {
            return quest.getReward(); // Return reward string if auto-give is disabled
        }
        
        List<ItemStack> rewards = new ArrayList<>();
        
        // Check if quest has structured rewards (new format)
        if (quest.hasStructuredRewards()) {
            // Use structured rewards
            for (Map.Entry<String, Integer> entry : quest.getStructuredRewards().entrySet()) {
                String materialName = entry.getKey();
                int amount = entry.getValue();
                
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    if (material != Material.AIR) {
                        rewards.add(new ItemStack(material, amount));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in structured rewards: " + materialName);
                }
            }
        } else {
            // Fall back to string-based rewards (old format)
            String rewardString = quest.getReward();
            if (rewardString == null || rewardString.trim().isEmpty()) {
                return "No rewards";
            }
            
            // Parse the reward string into ItemStacks
            rewards = RewardParser.parseRewards(rewardString);
        }
        
        if (rewards.isEmpty()) {
            return quest.getReward(); // Return reward string if no items to give
        }
        
        // Apply enchantments to items if present
        if (quest.hasEnchantments()) {
            plugin.getLogger().info("Quest has enchantments, applying to " + rewards.size() + " reward items");
            applyEnchantments(rewards, quest);
        } else {
            plugin.getLogger().info("Quest has no enchantments configured");
        }
        
        // Give XP to player if specified
        if (quest.getXpReward() > 0) {
            player.giveExp(quest.getXpReward());
            player.sendMessage("§a+§e" + quest.getXpReward() + " XP");
        }
        
        // Give items to player
        Map<Integer, ItemStack> leftover = new HashMap<>();
        
        for (ItemStack item : rewards) {
            // Try to add to inventory
            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            
            if (!overflow.isEmpty()) {
                leftover.putAll(overflow);
            }
        }
        
        // If inventory was full, drop items on ground
        if (!leftover.isEmpty()) {
            for (ItemStack item : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage("§e⚠ Some items were dropped because your inventory is full!");
        }
        
        // Play completion sound
        playCompletionSound(player);
        
        // Return formatted reward string
        return formatRewards(rewards);
    }
    
    /**
     * Format a list of ItemStacks into a human-readable string
     * @param rewards List of ItemStacks
     * @return Formatted string (e.g., "5x Diamond, 1x Iron Sword, 32x Bread")
     */
    private String formatRewards(List<ItemStack> rewards) {
        if (rewards.isEmpty()) {
            return "No rewards";
        }
        
        List<String> formattedItems = new ArrayList<>();
        
        for (ItemStack item : rewards) {
            String itemName = formatMaterialName(item.getType().name());
            int amount = item.getAmount();
            
            if (amount > 1) {
                formattedItems.add(amount + "x " + itemName);
            } else {
                formattedItems.add(itemName);
            }
        }
        
        return String.join(", ", formattedItems);
    }
    
    /**
     * Format a material name into a human-readable string
     * @param material Material name (e.g., "DIAMOND_SWORD")
     * @return Formatted name (e.g., "Diamond Sword")
     */
    private String formatMaterialName(String material) {
        String[] parts = material.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        
        return result.toString();
    }
    
    private void playCompletionSound(Player player) {
        String soundName = plugin.getConfig().getString("completion-sound", "ENTITY_PLAYER_LEVELUP");
        
        if (soundName == null || soundName.trim().isEmpty()) {
            return;
        }
        
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            // Invalid sound name, skip
            plugin.getLogger().warning("Invalid completion sound: " + soundName);
        }
    }
    
    /**
     * Apply enchantments to reward items
     */
    private void applyEnchantments(List<ItemStack> rewards, Quest quest) {
        for (ItemStack item : rewards) {
            String materialName = item.getType().name();
            Map<String, Integer> enchants = quest.getEnchantmentsForItem(materialName);
            
            if (!enchants.isEmpty()) {
                plugin.getLogger().info("Applying enchantments to " + materialName + ": " + enchants.size() + " enchantments");
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                        try {
                            Enchantment enchantment = Enchantment.getByName(entry.getKey().toUpperCase());
                            if (enchantment != null) {
                                int level = entry.getValue();
                                plugin.getLogger().info("  Attempting to add " + entry.getKey() + " level " + level + " to " + materialName);
                                
                                // Allow unsafe enchantments (levels beyond normal limits)
                                boolean success = meta.addEnchant(enchantment, level, true);
                                
                                if (success) {
                                    plugin.getLogger().info("  ✓ Successfully added " + entry.getKey() + " " + level);
                                } else {
                                    plugin.getLogger().warning("  ✗ Failed to add " + entry.getKey() + " " + level + " (returned false)");
                                }
                            } else {
                                plugin.getLogger().warning("  ✗ Invalid enchantment: " + entry.getKey());
                            }
                        } catch (Exception e) {
                            plugin.getLogger().severe("  ✗ Error applying enchantment " + entry.getKey() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    item.setItemMeta(meta);
                    plugin.getLogger().info("Item enchantments after setting meta: " + item.getEnchantments());
                } else {
                    plugin.getLogger().warning("ItemMeta is null for " + materialName);
                }
            }
        }
    }
    
    public void broadcastCompletion(Player player, Quest quest) {
        if (plugin.getConfig().getBoolean("broadcast-completions", false)) {
            String message = "§6§l[Quest] §e" + player.getName() + " §7completed §a" + quest.getName() + "§7!";
            plugin.getServer().broadcastMessage(message);
        }
    }
}

