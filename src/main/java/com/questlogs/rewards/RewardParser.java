package com.questlogs.rewards;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewardParser {
    
    // Pattern to match items like "DIAMOND x5", "Iron Pickaxe", "Diamond Sword (Sharpness V)"
    private static final Pattern ITEM_PATTERN = Pattern.compile(
        "([A-Za-z_\\s]+?)(?:\\s*x(\\d+))?(?:\\s*\\(([^)]+)\\))?(?:\\s*[+,]|$)"
    );
    
    public static List<ItemStack> parseRewards(String rewardString) {
        List<ItemStack> items = new ArrayList<>();
        
        if (rewardString == null || rewardString.trim().isEmpty()) {
            return items;
        }
        
        Matcher matcher = ITEM_PATTERN.matcher(rewardString);
        
        while (matcher.find()) {
            String itemName = matcher.group(1).trim();
            String amountStr = matcher.group(2);
            String enchantments = matcher.group(3);
            
            int amount = 1;
            if (amountStr != null) {
                try {
                    amount = Integer.parseInt(amountStr);
                } catch (NumberFormatException e) {
                    amount = 1;
                }
            }
            
            // Try to parse the material
            Material material = parseMaterial(itemName);
            
            if (material != null && material != Material.AIR) {
                ItemStack item = new ItemStack(material, amount);
                
                // Apply enchantments if specified
                if (enchantments != null && !enchantments.trim().isEmpty()) {
                    applyEnchantments(item, enchantments);
                }
                
                items.add(item);
            }
        }
        
        return items;
    }
    
    private static Material parseMaterial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // Convert to uppercase and replace spaces with underscores
        String materialName = name.trim().toUpperCase().replace(" ", "_");
        
        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            // Try common variations
            return tryCommonVariations(materialName);
        }
    }
    
    private static Material tryCommonVariations(String name) {
        // Common item name variations
        Map<String, String> variations = new HashMap<>();
        variations.put("DIAMOND_PICK", "DIAMOND_PICKAXE");
        variations.put("IRON_PICK", "IRON_PICKAXE");
        variations.put("STONE_PICK", "STONE_PICKAXE");
        variations.put("WOOD_PICKAXE", "WOODEN_PICKAXE");
        variations.put("WOOD_AXE", "WOODEN_AXE");
        variations.put("WOOD_SHOVEL", "WOODEN_SHOVEL");
        variations.put("WOOD_SWORD", "WOODEN_SWORD");
        variations.put("IRON_CHEST", "IRON_CHESTPLATE");
        variations.put("IRON_LEGS", "IRON_LEGGINGS");
        variations.put("IRON_BOOTS", "IRON_BOOTS");
        variations.put("IRON_HELMET", "IRON_HELMET");
        variations.put("DIAMOND_CHEST", "DIAMOND_CHESTPLATE");
        variations.put("DIAMOND_LEGS", "DIAMOND_LEGGINGS");
        variations.put("NETHERITE_CHEST", "NETHERITE_CHESTPLATE");
        variations.put("NETHERITE_LEGS", "NETHERITE_LEGGINGS");
        variations.put("ENCH_TABLE", "ENCHANTING_TABLE");
        variations.put("ENCHANTING_TABLE", "ENCHANTING_TABLE");
        variations.put("CRAFTING_TABLE", "CRAFTING_TABLE");
        variations.put("COOKED_BEEF", "COOKED_BEEF");
        variations.put("BREAD", "BREAD");
        variations.put("TORCH", "TORCH");
        variations.put("TORCHES", "TORCH");
        
        if (variations.containsKey(name)) {
            try {
                return Material.valueOf(variations.get(name));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        return null;
    }
    
    private static void applyEnchantments(ItemStack item, String enchantString) {
        // Split by comma for multiple enchantments
        String[] enchants = enchantString.split(",");
        
        for (String enchant : enchants) {
            enchant = enchant.trim();
            
            // Try to parse enchantment name and level
            // Format: "Sharpness V" or "Fortune III" or "Efficiency 4"
            String[] parts = enchant.split("\\s+");
            
            if (parts.length >= 1) {
                String enchantName = parts[0].toUpperCase();
                int level = 1;
                
                if (parts.length >= 2) {
                    level = parseRomanOrNumber(parts[1]);
                }
                
                Enchantment enchantment = getEnchantment(enchantName);
                
                if (enchantment != null) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.addEnchant(enchantment, level, true);
                        item.setItemMeta(meta);
                    }
                }
            }
        }
    }
    
    private static Enchantment getEnchantment(String name) {
        // Map common enchantment names
        Map<String, Enchantment> enchantMap = new HashMap<>();
        enchantMap.put("SHARPNESS", Enchantment.SHARPNESS);
        enchantMap.put("FORTUNE", Enchantment.FORTUNE);
        enchantMap.put("EFFICIENCY", Enchantment.EFFICIENCY);
        enchantMap.put("UNBREAKING", Enchantment.UNBREAKING);
        enchantMap.put("PROTECTION", Enchantment.PROTECTION);
        enchantMap.put("MENDING", Enchantment.MENDING);
        enchantMap.put("SILK_TOUCH", Enchantment.SILK_TOUCH);
        enchantMap.put("LOOTING", Enchantment.LOOTING);
        enchantMap.put("POWER", Enchantment.POWER);
        enchantMap.put("FLAME", Enchantment.FLAME);
        enchantMap.put("INFINITY", Enchantment.INFINITY);
        enchantMap.put("KNOCKBACK", Enchantment.KNOCKBACK);
        enchantMap.put("FIRE_ASPECT", Enchantment.FIRE_ASPECT);
        enchantMap.put("THORNS", Enchantment.THORNS);
        enchantMap.put("RESPIRATION", Enchantment.RESPIRATION);
        enchantMap.put("AQUA_AFFINITY", Enchantment.AQUA_AFFINITY);
        enchantMap.put("FEATHER_FALLING", Enchantment.FEATHER_FALLING);
        
        return enchantMap.get(name);
    }
    
    private static int parseRomanOrNumber(String str) {
        // Try to parse as roman numeral first
        switch (str.toUpperCase()) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            case "VI": return 6;
            case "VII": return 7;
            case "VIII": return 8;
            case "IX": return 9;
            case "X": return 10;
        }
        
        // Try to parse as regular number
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}


