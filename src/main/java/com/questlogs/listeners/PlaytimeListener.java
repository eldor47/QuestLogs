package com.questlogs.listeners;

import com.questlogs.QuestLogsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks player playtime on the server
 */
public class PlaytimeListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final Map<UUID, Long> sessionStartTimes;
    
    public PlaytimeListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.sessionStartTimes = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Record when this session started
        sessionStartTimes.put(playerId, System.currentTimeMillis());
        
        // Load player stats (creates entry if doesn't exist)
        plugin.getStatsManager().getPlayerStats(playerId);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Calculate session duration
        Long sessionStart = sessionStartTimes.remove(playerId);
        if (sessionStart != null) {
            long sessionDuration = System.currentTimeMillis() - sessionStart;
            
            // Add to player's total playtime
            plugin.getStatsManager().getPlayerStats(playerId).addPlaytime(sessionDuration);
            
            // Save stats to database
            plugin.getStatsManager().saveStats();
        }
    }
    
    /**
     * Get current session playtime for a player (in milliseconds)
     */
    public long getCurrentSessionTime(UUID playerId) {
        Long sessionStart = sessionStartTimes.get(playerId);
        if (sessionStart == null) {
            return 0;
        }
        return System.currentTimeMillis() - sessionStart;
    }
    
    /**
     * Get total playtime including current session (in milliseconds)
     */
    public long getTotalPlaytime(UUID playerId) {
        long storedPlaytime = plugin.getStatsManager().getPlayerStats(playerId).getPlaytime();
        return storedPlaytime + getCurrentSessionTime(playerId);
    }
}

