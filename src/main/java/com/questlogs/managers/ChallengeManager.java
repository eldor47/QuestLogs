package com.questlogs.managers;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Challenge;
import com.questlogs.models.ChallengeType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Manages timed challenges - server-wide mini-games
 */
public class ChallengeManager {
    
    private final QuestLogsPlugin plugin;
    private final Logger logger;
    private final File challengesFile;
    private FileConfiguration challengesConfig;
    
    private Challenge activeChallenge;
    private List<String> challengePool;
    private BukkitTask schedulerTask;
    private BukkitTask reminderTask;
    
    // ASCII mode settings for compatibility
    private boolean useAsciiCharacters;
    
    public ChallengeManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.challengesFile = new File(plugin.getDataFolder(), "challenges.yml");
        this.challengePool = new ArrayList<>();
        
        loadChallenges();
        
        // Start challenge scheduler if enabled
        if (challengesConfig.getBoolean("settings.enabled", true)) {
            startScheduler();
        }
    }
    
    /**
     * Load challenges configuration
     */
    public void loadChallenges() {
        if (!challengesFile.exists()) {
            // Save default config from resources
            plugin.saveResource("challenges.yml", false);
        }
        
        challengesConfig = YamlConfiguration.loadConfiguration(challengesFile);
        challengePool.clear();
        
        ConfigurationSection poolSection = challengesConfig.getConfigurationSection("challenge-pool");
        if (poolSection != null) {
            challengePool.addAll(poolSection.getKeys(false));
        }
        
        // Load ASCII mode setting
        useAsciiCharacters = challengesConfig.getBoolean("settings.use-ascii-characters", false);
        
        logger.info("Loaded " + challengePool.size() + " challenges" + 
                   (useAsciiCharacters ? " (ASCII mode enabled)" : ""));
    }
    
    /**
     * Start challenge scheduler
     */
    private void startScheduler() {
        int frequency = challengesConfig.getInt("settings.frequency", 1800) * 20; // Convert to ticks
        
        schedulerTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeChallenge == null) {
                startRandomChallenge();
            }
        }, frequency, frequency);
        
        logger.info("Challenge scheduler started (frequency: " + (frequency / 20) + " seconds)");
    }
    
    /**
     * Start a random challenge from the pool
     */
    public void startRandomChallenge() {
        if (challengePool.isEmpty()) {
            logger.warning("No challenges available in pool!");
            return;
        }
        
        // Check minimum players
        int minPlayers = challengesConfig.getInt("settings.minimum-players", 2);
        if (Bukkit.getOnlinePlayers().size() < minPlayers) {
            return; // Not enough players online
        }
        
        String challengeId = challengePool.get(new Random().nextInt(challengePool.size()));
        startChallenge(challengeId);
    }
    
    /**
     * Start a specific challenge
     */
    public boolean startChallenge(String challengeId) {
        if (activeChallenge != null) {
            return false; // Challenge already active
        }
        
        String path = "challenge-pool." + challengeId;
        if (!challengesConfig.contains(path)) {
            return false;
        }
        
        // Load challenge data
        String name = challengesConfig.getString(path + ".name");
        String description = challengesConfig.getString(path + ".description");
        String typeString = challengesConfig.getString(path + ".type");
        String target = challengesConfig.getString(path + ".target");
        int duration = challengesConfig.getInt(path + ".duration", 300);
        
        ChallengeType type = ChallengeType.fromString(typeString);
        if (type == null) {
            logger.warning("Invalid challenge type: " + typeString);
            return false;
        }
        
        // Load rewards
        Map<Integer, Map<String, Integer>> rewards = new HashMap<>();
        ConfigurationSection rewardsSection = challengesConfig.getConfigurationSection(path + ".rewards");
        if (rewardsSection != null) {
            for (String place : rewardsSection.getKeys(false)) {
                int placeNum = getPlaceNumber(place);
                if (placeNum > 0) {
                    Map<String, Integer> placeRewards = new HashMap<>();
                    ConfigurationSection placeSection = rewardsSection.getConfigurationSection(place);
                    if (placeSection != null) {
                        for (String item : placeSection.getKeys(false)) {
                            placeRewards.put(item, placeSection.getInt(item));
                        }
                    }
                    rewards.put(placeNum, placeRewards);
                }
            }
        }
        
        // Create and start challenge
        activeChallenge = new Challenge(challengeId, name, description, type, target, duration, rewards);
        
        // Broadcast start
        if (challengesConfig.getBoolean("settings.broadcast-start", true)) {
            broadcastChallengeStart();
        }
        
        // Schedule end
        Bukkit.getScheduler().runTaskLater(plugin, this::endChallenge, duration * 20L);
        
        // Start reminder task
        startReminderTask();
        
        return true;
    }
    
    private int getPlaceNumber(String place) {
        if (place.equals("1st")) return 1;
        if (place.equals("2nd")) return 2;
        if (place.equals("3rd")) return 3;
        try {
            return Integer.parseInt(place);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Start reminder task
     */
    private void startReminderTask() {
        int interval = challengesConfig.getInt("settings.announce-interval", 60) * 20;
        
        reminderTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeChallenge != null && activeChallenge.isActive()) {
                broadcastChallengeReminder();
            }
        }, interval, interval);
    }
    
    /**
     * End the active challenge
     */
    public void endChallenge() {
        if (activeChallenge == null) {
            return;
        }
        
        // Cancel reminder task
        if (reminderTask != null) {
            reminderTask.cancel();
            reminderTask = null;
        }
        
        // Get winners
        List<Map.Entry<String, Integer>> leaderboard = getLeaderboard();
        
        // Broadcast results
        if (challengesConfig.getBoolean("settings.broadcast-end", true)) {
            broadcastChallengeEnd(leaderboard);
        }
        
        // Give rewards
        giveRewards(leaderboard);
        
        activeChallenge = null;
    }
    
    /**
     * Get current leaderboard
     */
    public List<Map.Entry<String, Integer>> getLeaderboard() {
        if (activeChallenge == null) {
            return new ArrayList<>();
        }
        
        return activeChallenge.getProgress().entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Add progress for a player
     */
    public void addProgress(String playerName, String actionType, String target, int amount) {
        if (activeChallenge == null || !activeChallenge.isActive()) {
            return;
        }
        
        // Check if this action matches the challenge
        if (!matchesChallenge(actionType, target)) {
            return;
        }
        
        activeChallenge.addProgress(playerName, amount);
    }
    
    private boolean matchesChallenge(String actionType, String target) {
        ChallengeType challengeType = activeChallenge.getType();
        String challengeTarget = activeChallenge.getTarget();
        
        // Check type matches
        boolean typeMatches = false;
        switch (challengeType) {
            case MINE_BLOCKS:
                typeMatches = actionType.equals("MINE");
                break;
            case BREAK_BLOCKS:
                typeMatches = actionType.equals("BREAK");
                break;
            case PLACE_BLOCKS:
                typeMatches = actionType.equals("PLACE");
                break;
            case CRAFT_ITEMS:
                typeMatches = actionType.equals("CRAFT");
                break;
            case KILL_MOBS:
                typeMatches = actionType.equals("KILL");
                break;
            case EXPLORE_BLOCKS:
                typeMatches = actionType.equals("EXPLORE");
                return typeMatches; // Exploration doesn't need target match
        }
        
        if (!typeMatches) {
            return false;
        }
        
        // Check target matches
        if (challengeTarget.equals("ANY")) {
            return true;
        }
        
        // For CRAFT_ITEMS, support multiple items (comma-separated)
        if (challengeType == ChallengeType.CRAFT_ITEMS && challengeTarget.contains(",")) {
            String[] allowedItems = challengeTarget.split(",");
            for (String item : allowedItems) {
                if (item.trim().equalsIgnoreCase(target)) {
                    return true;
                }
            }
            return false;
        }
        
        // Single target match
        return challengeTarget.equalsIgnoreCase(target);
    }
    
    /**
     * Broadcast challenge start
     */
    private void broadcastChallengeStart() {
        String border = getBorderLine();
        String trophy = getTrophyPrefix();
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + border);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + trophy + " CHALLENGE STARTED! " + trophy);
        Bukkit.broadcastMessage(ChatColor.GOLD + border);
        Bukkit.broadcastMessage(ChatColor.AQUA + activeChallenge.getName());
        Bukkit.broadcastMessage(ChatColor.GRAY + activeChallenge.getDescription());
        
        // Show specific items for crafting challenges
        if (activeChallenge.getType() == ChallengeType.CRAFT_ITEMS && 
            !activeChallenge.getTarget().equals("ANY")) {
            String target = activeChallenge.getTarget();
            if (target.contains(",")) {
                // Multiple items
                String[] items = target.split(",");
                StringBuilder itemList = new StringBuilder();
                for (int i = 0; i < items.length; i++) {
                    if (i > 0) itemList.append(", ");
                    itemList.append(formatItemName(items[i].trim()));
                }
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Tracked Items: " + ChatColor.WHITE + itemList.toString());
            } else {
                // Single item
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Tracked Item: " + ChatColor.WHITE + formatItemName(target));
            }
        } else if (activeChallenge.getType() != ChallengeType.EXPLORE_BLOCKS && 
                   !activeChallenge.getTarget().equals("ANY")) {
            // Show target for other challenge types
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Target: " + ChatColor.WHITE + formatItemName(activeChallenge.getTarget()));
        }
        
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + activeChallenge.getDuration() + " seconds");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Type /challenge to view leaderboard");
        Bukkit.broadcastMessage(ChatColor.GOLD + border);
        Bukkit.broadcastMessage("");
        
        // Play sound
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
    }
    
    /**
     * Broadcast challenge reminder
     */
    private void broadcastChallengeReminder() {
        long remaining = activeChallenge.getRemainingSeconds();
        String clock = getClockPrefix();
        Bukkit.broadcastMessage(ChatColor.YELLOW + clock + " Challenge: " + ChatColor.AQUA + activeChallenge.getName() + 
                               ChatColor.GRAY + " - " + ChatColor.WHITE + remaining + "s remaining");
    }
    
    /**
     * Broadcast challenge end
     */
    private void broadcastChallengeEnd(List<Map.Entry<String, Integer>> leaderboard) {
        String border = getBorderLine();
        String trophy = getTrophyPrefix();
        
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + border);
        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + trophy + " CHALLENGE COMPLETE! " + trophy);
        Bukkit.broadcastMessage(ChatColor.GOLD + border);
        Bukkit.broadcastMessage(ChatColor.AQUA + activeChallenge.getName());
        Bukkit.broadcastMessage("");
        
        if (leaderboard.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.GRAY + "No participants!");
        } else {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Top Players:");
            for (int i = 0; i < Math.min(5, leaderboard.size()); i++) {
                Map.Entry<String, Integer> entry = leaderboard.get(i);
                String medal = getMedal(i + 1);
                Bukkit.broadcastMessage(ChatColor.GRAY + "  " + medal + " " + 
                                      ChatColor.WHITE + entry.getKey() + 
                                      ChatColor.GRAY + " - " + ChatColor.YELLOW + entry.getValue());
            }
        }
        
        Bukkit.broadcastMessage(ChatColor.GOLD + border);
        Bukkit.broadcastMessage("");
        
        // Play sound
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }
    
    private String getMedal(int place) {
        if (useAsciiCharacters) {
            switch (place) {
                case 1: return "[1st]";
                case 2: return "[2nd]";
                case 3: return "[3rd]";
                default: return "[" + place + "th]";
            }
        } else {
            switch (place) {
                case 1: return "🥇";
                case 2: return "🥈";
                case 3: return "🥉";
                default: return String.valueOf(place) + ".";
            }
        }
    }
    
    /**
     * Get border line for broadcasts (respects ASCII mode)
     */
    private String getBorderLine() {
        return useAsciiCharacters ? "===================================" : "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }
    
    /**
     * Get trophy/title prefix (respects ASCII mode)
     */
    private String getTrophyPrefix() {
        return useAsciiCharacters ? "***" : "🏆";
    }
    
    /**
     * Get clock/timer prefix (respects ASCII mode)
     */
    private String getClockPrefix() {
        return useAsciiCharacters ? "[!]" : "⏰";
    }
    
    /**
     * Check if ASCII mode is enabled
     */
    public boolean isAsciiMode() {
        return useAsciiCharacters;
    }
    
    /**
     * Give rewards to winners
     */
    private void giveRewards(List<Map.Entry<String, Integer>> leaderboard) {
        int rewardPlaces = challengesConfig.getInt("settings.reward-top-players", 3);
        
        // Track participation for all players with progress
        for (Map.Entry<String, Integer> entry : leaderboard) {
            String playerName = entry.getKey();
            Player player = Bukkit.getPlayer(playerName);
            
            if (player != null && entry.getValue() > 0) {
                // Increment participation count for anyone who contributed
                plugin.getStatsManager().getPlayerStats(player.getUniqueId()).incrementChallengesParticipated();
            }
        }
        
        // Give rewards to top players
        for (int i = 0; i < Math.min(rewardPlaces, leaderboard.size()); i++) {
            int place = i + 1;
            String playerName = leaderboard.get(i).getKey();
            Player player = Bukkit.getPlayer(playerName);
            
            if (player != null && activeChallenge.hasRewardForPlace(place)) {
                Map<String, Integer> rewards = activeChallenge.getRewardForPlace(place);
                giveRewardsToPlayer(player, rewards, place);
            }
        }
    }
    
    private void giveRewardsToPlayer(Player player, Map<String, Integer> rewards, int place) {
        for (Map.Entry<String, Integer> reward : rewards.entrySet()) {
            try {
                Material material = Material.valueOf(reward.getKey());
                ItemStack item = new ItemStack(material, reward.getValue());
                
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid material in challenge rewards: " + reward.getKey());
            }
        }
        
        // Track challenge win in player stats (only for 1st place)
        if (place == 1) {
            plugin.getStatsManager().getPlayerStats(player.getUniqueId()).incrementChallengesWon();
        }
        
        player.sendMessage(ChatColor.GREEN + "You placed " + getMedal(place) + " and received rewards!");
    }
    
    /**
     * Get active challenge
     */
    public Challenge getActiveChallenge() {
        return activeChallenge;
    }
    
    /**
     * Check if challenge is active
     */
    public boolean isChallengeActive() {
        return activeChallenge != null && activeChallenge.isActive();
    }
    
    /**
     * Get challenges configuration
     */
    public FileConfiguration getChallengesConfig() {
        return challengesConfig;
    }
    
    /**
     * Format item/material name for display (IRON_SWORD -> Iron Sword)
     */
    private String formatItemName(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return materialName;
        }
        
        String[] parts = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            if (part.length() > 0) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        return result.toString();
    }
    
    /**
     * Stop all tasks
     */
    public void shutdown() {
        if (schedulerTask != null) {
            schedulerTask.cancel();
        }
        if (reminderTask != null) {
            reminderTask.cancel();
        }
    }
}

