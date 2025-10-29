package com.questlogs.managers;

import com.questlogs.QuestLogsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final QuestLogsPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}





