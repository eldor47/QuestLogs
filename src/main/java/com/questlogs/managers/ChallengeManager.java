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
import java.io.IOException;
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
            createDefaultConfig();
        }
        
        challengesConfig = YamlConfiguration.loadConfiguration(challengesFile);
        challengePool.clear();
        
        ConfigurationSection poolSection = challengesConfig.getConfigurationSection("challenge-pool");
        if (poolSection != null) {
            challengePool.addAll(poolSection.getKeys(false));
        }
        
        logger.info("Loaded " + challengePool.size() + " challenges");
    }
    
    /**
     * Create default challenges configuration
     */
    private void createDefaultConfig() {
        try {
            challengesFile.getParentFile().mkdirs();
            challengesFile.createNewFile();
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(challengesFile);
            
            // Settings
            config.set("settings.enabled", true);
            config.set("settings.frequency", 3600); // 1 hour (less frequent)
            config.set("settings.duration", 300); // 5 minutes
            config.set("settings.announce-interval", 60); // 1 minute
            config.set("settings.broadcast-start", true);
            config.set("settings.broadcast-end", true);
            config.set("settings.reward-top-players", 3);
            config.set("settings.minimum-players", 2); // Don't start if less than 2 players online
            
            // Easy Challenges (common resources, modest rewards)
            createChallenge(config, "stone_sprint", "Stone Mining Sprint", 
                "Mine the most stone!", "MINE_BLOCKS", "STONE", 300,
                new int[]{16, 8, 4}, "IRON_INGOT",
                new int[]{32, 16, 8}, "COAL",
                new int[]{0, 0, 0}, null);
            
            createChallenge(config, "wood_chopper", "Lumberjack Challenge", 
                "Break the most logs!", "BREAK_BLOCKS", "OAK_LOG", 300,
                new int[]{24, 12, 6}, "IRON_INGOT",
                new int[]{8, 4, 2}, "GOLDEN_APPLE",
                new int[]{1, 0, 0}, "ENCHANTED_BOOK"); // Efficiency I
            addEnchantment(config, "wood_chopper", "1st", "EFFICIENCY", 1);
            
            createChallenge(config, "zombie_hunter", "Zombie Hunt", 
                "Kill the most zombies!", "KILL_MOBS", "ZOMBIE", 300,
                new int[]{12, 6, 3}, "IRON_INGOT",
                new int[]{4, 2, 1}, "GOLD_INGOT",
                new int[]{1, 1, 0}, "ENCHANTED_BOOK"); // Sharpness I/II
            addEnchantment(config, "zombie_hunter", "1st", "SHARPNESS", 2);
            addEnchantment(config, "zombie_hunter", "2nd", "SHARPNESS", 1);
            
            // Medium Challenges (less common resources, better rewards)
            createChallenge(config, "coal_rush", "Coal Rush", 
                "Mine the most coal ore!", "MINE_BLOCKS", "COAL_ORE", 300,
                new int[]{20, 10, 5}, "IRON_INGOT",
                new int[]{2, 1, 0}, "DIAMOND",
                new int[]{1, 1, 0}, "ENCHANTED_BOOK"); // Efficiency II/III
            addEnchantment(config, "coal_rush", "1st", "EFFICIENCY", 3);
            addEnchantment(config, "coal_rush", "2nd", "EFFICIENCY", 2);
            
            createChallenge(config, "skeleton_sniper", "Skeleton Sniper", 
                "Kill the most skeletons!", "KILL_MOBS", "SKELETON", 300,
                new int[]{16, 8, 4}, "IRON_INGOT",
                new int[]{2, 1, 0}, "DIAMOND",
                new int[]{1, 1, 0}, "ENCHANTED_BOOK"); // Power II/III
            addEnchantment(config, "skeleton_sniper", "1st", "POWER", 3);
            addEnchantment(config, "skeleton_sniper", "2nd", "POWER", 2);
            
            createChallenge(config, "iron_seeker", "Iron Seeker", 
                "Mine the most iron ore!", "MINE_BLOCKS", "IRON_ORE", 300,
                new int[]{24, 12, 6}, "IRON_INGOT",
                new int[]{3, 1, 0}, "DIAMOND",
                new int[]{1, 0, 0}, "ENCHANTED_BOOK"); // Unbreaking III
            addEnchantment(config, "iron_seeker", "1st", "UNBREAKING", 3);
            
            // Hard Challenges (rare resources, premium rewards)
            createChallenge(config, "diamond_dash", "Diamond Dash", 
                "Mine the most diamond ore!", "MINE_BLOCKS", "DIAMOND_ORE", 300,
                new int[]{5, 3, 1}, "DIAMOND",
                new int[]{16, 8, 4}, "IRON_INGOT",
                new int[]{1, 1, 0}, "ENCHANTED_BOOK"); // Fortune III / Silk Touch
            addEnchantment(config, "diamond_dash", "1st", "FORTUNE", 3);
            addEnchantment(config, "diamond_dash", "2nd", "SILK_TOUCH", 1);
            
            createChallenge(config, "nether_explorer", "Nether Explorer", 
                "Explore the most blocks in the Nether!", "EXPLORE_BLOCKS", "ANY", 300,
                new int[]{4, 2, 1}, "DIAMOND",
                new int[]{8, 4, 2}, "GOLD_INGOT",
                new int[]{1, 1, 0}, "ENCHANTED_BOOK"); // Protection III/IV
            addEnchantment(config, "nether_explorer", "1st", "PROTECTION", 4);
            addEnchantment(config, "nether_explorer", "2nd", "PROTECTION", 3);
            
            // Fun/Creative Challenges
            createChallenge(config, "builder_challenge", "Speed Builder", 
                "Place the most blocks!", "PLACE_BLOCKS", "COBBLESTONE", 300,
                new int[]{12, 6, 3}, "IRON_INGOT",
                new int[]{64, 32, 16}, "OAK_PLANKS",
                new int[]{1, 1, 0}, "ENCHANTED_BOOK"); // Efficiency II/III
            addEnchantment(config, "builder_challenge", "1st", "EFFICIENCY", 3);
            addEnchantment(config, "builder_challenge", "2nd", "EFFICIENCY", 2);
            
            createChallenge(config, "crafting_master", "Crafting Master", 
                "Craft the most items!", "CRAFT_ITEMS", "STICK", 300,
                new int[]{16, 8, 4}, "IRON_INGOT",
                new int[]{8, 4, 2}, "GOLD_INGOT",
                new int[]{1, 0, 0}, "ENCHANTED_BOOK"); // Mending
            addEnchantment(config, "crafting_master", "1st", "MENDING", 1);
            
            config.save(challengesFile);
            logger.info("Created default challenges.yml");
            
        } catch (IOException e) {
            logger.severe("Could not create challenges.yml!");
            e.printStackTrace();
        }
    }
    
    /**
     * Create a balanced challenge with scaled rewards
     */
    private void createChallenge(FileConfiguration config, String id, String name, 
                                String desc, String type, String target, int duration,
                                int[] reward1Amounts, String reward1Type,
                                int[] reward2Amounts, String reward2Type,
                                int[] reward3Amounts, String reward3Type) {
        String path = "challenge-pool." + id;
        config.set(path + ".name", name);
        config.set(path + ".description", desc);
        config.set(path + ".type", type);
        config.set(path + ".target", target);
        config.set(path + ".duration", duration);
        
        // 1st place rewards
        if (reward1Amounts[0] > 0 && reward1Type != null) {
            config.set(path + ".rewards.1st." + reward1Type, reward1Amounts[0]);
        }
        if (reward2Amounts[0] > 0 && reward2Type != null) {
            config.set(path + ".rewards.1st." + reward2Type, reward2Amounts[0]);
        }
        if (reward3Amounts[0] > 0 && reward3Type != null) {
            config.set(path + ".rewards.1st." + reward3Type, reward3Amounts[0]);
        }
        
        // 2nd place rewards
        if (reward1Amounts[1] > 0 && reward1Type != null) {
            config.set(path + ".rewards.2nd." + reward1Type, reward1Amounts[1]);
        }
        if (reward2Amounts[1] > 0 && reward2Type != null) {
            config.set(path + ".rewards.2nd." + reward2Type, reward2Amounts[1]);
        }
        if (reward3Amounts[1] > 0 && reward3Type != null) {
            config.set(path + ".rewards.2nd." + reward3Type, reward3Amounts[1]);
        }
        
        // 3rd place rewards
        if (reward1Amounts[2] > 0 && reward1Type != null) {
            config.set(path + ".rewards.3rd." + reward1Type, reward1Amounts[2]);
        }
        if (reward2Amounts[2] > 0 && reward2Type != null) {
            config.set(path + ".rewards.3rd." + reward2Type, reward2Amounts[2]);
        }
        if (reward3Amounts[2] > 0 && reward3Type != null) {
            config.set(path + ".rewards.3rd." + reward3Type, reward3Amounts[2]);
        }
    }
    
    /**
     * Add enchantment to an enchanted book reward
     */
    private void addEnchantment(FileConfiguration config, String challengeId, String place, String enchantment, int level) {
        String path = "challenge-pool." + challengeId + ".rewards." + place + ".ENCHANTED_BOOK_enchantments." + enchantment;
        config.set(path, level);
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
        
        // Check target matches
        return typeMatches && (challengeTarget.equals("ANY") || challengeTarget.equals(target));
    }
    
    /**
     * Broadcast challenge start
     */
    private void broadcastChallengeStart() {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "🏆 CHALLENGE STARTED! 🏆");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage(ChatColor.AQUA + activeChallenge.getName());
        Bukkit.broadcastMessage(ChatColor.GRAY + activeChallenge.getDescription());
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Duration: " + ChatColor.WHITE + activeChallenge.getDuration() + " seconds");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Type /challenge to view leaderboard");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
        Bukkit.broadcastMessage(ChatColor.YELLOW + "⏰ Challenge: " + ChatColor.AQUA + activeChallenge.getName() + 
                               ChatColor.GRAY + " - " + ChatColor.WHITE + remaining + "s remaining");
    }
    
    /**
     * Broadcast challenge end
     */
    private void broadcastChallengeEnd(List<Map.Entry<String, Integer>> leaderboard) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "🏆 CHALLENGE COMPLETE! 🏆");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
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
        
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage("");
        
        // Play sound
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }
    
    private String getMedal(int place) {
        switch (place) {
            case 1: return "🥇";
            case 2: return "🥈";
            case 3: return "🥉";
            default: return String.valueOf(place) + ".";
        }
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

