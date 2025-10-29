package com.questlogs.gui;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestBookGUI {
    
    private final QuestLogsPlugin plugin;
    
    public QuestBookGUI(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void openQuestBook(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Quest Book");
        
        // Separate quests into custom slot and auto-slot
        List<Quest> autoSlotQuests = new ArrayList<>();
        
        for (Quest quest : plugin.getQuestManager().getAllQuests()) {
            if (!quest.isActive()) {
                continue;
            }
            
            ItemStack item = createQuestItem(player, quest);
            
            // If quest has custom GUI slot, use it
            if (quest.hasCustomGuiSlot()) {
                int customSlot = quest.getGuiSlot();
                if (customSlot >= 0 && customSlot < 45) {
                    inv.setItem(customSlot, item);
                } else {
                    autoSlotQuests.add(quest); // Invalid slot, auto-assign
                }
            } else {
                autoSlotQuests.add(quest); // No custom slot, auto-assign
            }
        }
        
        // Auto-assign remaining quests to empty slots
        int slot = 0;
        for (Quest quest : autoSlotQuests) {
            // Find next empty slot
            while (slot < 45 && inv.getItem(slot) != null) {
                slot++;
            }
            if (slot >= 45) {
                break; // No more room
            }
            
            ItemStack item = createQuestItem(player, quest);
            inv.setItem(slot, item);
            slot++;
        }
        
        // Add stats button at bottom left
        ItemStack statsItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName(ChatColor.GOLD + "Your Statistics");
        List<String> statsLore = new ArrayList<>();
        statsLore.add(ChatColor.GRAY + "Click to view your stats!");
        statsLore.add(ChatColor.GRAY + "");
        statsLore.add(ChatColor.YELLOW + "Track all your achievements");
        statsMeta.setLore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inv.setItem(45, statsItem);
        
        // Add info item at bottom
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Quest Book Help");
        List<String> infoLore = new ArrayList<>();
        infoLore.add(ChatColor.GRAY + "Click any quest to view details");
        infoLore.add(ChatColor.GRAY + "");
        infoLore.add(ChatColor.GREEN + "✓ = Completed");
        infoLore.add(ChatColor.YELLOW + "○ = In Progress");
        infoLore.add(ChatColor.RED + "🔒 = Locked");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(49, infoItem);
        
        player.openInventory(inv);
    }
    
    public void openStatsView(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Your Statistics");
        
        com.questlogs.models.PlayerStats stats = plugin.getStatsManager().getPlayerStats(player.getUniqueId());
        
        // Quest stats
        ItemStack questsItem = new ItemStack(Material.PAPER);
        ItemMeta questsMeta = questsItem.getItemMeta();
        questsMeta.setDisplayName(ChatColor.GOLD + "Quest Statistics");
        List<String> questsLore = new ArrayList<>();
        questsLore.add(ChatColor.GRAY + "Quests Completed: " + ChatColor.WHITE + stats.getQuestsCompleted());
        questsMeta.setLore(questsLore);
        questsItem.setItemMeta(questsMeta);
        inv.setItem(10, questsItem);
        
        // Exploration stats
        ItemStack exploreItem = new ItemStack(Material.COMPASS);
        ItemMeta exploreMeta = exploreItem.getItemMeta();
        exploreMeta.setDisplayName(ChatColor.GOLD + "Exploration");
        List<String> exploreLore = new ArrayList<>();
        exploreLore.add(ChatColor.GRAY + "Blocks Explored: " + ChatColor.WHITE + String.format("%,d", stats.getTotalBlocksExplored()));
        exploreMeta.setLore(exploreLore);
        exploreItem.setItemMeta(exploreMeta);
        inv.setItem(12, exploreItem);
        
        // Mining stats
        ItemStack mineItem = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta mineMeta = mineItem.getItemMeta();
        mineMeta.setDisplayName(ChatColor.GOLD + "Mining");
        List<String> mineLore = new ArrayList<>();
        mineLore.add(ChatColor.GRAY + "Total Blocks Mined: " + ChatColor.WHITE + String.format("%,d", stats.getTotalBlocksMined()));
        mineLore.add("");
        addTopStats(mineLore, stats.getBlocksMined(), 5);
        mineMeta.setLore(mineLore);
        mineItem.setItemMeta(mineMeta);
        inv.setItem(14, mineItem);
        
        // Breaking stats
        ItemStack breakItem = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta breakMeta = breakItem.getItemMeta();
        breakMeta.setDisplayName(ChatColor.GOLD + "Block Breaking");
        List<String> breakLore = new ArrayList<>();
        breakLore.add(ChatColor.GRAY + "Total Blocks Broken: " + ChatColor.WHITE + String.format("%,d", stats.getTotalBlocksBroken()));
        breakLore.add("");
        addTopStats(breakLore, stats.getBlocksBroken(), 5);
        breakMeta.setLore(breakLore);
        breakItem.setItemMeta(breakMeta);
        inv.setItem(16, breakItem);
        
        // Placing stats
        ItemStack placeItem = new ItemStack(Material.BRICKS);
        ItemMeta placeMeta = placeItem.getItemMeta();
        placeMeta.setDisplayName(ChatColor.GOLD + "Block Placing");
        List<String> placeLore = new ArrayList<>();
        placeLore.add(ChatColor.GRAY + "Total Blocks Placed: " + ChatColor.WHITE + String.format("%,d", stats.getTotalBlocksPlaced()));
        placeLore.add("");
        addTopStats(placeLore, stats.getBlocksPlaced(), 5);
        placeMeta.setLore(placeLore);
        placeItem.setItemMeta(placeMeta);
        inv.setItem(28, placeItem);
        
        // Crafting stats
        ItemStack craftItem = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftMeta = craftItem.getItemMeta();
        craftMeta.setDisplayName(ChatColor.GOLD + "Crafting");
        List<String> craftLore = new ArrayList<>();
        craftLore.add(ChatColor.GRAY + "Total Items Crafted: " + ChatColor.WHITE + String.format("%,d", stats.getTotalItemsCrafted()));
        craftLore.add("");
        addTopStats(craftLore, stats.getItemsCrafted(), 5);
        craftMeta.setLore(craftLore);
        craftItem.setItemMeta(craftMeta);
        inv.setItem(30, craftItem);
        
        // Combat stats
        ItemStack combatItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta combatMeta = combatItem.getItemMeta();
        combatMeta.setDisplayName(ChatColor.GOLD + "Combat");
        List<String> combatLore = new ArrayList<>();
        combatLore.add(ChatColor.GRAY + "Total Mobs Killed: " + ChatColor.WHITE + String.format("%,d", stats.getTotalMobsKilled()));
        combatLore.add("");
        addTopStats(combatLore, stats.getMobsKilled(), 5);
        combatMeta.setLore(combatLore);
        combatItem.setItemMeta(combatMeta);
        inv.setItem(32, combatItem);
        
        // Back button
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.YELLOW + "Back to Quest Book");
        backItem.setItemMeta(backMeta);
        inv.setItem(49, backItem);
        
        player.openInventory(inv);
    }
    
    private void addTopStats(List<String> lore, Map<String, Integer> stats, int limit) {
        if (stats.isEmpty()) {
            lore.add(ChatColor.GRAY + "No data yet");
            return;
        }
        
        stats.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(limit)
            .forEach(entry -> {
                String name = formatMaterialName(entry.getKey());
                lore.add(ChatColor.YELLOW + name + ": " + ChatColor.WHITE + String.format("%,d", entry.getValue()));
            });
    }
    
    private ItemStack createQuestItem(Player player, Quest quest) {
        boolean isAvailable = plugin.getQuestManager().isQuestAvailable(player.getUniqueId(), quest.getId());
        boolean isComplete = checkQuestComplete(player, quest);
        boolean isLocked = !isAvailable && !isComplete;
        
        // Choose icon based on custom icon, quest type, and lock status
        Material icon;
        
        // Check if quest has custom GUI icon
        if (quest.hasCustomGuiIcon()) {
            try {
                icon = Material.valueOf(quest.getGuiIcon());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid GUI icon for quest " + quest.getId() + ": " + quest.getGuiIcon());
                icon = Material.PAPER; // Fallback
            }
        } else if (isLocked) {
            icon = Material.BARRIER; // Locked quests show as barrier
        } else {
            // Use default icon based on quest type
            switch (quest.getType()) {
                case EXPLORE_BLOCKS:
                    icon = Material.COMPASS;
                    break;
                case MINE_BLOCKS:
                    icon = Material.DIAMOND_PICKAXE;
                    break;
                case BREAK_BLOCKS:
                    icon = Material.IRON_PICKAXE;
                    break;
                case PLACE_BLOCKS:
                    icon = Material.BRICKS;
                    break;
                case CRAFT_ITEMS:
                    icon = Material.CRAFTING_TABLE;
                    break;
                case KILL_MOBS:
                    icon = Material.DIAMOND_SWORD;
                    break;
                default:
                    icon = Material.PAPER;
            }
        }
        
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        
        // Set name with completion status
        String statusIcon;
        if (isLocked) {
            statusIcon = ChatColor.RED + "🔒 ";
        } else if (isComplete) {
            statusIcon = ChatColor.GREEN + "✓ ";
        } else {
            statusIcon = ChatColor.YELLOW + "○ ";
        }
        meta.setDisplayName(statusIcon + ChatColor.WHITE + quest.getName());
        
        // Create lore with quest details
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + quest.getDescription());
        lore.add("");
        
        // If quest is locked, show prerequisite info
        if (isLocked && quest.hasPrerequisite()) {
            Quest prerequisiteQuest = plugin.getQuestManager().getQuest(quest.getPrerequisite());
            lore.add(ChatColor.RED + "🔒 LOCKED");
            lore.add(ChatColor.GRAY + "Requires: " + ChatColor.YELLOW + 
                    (prerequisiteQuest != null ? prerequisiteQuest.getName() : quest.getPrerequisite()));
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Complete the required quest");
            lore.add(ChatColor.DARK_GRAY + "to unlock this one!");
        } else {
            // Add progress information
            addProgressInfo(player, quest, lore, isComplete);
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void addProgressInfo(Player player, Quest quest, List<String> lore, boolean isComplete) {
        // Add progress information
        if ((quest.getType() == QuestType.MINE_BLOCKS || quest.getType() == QuestType.BREAK_BLOCKS ||
             quest.getType() == QuestType.PLACE_BLOCKS || quest.getType() == QuestType.CRAFT_ITEMS) && 
             quest.hasBlockTargets()) {
            // Determine label based on quest type
            String label;
            switch (quest.getType()) {
                case MINE_BLOCKS:
                    label = "Mining Quest:";
                    break;
                case BREAK_BLOCKS:
                    label = "Break Blocks Quest:";
                    break;
                case PLACE_BLOCKS:
                    label = "Place Blocks Quest:";
                    break;
                case CRAFT_ITEMS:
                    label = "Crafting Quest:";
                    break;
                default:
                    label = "Block Quest:";
            }
            
            lore.add(ChatColor.YELLOW + label);
            int completed = 0;
            int total = quest.getBlockTargets().size();
            
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                int current = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), blockType);
                
                String blockName = formatMaterialName(blockType);
                String check = current >= required ? ChatColor.GREEN + "✓" : ChatColor.GRAY + "○";
                lore.add(check + ChatColor.GRAY + " " + blockName + ": " + 
                        ChatColor.WHITE + current + ChatColor.GRAY + "/" + required);
                
                if (current >= required) completed++;
            }
            
            lore.add("");
            lore.add(ChatColor.GOLD + "Progress: " + completed + "/" + total + " objectives");
        } else if (quest.getType() == QuestType.KILL_MOBS && quest.hasMobTargets()) {
            lore.add(ChatColor.YELLOW + "Mob Kill Quest:");
            int completed = 0;
            int total = quest.getMobTargets().size();
            
            for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
                String mobType = entry.getKey();
                int required = entry.getValue();
                int current = plugin.getProgressManager().getMobProgress(player.getUniqueId(), quest.getId(), mobType);
                
                String mobName = formatMaterialName(mobType);
                String check = current >= required ? ChatColor.GREEN + "✓" : ChatColor.GRAY + "○";
                lore.add(check + ChatColor.GRAY + " " + mobName + ": " + 
                        ChatColor.WHITE + current + ChatColor.GRAY + "/" + required);
                
                if (current >= required) completed++;
            }
            
            lore.add("");
            lore.add(ChatColor.GOLD + "Progress: " + completed + "/" + total + " objectives");
        } else {
            // Regular quest (exploration, etc.)
            int progress = plugin.getProgressManager().getPlayerProgress(player.getUniqueId(), quest.getId());
            int target = quest.getTargetAmount();
            double percentage = target > 0 ? (double) progress / target * 100 : 0;
            
            lore.add(ChatColor.YELLOW + "Progress:");
            lore.add(createProgressBar(progress, target));
            lore.add(ChatColor.GRAY + "" + progress + "/" + target + " (" + String.format("%.1f", percentage) + "%)");
        }
        
        // Add rewards with detailed breakdown
        lore.add("");
        lore.add(ChatColor.GOLD + "Rewards:");
        addRewardDetails(quest, lore);
        
        // Add status
        lore.add("");
        if (isComplete) {
            lore.add(ChatColor.GREEN + "✓ COMPLETED");
        } else {
            lore.add(ChatColor.YELLOW + "Click for details");
        }
    }
    
    private boolean checkQuestComplete(Player player, Quest quest) {
        if ((quest.getType() == QuestType.MINE_BLOCKS || quest.getType() == QuestType.BREAK_BLOCKS ||
             quest.getType() == QuestType.PLACE_BLOCKS || quest.getType() == QuestType.CRAFT_ITEMS) && 
             quest.hasBlockTargets()) {
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                int current = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), blockType);
                
                if (current < required) {
                    return false;
                }
            }
            return true;
        } else if (quest.getType() == QuestType.KILL_MOBS && quest.hasMobTargets()) {
            for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
                String mobType = entry.getKey();
                int required = entry.getValue();
                int current = plugin.getProgressManager().getMobProgress(player.getUniqueId(), quest.getId(), mobType);
                
                if (current < required) {
                    return false;
                }
            }
            return true;
        } else {
            int progress = plugin.getProgressManager().getPlayerProgress(player.getUniqueId(), quest.getId());
            return progress >= quest.getTargetAmount();
        }
    }
    
    private String createProgressBar(int current, int target) {
        int barLength = 20;
        int filled = Math.min((int) ((double) current / target * barLength), barLength);
        
        StringBuilder bar = new StringBuilder(ChatColor.GRAY + "[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN + "|");
            } else {
                bar.append(ChatColor.DARK_GRAY + "|");
            }
        }
        bar.append(ChatColor.GRAY + "]");
        
        return bar.toString();
    }
    
    private String formatMaterialName(String material) {
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
    
    /**
     * Add detailed reward information to quest lore
     */
    private void addRewardDetails(Quest quest, List<String> lore) {
        // Check if quest has structured rewards (new format)
        if (quest.hasStructuredRewards()) {
            // Show each reward item on its own line
            for (Map.Entry<String, Integer> entry : quest.getStructuredRewards().entrySet()) {
                String materialName = entry.getKey();
                int amount = entry.getValue();
                
                String itemName = formatMaterialName(materialName);
                if (amount > 1) {
                    lore.add(ChatColor.GRAY + "  • " + ChatColor.WHITE + amount + "x " + itemName);
                } else {
                    lore.add(ChatColor.GRAY + "  • " + ChatColor.WHITE + itemName);
                }
            }
        } else {
            // Fall back to reward string (old format)
            String rewardString = quest.getReward();
            if (rewardString != null && !rewardString.trim().isEmpty()) {
                // Try to parse and display nicely, or just show the string
                if (rewardString.contains("+") || rewardString.contains("x")) {
                    // Looks like a structured reward string, try to parse it
                    String[] parts = rewardString.split("\\+");
                    for (String part : parts) {
                        part = part.trim();
                        if (!part.isEmpty()) {
                            lore.add(ChatColor.GRAY + "  • " + ChatColor.WHITE + part);
                        }
                    }
                } else {
                    // Simple reward string, just display it
                    lore.add(ChatColor.WHITE + "  " + rewardString);
                }
            } else {
                lore.add(ChatColor.GRAY + "  No rewards");
            }
        }
    }
}

