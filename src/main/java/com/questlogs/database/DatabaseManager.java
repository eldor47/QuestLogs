package com.questlogs.database;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.PlayerStats;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Manages SQLite database connection and operations for player statistics
 */
public class DatabaseManager {
    
    private final QuestLogsPlugin plugin;
    private final Logger logger;
    private final File databaseFile;
    private Connection connection;
    
    public DatabaseManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.databaseFile = new File(plugin.getDataFolder(), "stats.db");
        
        initializeDatabase();
    }
    
    /**
     * Initialize database connection and create tables if needed
     */
    private void initializeDatabase() {
        try {
            // Create data folder if it doesn't exist
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Load SQLite driver
            Class.forName("org.sqlite.JDBC");
            
            // Connect to database
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            
            logger.info("Connected to SQLite database: " + databaseFile.getName());
            
            // Create tables
            createTables();
            
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite JDBC driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            logger.severe("Failed to initialize database!");
            e.printStackTrace();
        }
    }
    
    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        String createPlayersTable = 
            "CREATE TABLE IF NOT EXISTS players (" +
            "uuid TEXT PRIMARY KEY," +
            "total_blocks_explored INTEGER DEFAULT 0," +
            "quests_completed INTEGER DEFAULT 0," +
            "challenges_won INTEGER DEFAULT 0," +
            "challenges_participated INTEGER DEFAULT 0," +
            "playtime INTEGER DEFAULT 0," +
            "first_seen INTEGER," +
            "last_seen INTEGER" +
            ")";
        
        String createBlocksMinedTable = 
            "CREATE TABLE IF NOT EXISTS blocks_mined (" +
            "uuid TEXT," +
            "block_type TEXT," +
            "amount INTEGER," +
            "PRIMARY KEY (uuid, block_type)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE" +
            ")";
        
        String createBlocksBrokenTable = 
            "CREATE TABLE IF NOT EXISTS blocks_broken (" +
            "uuid TEXT," +
            "block_type TEXT," +
            "amount INTEGER," +
            "PRIMARY KEY (uuid, block_type)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE" +
            ")";
        
        String createBlocksPlacedTable = 
            "CREATE TABLE IF NOT EXISTS blocks_placed (" +
            "uuid TEXT," +
            "block_type TEXT," +
            "amount INTEGER," +
            "PRIMARY KEY (uuid, block_type)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE" +
            ")";
        
        String createItemsCraftedTable = 
            "CREATE TABLE IF NOT EXISTS items_crafted (" +
            "uuid TEXT," +
            "item_type TEXT," +
            "amount INTEGER," +
            "PRIMARY KEY (uuid, item_type)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE" +
            ")";
        
        String createMobsKilledTable = 
            "CREATE TABLE IF NOT EXISTS mobs_killed (" +
            "uuid TEXT," +
            "mob_type TEXT," +
            "amount INTEGER," +
            "PRIMARY KEY (uuid, mob_type)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE" +
            ")";
        
        // Create indexes for better performance
        String createIndexBlocksMined = "CREATE INDEX IF NOT EXISTS idx_blocks_mined_uuid ON blocks_mined(uuid)";
        String createIndexBlocksBroken = "CREATE INDEX IF NOT EXISTS idx_blocks_broken_uuid ON blocks_broken(uuid)";
        String createIndexBlocksPlaced = "CREATE INDEX IF NOT EXISTS idx_blocks_placed_uuid ON blocks_placed(uuid)";
        String createIndexItemsCrafted = "CREATE INDEX IF NOT EXISTS idx_items_crafted_uuid ON items_crafted(uuid)";
        String createIndexMobsKilled = "CREATE INDEX IF NOT EXISTS idx_mobs_killed_uuid ON mobs_killed(uuid)";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createBlocksMinedTable);
            stmt.execute(createBlocksBrokenTable);
            stmt.execute(createBlocksPlacedTable);
            stmt.execute(createItemsCraftedTable);
            stmt.execute(createMobsKilledTable);
            
            stmt.execute(createIndexBlocksMined);
            stmt.execute(createIndexBlocksBroken);
            stmt.execute(createIndexBlocksPlaced);
            stmt.execute(createIndexItemsCrafted);
            stmt.execute(createIndexMobsKilled);
            
            logger.info("Database tables created/verified successfully");
        }
    }
    
    /**
     * Load player statistics from database
     */
    public PlayerStats loadPlayerStats(UUID playerId) {
        PlayerStats stats = new PlayerStats(playerId);
        String uuidString = playerId.toString();
        
        try {
            // Load basic player stats
            String query = "SELECT * FROM players WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, uuidString);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    stats.setTotalBlocksExplored(rs.getInt("total_blocks_explored"));
                    stats.setQuestsCompleted(rs.getInt("quests_completed"));
                    stats.setChallengesWon(rs.getInt("challenges_won"));
                    stats.setChallengesParticipated(rs.getInt("challenges_participated"));
                    stats.setPlaytime(rs.getLong("playtime"));
                    stats.setFirstSeen(rs.getLong("first_seen"));
                    stats.setLastSeen(rs.getLong("last_seen"));
                } else {
                    // New player - insert default record
                    createPlayerRecord(playerId);
                }
            }
            
            // Load blocks mined
            stats.setBlocksMined(loadStatMap("blocks_mined", uuidString));
            
            // Load blocks broken
            stats.setBlocksBroken(loadStatMap("blocks_broken", uuidString));
            
            // Load blocks placed
            stats.setBlocksPlaced(loadStatMap("blocks_placed", uuidString));
            
            // Load items crafted
            stats.setItemsCrafted(loadStatMap("items_crafted", uuidString));
            
            // Load mobs killed
            stats.setMobsKilled(loadStatMap("mobs_killed", uuidString));
            
        } catch (SQLException e) {
            logger.warning("Failed to load stats for player " + playerId);
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Load a stat map (block type -> amount) from database
     */
    private Map<String, Integer> loadStatMap(String tableName, String uuid) throws SQLException {
        Map<String, Integer> statMap = new HashMap<>();
        String query = "SELECT * FROM " + tableName + " WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String typeColumn = tableName.startsWith("items_") ? "item_type" : 
                                   tableName.startsWith("mobs_") ? "mob_type" : "block_type";
                String type = rs.getString(typeColumn);
                int amount = rs.getInt("amount");
                statMap.put(type, amount);
            }
        }
        
        return statMap;
    }
    
    /**
     * Save player statistics to database
     */
    public void savePlayerStats(PlayerStats stats) {
        String uuidString = stats.getPlayerId().toString();
        
        try {
            // Save basic player stats
            String upsert = "INSERT OR REPLACE INTO players (uuid, total_blocks_explored, quests_completed, challenges_won, challenges_participated, playtime, first_seen, last_seen) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(upsert)) {
                stmt.setString(1, uuidString);
                stmt.setInt(2, stats.getTotalBlocksExplored());
                stmt.setInt(3, stats.getQuestsCompleted());
                stmt.setInt(4, stats.getChallengesWon());
                stmt.setInt(5, stats.getChallengesParticipated());
                stmt.setLong(6, stats.getPlaytime());
                stmt.setLong(7, stats.getFirstSeen());
                stmt.setLong(8, stats.getLastSeen());
                stmt.executeUpdate();
            }
            
            // Save blocks mined
            saveStatMap("blocks_mined", "block_type", uuidString, stats.getBlocksMined());
            
            // Save blocks broken
            saveStatMap("blocks_broken", "block_type", uuidString, stats.getBlocksBroken());
            
            // Save blocks placed
            saveStatMap("blocks_placed", "block_type", uuidString, stats.getBlocksPlaced());
            
            // Save items crafted
            saveStatMap("items_crafted", "item_type", uuidString, stats.getItemsCrafted());
            
            // Save mobs killed
            saveStatMap("mobs_killed", "mob_type", uuidString, stats.getMobsKilled());
            
        } catch (SQLException e) {
            logger.warning("Failed to save stats for player " + stats.getPlayerId());
            e.printStackTrace();
        }
    }
    
    /**
     * Save a stat map to database
     */
    private void saveStatMap(String tableName, String typeColumn, String uuid, Map<String, Integer> statMap) throws SQLException {
        // Delete old entries
        String delete = "DELETE FROM " + tableName + " WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(delete)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        }
        
        // Insert new entries
        if (!statMap.isEmpty()) {
            String insert = "INSERT INTO " + tableName + " (uuid, " + typeColumn + ", amount) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insert)) {
                for (Map.Entry<String, Integer> entry : statMap.entrySet()) {
                    stmt.setString(1, uuid);
                    stmt.setString(2, entry.getKey());
                    stmt.setInt(3, entry.getValue());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        }
    }
    
    /**
     * Create a new player record
     */
    private void createPlayerRecord(UUID playerId) throws SQLException {
        String insert = "INSERT INTO players (uuid, total_blocks_explored, quests_completed, challenges_won, challenges_participated, playtime, first_seen, last_seen) " +
                       "VALUES (?, 0, 0, 0, 0, 0, ?, ?)";
        long now = System.currentTimeMillis();
        
        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setString(1, playerId.toString());
            stmt.setLong(2, now);
            stmt.setLong(3, now);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Get all player UUIDs from database
     */
    public Set<UUID> getAllPlayerIds() {
        Set<UUID> playerIds = new HashSet<>();
        
        try {
            String query = "SELECT uuid FROM players";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        playerIds.add(uuid);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to load player IDs from database");
            e.printStackTrace();
        }
        
        return playerIds;
    }
    
    /**
     * Get top players by challenges won
     */
    public List<Map.Entry<UUID, Integer>> getTopChallengeWinners(int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, challenges_won FROM players " +
                          "WHERE challenges_won > 0 " +
                          "ORDER BY challenges_won DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int wins = rs.getInt("challenges_won");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, wins));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top challenge winners");
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Get top players by challenges participated
     */
    public List<Map.Entry<UUID, Integer>> getTopChallengeParticipants(int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, challenges_participated FROM players " +
                          "WHERE challenges_participated > 0 " +
                          "ORDER BY challenges_participated DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int participated = rs.getInt("challenges_participated");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, participated));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top challenge participants");
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Get top players by playtime
     */
    public List<Map.Entry<UUID, Long>> getTopByPlaytime(int limit) {
        List<Map.Entry<UUID, Long>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, playtime FROM players " +
                          "WHERE playtime > 0 " +
                          "ORDER BY playtime DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        long playtime = rs.getLong("playtime");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, playtime));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top players by playtime");
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Get top players by blocks explored
     */
    public List<Map.Entry<UUID, Integer>> getTopByBlocksExplored(int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, total_blocks_explored FROM players " +
                          "WHERE total_blocks_explored > 0 " +
                          "ORDER BY total_blocks_explored DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int explored = rs.getInt("total_blocks_explored");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, explored));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top explorers");
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Get top players by quests completed
     */
    public List<Map.Entry<UUID, Integer>> getTopByQuestsCompleted(int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, quests_completed FROM players " +
                          "WHERE quests_completed > 0 " +
                          "ORDER BY quests_completed DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int completed = rs.getInt("quests_completed");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, completed));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top by quests completed");
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Get top players for a specific block type mined
     */
    public List<Map.Entry<UUID, Integer>> getTopByBlockType(String blockType, int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, count FROM blocks_mined " +
                          "WHERE block_type = ? " +
                          "ORDER BY count DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, blockType);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int count = rs.getInt("count");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, count));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top by block type: " + blockType);
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Get top players for a specific mob type killed
     */
    public List<Map.Entry<UUID, Integer>> getTopByMobType(String mobType, int limit) {
        List<Map.Entry<UUID, Integer>> topPlayers = new ArrayList<>();
        
        try {
            String query = "SELECT uuid, count FROM mobs_killed " +
                          "WHERE mob_type = ? " +
                          "ORDER BY count DESC LIMIT ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, mobType);
                stmt.setInt(2, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        UUID uuid = UUID.fromString(rs.getString("uuid"));
                        int count = rs.getInt("count");
                        topPlayers.add(new AbstractMap.SimpleEntry<>(uuid, count));
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid UUID in database: " + rs.getString("uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.warning("Failed to get top by mob type: " + mobType);
            e.printStackTrace();
        }
        
        return topPlayers;
    }
    
    /**
     * Close database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.warning("Failed to close database connection");
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get database connection (for advanced queries)
     */
    public Connection getConnection() {
        return connection;
    }
}

