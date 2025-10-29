package com.questlogs;

import com.questlogs.commands.QuestCommand;
import com.questlogs.commands.QuestAdminCommand;
import com.questlogs.commands.QuestBookCommand;
import com.questlogs.commands.QuestStatsCommand;
import com.questlogs.commands.ChallengeCommand;
import com.questlogs.commands.ChallengeAdminCommand;
import com.questlogs.commands.QuestTabCompleter;
import com.questlogs.commands.QuestAdminTabCompleter;
import com.questlogs.commands.QuestBookTabCompleter;
import com.questlogs.commands.QuestStatsTabCompleter;
import com.questlogs.commands.ChallengeAdminTabCompleter;
import com.questlogs.listeners.ExplorationListener;
import com.questlogs.listeners.BlockBreakListener;
import com.questlogs.listeners.BlockPlaceListener;
import com.questlogs.listeners.CraftItemListener;
import com.questlogs.listeners.MobKillListener;
import com.questlogs.listeners.PlaytimeListener;
import com.questlogs.gui.QuestBookListener;
import com.questlogs.managers.QuestManager;
import com.questlogs.managers.PlayerProgressManager;
import com.questlogs.managers.ConfigManager;
import com.questlogs.managers.StatsManager;
import com.questlogs.managers.ChallengeManager;
import com.questlogs.rewards.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class QuestLogsPlugin extends JavaPlugin {
    
    private static QuestLogsPlugin instance;
    private QuestManager questManager;
    private PlayerProgressManager progressManager;
    private ConfigManager configManager;
    private RewardManager rewardManager;
    private StatsManager statsManager;
    private ChallengeManager challengeManager;
    private PlaytimeListener playtimeListener;
    private Logger logger;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        // Create required directories
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        
        // Initialize managers
        configManager = new ConfigManager(this);
        questManager = new QuestManager(this);
        progressManager = new PlayerProgressManager(this);
        rewardManager = new RewardManager(this);
        statsManager = new StatsManager(this);
        challengeManager = new ChallengeManager(this); // Initialize challenges last
        
        // Load configuration and quests
        configManager.loadConfig();
        questManager.loadQuests();
        progressManager.loadProgress();
        statsManager.loadStats();
        challengeManager.loadChallenges();
        
        // Register commands
        getCommand("quest").setExecutor(new QuestCommand(this));
        getCommand("questadmin").setExecutor(new QuestAdminCommand(this));
        getCommand("questbook").setExecutor(new QuestBookCommand(this));
        getCommand("queststats").setExecutor(new QuestStatsCommand(this));
        getCommand("challenge").setExecutor(new ChallengeCommand(this));
        getCommand("challengeadmin").setExecutor(new ChallengeAdminCommand(this));
        
        // Register tab completers
        getCommand("quest").setTabCompleter(new QuestTabCompleter(this));
        getCommand("questadmin").setTabCompleter(new QuestAdminTabCompleter(this));
        getCommand("questbook").setTabCompleter(new QuestBookTabCompleter());
        getCommand("queststats").setTabCompleter(new QuestStatsTabCompleter());
        getCommand("challengeadmin").setTabCompleter(new ChallengeAdminTabCompleter(this));
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new ExplorationListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftItemListener(this), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        playtimeListener = new PlaytimeListener(this);
        getServer().getPluginManager().registerEvents(playtimeListener, this);
        getServer().getPluginManager().registerEvents(new QuestBookListener(this), this);
        
        logger.info("QuestLogs has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save all data
        if (progressManager != null) {
            progressManager.saveProgress();
        }
        if (questManager != null) {
            questManager.saveQuests();
        }
        if (statsManager != null) {
            statsManager.close(); // Close database connection properly
        }
        if (challengeManager != null) {
            challengeManager.shutdown(); // Stop challenge scheduler
        }
        
        logger.info("QuestLogs has been disabled!");
    }
    
    public static QuestLogsPlugin getInstance() {
        return instance;
    }
    
    public QuestManager getQuestManager() {
        return questManager;
    }
    
    public PlayerProgressManager getProgressManager() {
        return progressManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public RewardManager getRewardManager() {
        return rewardManager;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public ChallengeManager getChallengeManager() {
        return challengeManager;
    }
    
    public PlaytimeListener getPlaytimeListener() {
        return playtimeListener;
    }
}




