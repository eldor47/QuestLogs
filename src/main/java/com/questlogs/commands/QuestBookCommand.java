package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.gui.QuestBookGUI;
import com.questlogs.gui.QuestBookListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestBookCommand implements CommandExecutor {
    
    private final QuestLogsPlugin plugin;
    private final QuestBookGUI gui;
    
    public QuestBookCommand(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.gui = new QuestBookGUI(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length > 0 && args[0].equalsIgnoreCase("get")) {
            // Give the player a quest book item
            QuestBookListener listener = new QuestBookListener(plugin);
            listener.giveQuestBook(player);
            return true;
        }
        
        // Open the quest book GUI
        gui.openQuestBook(player);
        
        return true;
    }
}


