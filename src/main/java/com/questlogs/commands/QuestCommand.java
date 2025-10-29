package com.questlogs.commands;

import com.questlogs.QuestLogsPlugin;
import com.questlogs.models.Quest;
import com.questlogs.models.QuestType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;

public class QuestCommand implements CommandExecutor {
    
    private final QuestLogsPlugin plugin;
    
    public QuestCommand(QuestLogsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "view":
                handleViewCommand(player, args);
                break;
            case "list":
                handleListCommand(player);
                break;
            default:
                sendHelpMessage(player);
                break;
        }
        
        return true;
    }
    
    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Quest Logs ===");
        player.sendMessage(ChatColor.YELLOW + "/quest view <quest_id> - View your progress on a quest");
        player.sendMessage(ChatColor.YELLOW + "/quest list - List all available quests");
        player.sendMessage(ChatColor.YELLOW + "/questbook - Open quest book GUI");
        player.sendMessage(ChatColor.GRAY + "Tip: Right-click your Quest Book item to open GUI!");
    }
    
    private void handleViewCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /quest view <quest_id>");
            return;
        }
        
        String questId = args[1];
        Quest quest = plugin.getQuestManager().getQuest(questId);
        
        if (quest == null || !quest.isActive()) {
            player.sendMessage(ChatColor.RED + "Quest not found!");
            return;
        }
        
        // Check if quest is available (prerequisites met)
        boolean isAvailable = plugin.getQuestManager().isQuestAvailable(player.getUniqueId(), questId);
        
        player.sendMessage(ChatColor.GOLD + "=== " + quest.getName() + " ===");
        player.sendMessage(ChatColor.GRAY + quest.getDescription());
        
        // Show prerequisite info if quest is locked
        if (!isAvailable && quest.hasPrerequisite()) {
            Quest prerequisiteQuest = plugin.getQuestManager().getQuest(quest.getPrerequisite());
            player.sendMessage("");
            player.sendMessage(ChatColor.RED + "🔒 This quest is LOCKED!");
            player.sendMessage(ChatColor.GRAY + "Required: " + ChatColor.YELLOW + 
                    (prerequisiteQuest != null ? prerequisiteQuest.getName() : quest.getPrerequisite()));
            player.sendMessage(ChatColor.GRAY + "Complete the required quest to unlock this one.");
            return;
        }
        player.sendMessage("");
        
        // Check if this is a block-based quest (mining, breaking, placing, crafting)
        if ((quest.getType() == QuestType.MINE_BLOCKS || quest.getType() == QuestType.BREAK_BLOCKS ||
             quest.getType() == QuestType.PLACE_BLOCKS || quest.getType() == QuestType.CRAFT_ITEMS) && 
             quest.hasBlockTargets()) {
            // Determine label based on quest type
            String label;
            switch (quest.getType()) {
                case MINE_BLOCKS:
                    label = "Mining Progress:";
                    break;
                case BREAK_BLOCKS:
                    label = "Break Progress:";
                    break;
                case PLACE_BLOCKS:
                    label = "Place Progress:";
                    break;
                case CRAFT_ITEMS:
                    label = "Crafting Progress:";
                    break;
                default:
                    label = "Block Progress:";
            }
            
            player.sendMessage(ChatColor.YELLOW + label);
            
            int totalComplete = 0;
            int totalRequired = quest.getBlockTargets().size();
            
            for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
                String blockType = entry.getKey();
                int required = entry.getValue();
                int current = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), questId, blockType);
                
                String blockName = formatMaterialName(blockType);
                String checkmark = current >= required ? ChatColor.GREEN + "✓ " : ChatColor.GRAY + "○ ";
                String progressBar = createProgressBar(current, required);
                
                player.sendMessage(checkmark + ChatColor.WHITE + blockName + ": " + 
                                 ChatColor.YELLOW + current + ChatColor.GRAY + "/" + required + 
                                 " " + progressBar);
                
                if (current >= required) {
                    totalComplete++;
                }
            }
            
            player.sendMessage(ChatColor.GOLD + "Overall: " + ChatColor.GREEN + totalComplete + 
                             ChatColor.GRAY + "/" + totalRequired + " objectives complete");
            
            if (totalComplete >= totalRequired) {
                player.sendMessage(ChatColor.GREEN + "✓ Quest Complete!");
            }
        } else if (quest.getType() == QuestType.KILL_MOBS && quest.hasMobTargets()) {
            // Mob kill quest
            player.sendMessage(ChatColor.YELLOW + "Mob Kill Progress:");
            
            int totalComplete = 0;
            int totalRequired = quest.getMobTargets().size();
            
            for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
                String mobType = entry.getKey();
                int required = entry.getValue();
                int current = plugin.getProgressManager().getMobProgress(player.getUniqueId(), questId, mobType);
                
                String mobName = formatMaterialName(mobType);
                String checkmark = current >= required ? ChatColor.GREEN + "✓ " : ChatColor.GRAY + "○ ";
                String progressBar = createProgressBar(current, required);
                
                player.sendMessage(checkmark + ChatColor.WHITE + mobName + ": " + 
                                 ChatColor.YELLOW + current + ChatColor.GRAY + "/" + required + 
                                 " " + progressBar);
                
                if (current >= required) {
                    totalComplete++;
                }
            }
            
            player.sendMessage(ChatColor.GOLD + "Overall: " + ChatColor.GREEN + totalComplete + 
                             ChatColor.GRAY + "/" + totalRequired + " objectives complete");
            
            if (totalComplete >= totalRequired) {
                player.sendMessage(ChatColor.GREEN + "✓ Quest Complete!");
            }
        } else {
            // Regular quest (exploration, etc.)
            int progress = plugin.getProgressManager().getPlayerProgress(player.getUniqueId(), questId);
            int target = quest.getTargetAmount();
            double percentage = target > 0 ? (double) progress / target * 100 : 0;
            
            player.sendMessage(ChatColor.YELLOW + "Progress: " + ChatColor.GREEN + progress + ChatColor.YELLOW + " / " + 
                             ChatColor.AQUA + target + ChatColor.YELLOW + " (" + String.format("%.1f", percentage) + "%)");
            
            if (progress >= target) {
                player.sendMessage(ChatColor.GREEN + "✓ Quest Complete!");
            }
        }
        
        player.sendMessage(ChatColor.GOLD + "Reward: " + ChatColor.WHITE + quest.getReward());
    }
    
    private String formatMaterialName(String material) {
        // Convert COAL_ORE to Coal Ore
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
    
    private String createProgressBar(int current, int target) {
        int barLength = 10;
        int filled = Math.min((int) ((double) current / target * barLength), barLength);
        
        StringBuilder bar = new StringBuilder(ChatColor.GRAY + "[");
        for (int i = 0; i < barLength; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN + "▰");
            } else {
                bar.append(ChatColor.DARK_GRAY + "▱");
            }
        }
        bar.append(ChatColor.GRAY + "]");
        
        return bar.toString();
    }
    
    private void handleListCommand(Player player) {
        Collection<Quest> allQuests = plugin.getQuestManager().getAllQuests();
        
        if (allQuests.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No quests available!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "=== Available Quests ===");
        for (Quest quest : allQuests) {
            if (!quest.isActive()) continue;
            
            boolean isAvailable = plugin.getQuestManager().isQuestAvailable(player.getUniqueId(), quest.getId());
            boolean isComplete;
            
            // Check completion based on quest type
            if ((quest.getType() == QuestType.MINE_BLOCKS || quest.getType() == QuestType.BREAK_BLOCKS ||
                 quest.getType() == QuestType.PLACE_BLOCKS || quest.getType() == QuestType.CRAFT_ITEMS) && 
                 quest.hasBlockTargets()) {
                isComplete = isMiningQuestComplete(player, quest); // Works for all block-based quests
            } else if (quest.getType() == QuestType.KILL_MOBS && quest.hasMobTargets()) {
                isComplete = isMobKillQuestComplete(player, quest);
            } else {
                int progress = plugin.getProgressManager().getPlayerProgress(player.getUniqueId(), quest.getId());
                int target = quest.getTargetAmount();
                isComplete = progress >= target;
            }
            
            String status;
            if (!isAvailable && !isComplete) {
                Quest prerequisiteQuest = quest.hasPrerequisite() ? 
                        plugin.getQuestManager().getQuest(quest.getPrerequisite()) : null;
                status = ChatColor.RED + "🔒 Locked" + 
                        (prerequisiteQuest != null ? 
                         ChatColor.GRAY + " (Requires: " + ChatColor.YELLOW + prerequisiteQuest.getName() + ChatColor.GRAY + ")" : 
                         "");
            } else if (isComplete) {
                status = ChatColor.GREEN + "✓ Complete";
            } else {
                status = ChatColor.YELLOW + "○ In Progress";
            }
            
            player.sendMessage(ChatColor.YELLOW + "- " + ChatColor.WHITE + quest.getName() + 
                             ChatColor.GRAY + " (" + quest.getId() + ") " + status);
        }
        player.sendMessage(ChatColor.GRAY + "Use /quest view <quest_id> for more details");
    }
    
    private boolean isMiningQuestComplete(Player player, Quest quest) {
        for (Map.Entry<String, Integer> entry : quest.getBlockTargets().entrySet()) {
            String blockType = entry.getKey();
            int required = entry.getValue();
            int current = plugin.getProgressManager().getBlockProgress(player.getUniqueId(), quest.getId(), blockType);
            
            if (current < required) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isMobKillQuestComplete(Player player, Quest quest) {
        for (Map.Entry<String, Integer> entry : quest.getMobTargets().entrySet()) {
            String mobType = entry.getKey();
            int required = entry.getValue();
            int current = plugin.getProgressManager().getMobProgress(player.getUniqueId(), quest.getId(), mobType);
            
            if (current < required) {
                return false;
            }
        }
        return true;
    }
}




