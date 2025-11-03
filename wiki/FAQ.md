# Frequently Asked Questions

Common questions and solutions for QuestLogs.

## Installation & Setup

### Q: What Minecraft version does this support?
**A:** Minecraft 1.21+ on Spigot or Paper servers.

### Q: Do I need any dependencies?
**A:** No! SQLite is bundled. Just drop the JAR in your plugins folder.

### Q: Does this work with Bukkit?
**A:** It requires Spigot or Paper. Bukkit alone is not supported.

### Q: Can I use this on a modded server?
**A:** It's designed for vanilla Spigot/Paper. Modded servers (Forge, Fabric) are not officially supported, but it may work on hybrid servers like Mohist or Arclight.

---

## Configuration

### Q: How do I add my own quests?
**A:** Edit `plugins/QuestLogs/quests.yml`. See [Quest Types](Quest-Types) for examples.

### Q: Can I disable challenges but keep quests?
**A:** Yes! In `challenges.yml`, set `enabled: false`.

### Q: How do I reload config without restarting?
**A:** Use `/questadmin reload`.

### Q: Where are the example quests?
**A:** Check the `examples/` folder on GitHub, or the auto-generated `quests.yml`.

### Q: Can quests reward money (Vault)?
**A:** Not currently. Only items, enchantments, and XP are supported.

---

## Quests

### Q: Why aren't my quests tracking?
**A:** Common causes:
1. Wrong quest type (e.g., using MINE_BLOCKS for placed blocks)
2. Quest already completed
3. Prerequisites not met
4. Target material name incorrect

### Q: How do I reset a player's quest progress?
**A:** Use `/questadmin reset <player>`.

### Q: Can I have multiple quests of the same type?
**A:** Yes! Just use different quest IDs.

### Q: Do quests track activity before accepting them?
**A:** Yes, progress is automatic. There's no "accept quest" mechanism.

### Q: Can I make quests optional or hidden?
**A:** Not currently. All quests in `quests.yml` are active for all players.

### Q: How do prerequisites work?
**A:** Set `prerequisites: [quest_id]` in the quest. Players must complete the prerequisite quest first. Locked quests appear with a barrier icon in the GUI.

---

## Rewards

### Q: Can I give enchanted items as rewards?
**A:** Yes! Use the enchantments format:
```yaml
rewards:
  IRON_PICKAXE: 1
  ENCHANTED_BOOK: 1  # Enchanted books are supported!
enchantments:
  IRON_PICKAXE:
    EFFICIENCY: 3
  ENCHANTED_BOOK:  # Stored enchantments for books
    FORTUNE: 3
```

### Q: What happens if a player's inventory is full?
**A:** Items drop on the ground at the player's location.

### Q: Can I use custom items from other plugins?
**A:** No, only vanilla Minecraft items are supported.

### Q: How do I give multiple of the same item?
**A:** Specify the amount:
```yaml
rewards:
  DIAMOND: 10
  IRON_INGOT: 32
```

### Q: Can enchanted books have multiple enchantments?
**A:** Yes! Enchanted books with stored enchantments are fully supported:
```yaml
rewards:
  ENCHANTED_BOOK: 1
enchantments:
  ENCHANTED_BOOK:
    FORTUNE: 3
    UNBREAKING: 3
```

---

## Statistics

### Q: Where are statistics stored?
**A:** In `plugins/QuestLogs/stats.db` (SQLite database).

### Q: Can I reset all statistics?
**A:** Yes, delete `stats.db` and restart the server. **This cannot be undone!**

### Q: Do statistics persist across server restarts?
**A:** Yes, they're stored in the database.

### Q: Can I export statistics?
**A:** The database is SQLite format. Use any SQLite viewer to access it.

### Q: Why is playtime not accurate?
**A:** Playtime only tracks when players are online. It doesn't count offline time.

### Q: Are statistics tracked for all players?
**A:** Yes, all player activity is automatically tracked.

---

## Challenges

### Q: How often do challenges run?
**A:** Configurable in `challenges.yml`. Default is every hour.

### Q: Can I manually start a challenge?
**A:** Yes! Use `/challengeadmin start <challenge_id>`.

### Q: Do challenges affect quests?
**A:** No, they're separate systems. Progress on challenges doesn't count toward quests.

### Q: Can I create custom challenges?
**A:** Yes! Add them to `challenges.yml`. See [Timed Challenges](Timed-Challenges).

### Q: Why won't challenges start?
**A:** Check:
1. `enabled: true` in settings
2. Enough players online (see `minimum-players`)
3. No other challenge is active

### Q: Can players see their challenge stats?
**A:** Yes! Use `/queststats` to see challenges won and participated.

---

## GUI

### Q: Can I change the GUI layout?
**A:** Yes! Use `gui_slot` to position quests and `gui_icon` to change icons.

### Q: What are the slot numbers?
**A:** 0-53 for chest inventory. See [Quest Types](Quest-Types#gui-customization) for the layout.

### Q: Can I have multiple GUI pages?
**A:** Not currently. The GUI is limited to one chest (54 slots).

### Q: Why are some quests showing a barrier icon?
**A:** They're locked by prerequisites. Complete the required quests first.

### Q: Can I change the GUI title?
**A:** Not currently. It's fixed as "Quest Book".

---

## Permissions

### Q: What permissions do players need?
**A:** Just `questlogs.use` for basic access.

### Q: How do I let mods view other player stats?
**A:** Give them `questlogs.stats.others`.

### Q: Do admins need special permissions?
**A:** Yes, `questlogs.admin` for all admin commands.

### Q: Can I make certain quests admin-only?
**A:** Not currently. All quests are available to all players.

---

## Performance

### Q: Does this lag the server?
**A:** No, it's designed to be lightweight. All tracking is asynchronous.

### Q: How much disk space does it use?
**A:** The SQLite database is typically <1 MB even with many players.

### Q: Can I use this on a large server?
**A:** Yes! It's been designed with scalability in mind.

---

## Troubleshooting

### Q: Commands aren't working
**A:** Check:
1. Correct permissions
2. No typos in command
3. Plugin loaded (`/plugins`)
4. Use tab completion

### Q: Plugin won't load
**A:** Check:
1. Java 21+ installed
2. Spigot/Paper 1.21+
3. No conflicting plugins
4. Server console errors

### Q: Progress isn't saving
**A:** Check:
1. `progress.yml` has write permissions
2. No disk space issues
3. Server console for errors

### Q: Database errors
**A:** The plugin will recreate `stats.db` if corrupted. Make backups!

### Q: Rewards not giving
**A:** Check:
1. Reward syntax in YAML
2. Material names are correct (use tab completion)
3. Player inventory space
4. Server console for errors

---

## Compatibility

### Q: Works with PlaceholderAPI?
**A:** Not currently. PAPI integration is planned for future updates.

### Q: Works with Citizens/Denizens NPCs?
**A:** No integration, but they shouldn't conflict.

### Q: Works with economy plugins (Vault)?
**A:** No integration currently. Only item/XP rewards are supported.

### Q: Works with other quest plugins?
**A:** Should work fine alongside other quest plugins, but may cause confusion for players.

---

## Updates & Support

### Q: How do I update the plugin?
**A:** 
1. Download the new JAR
2. Stop server
3. Replace old JAR
4. Start server

### Q: Will updates delete my data?
**A:** No, updates preserve:
- Quest progress (`progress.yml`)
- Statistics (`stats.db`)
- Your configurations

### Q: Where do I report bugs?
**A:** [GitHub Issues](https://github.com/YOUR-USERNAME/QuestLogs/issues)

### Q: Can I request features?
**A:** Yes! Use [GitHub Discussions](https://github.com/YOUR-USERNAME/QuestLogs/discussions).

### Q: Is there a Discord?
**A:** Check the GitHub repository for community links.

---

## Advanced

### Q: Can I edit the database directly?
**A:** Yes, but make backups first! Use any SQLite editor.

### Q: Can I import player data from another plugin?
**A:** Not automatically. You'd need to write a custom script.

### Q: Can I use this in a minigame?
**A:** Yes, but you'll want to reset progress between games using `/questadmin reset`.

### Q: Can I have per-world quests?
**A:** Not currently. Quests are server-wide.

### Q: Can I disable certain quest types?
**A:** Just don't include them in `quests.yml`.

---

## Still Have Questions?

- Check the [Wiki Home](Home) for more guides
- Ask in [GitHub Discussions](https://github.com/YOUR-USERNAME/QuestLogs/discussions)
- Report bugs in [GitHub Issues](https://github.com/YOUR-USERNAME/QuestLogs/issues)

