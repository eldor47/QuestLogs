package com.questlogs.managers;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.database.DatabaseManager;
import com.questlogs.models.PlayerStats;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages player statistics using SQLite database for better performance
 */
public class StatsManager {
    
    private final QuestLogsPlugin plugin;
    private final Logger logger;
    private final DatabaseManager database;
    
    // Cache of player stats (playerId -> PlayerStats)
    private final Map<UUID, PlayerStats> playerStats;
    
    public StatsManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.database = new DatabaseManager(plugin);
        this.playerStats = new HashMap<>();
        
        loadStats();
        
        // Auto-save stats periodically
        int saveInterval = plugin.getConfig().getInt("auto-save-interval", 300) * 20; // Convert seconds to ticks
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, 
            this::saveStats, saveInterval, saveInterval);
    }
    
    /**
     * Get or create player stats (loads from database if not in cache)
     */
    public PlayerStats getPlayerStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, id -> database.loadPlayerStats(id));
    }
    
    /**
     * Get all player stats
     */
    public Collection<PlayerStats> getAllStats() {
        return new ArrayList<>(playerStats.values());
    }
    
    /**
     * Load stats from SQLite database
     */
    public void loadStats() {
        playerStats.clear();
        
        // Load all player IDs from database
        Set<UUID> allPlayerIds = database.getAllPlayerIds();
        
        logger.info("Found " + allPlayerIds.size() + " players in database");
        
        // Note: We don't load all stats into memory immediately
        // Stats are loaded on-demand when getPlayerStats() is called
        // This is much more memory-efficient for large servers
    }
    
    /**
     * Save stats to SQLite database
     */
    public void saveStats() {
        int savedCount = 0;
        
        for (PlayerStats stats : playerStats.values()) {
            database.savePlayerStats(stats);
            savedCount++;
        }
        
        logger.info("Saved stats for " + savedCount + " players to database");
    }
    
    /**
     * Clear stats for a specific player
     */
    public void clearPlayerStats(UUID playerId) {
        playerStats.remove(playerId);
        // Create new empty stats to clear database
        PlayerStats emptyStats = new PlayerStats(playerId);
        database.savePlayerStats(emptyStats);
    }
    
    /**
     * Clear all stats
     */
    public void clearAllStats() {
        playerStats.clear();
        logger.warning("clearAllStats() called - this only clears cache. Database remains intact.");
        // To truly clear all stats, would need to execute DELETE FROM all tables
    }
    
    /**
     * Close database connection (called on plugin disable)
     */
    public void close() {
        saveStats(); // Save any pending changes
        database.close();
    }
    
    /**
     * Get database manager for advanced operations
     */
    public DatabaseManager getDatabase() {
        return database;
    }
}

