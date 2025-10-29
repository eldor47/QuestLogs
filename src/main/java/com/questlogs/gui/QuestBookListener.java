package com.questlogs.gui;

import com.questlogs.QuestLogsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class QuestBookListener implements Listener {
    
    private final QuestLogsPlugin plugin;
    private final QuestBookGUI gui;
    
    public QuestBookListener(QuestLogsPlugin plugin) {
        this.plugin = plugin;
        this.gui = new QuestBookGUI(plugin);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Give quest book if they don't have one and config allows it
        if (plugin.getConfig().getBoolean("give-questbook-on-join", true)) {
            if (!hasQuestBook(player)) {
                giveQuestBook(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item == null || item.getType() != Material.WRITTEN_BOOK) {
            return;
        }
        
        if (!item.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().contains("Quest Book")) {
            event.setCancelled(true);
            
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                gui.openQuestBook(player);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.contains("Quest Book") && !title.contains("Your Statistics")) {
            return;
        }
        
        event.setCancelled(true); // Prevent taking items
        
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        // Handle stats view
        if (title.contains("Your Statistics")) {
            // Check if they clicked the back button
            if (clicked.getType() == Material.ARROW && clicked.hasItemMeta() && 
                clicked.getItemMeta().hasDisplayName() && 
                clicked.getItemMeta().getDisplayName().contains("Back")) {
                gui.openQuestBook(player);
                player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            }
            return;
        }
        
        // Handle quest book
        // Check if they clicked the stats button
        if (clicked.getType() == Material.WRITABLE_BOOK && clicked.hasItemMeta() && 
            clicked.getItemMeta().hasDisplayName() && 
            clicked.getItemMeta().getDisplayName().contains("Statistics")) {
            gui.openStatsView(player);
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
            return;
        }
        
        // If they clicked a quest item (not the help item or stats button)
        if (clicked.getType() != Material.BOOK && clicked.getType() != Material.WRITABLE_BOOK) {
            // Show quest details in chat (inventory is already showing visual info)
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "=== Quest Details ===");
            
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                List<String> lore = clicked.getItemMeta().getLore();
                for (String line : lore) {
                    player.sendMessage(line);
                }
            }
            player.sendMessage(ChatColor.GOLD + "====================");
            player.sendMessage("");
            
            // Play a sound for feedback
            player.playSound(player.getLocation(), org.bukkit.Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        }
    }
    
    private boolean hasQuestBook(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                if (item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasDisplayName() && meta.getDisplayName().contains("Quest Book")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public void giveQuestBook(Player player) {
        ItemStack book = createQuestBook();
        player.getInventory().addItem(book);
        player.sendMessage(ChatColor.GOLD + "You received a Quest Book! Right-click to open.");
    }
    
    public static ItemStack createQuestBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        
        meta.setDisplayName(ChatColor.GOLD + "Quest Book");
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to view your quests");
        lore.add(ChatColor.GRAY + "Track your progress and rewards");
        lore.add("");
        lore.add(ChatColor.YELLOW + "A comprehensive guide to");
        lore.add(ChatColor.YELLOW + "all available quests!");
        
        meta.setLore(lore);
        book.setItemMeta(meta);
        
        return book;
    }
}


