# Quest Examples

This folder contains example quest configurations for the QuestLogs plugin. Copy the one that best fits your server's needs to `plugins/QuestLogs/quests.yml`.

## Available Example Files

### `quests_exploration_only.yml`
**Best for:** Servers focusing only on exploration
- Contains only EXPLORE_BLOCKS quests
- 5 progressive exploration quests (500 to 25,000 blocks)
- Simple setup, no mining tracking

### `quests_mining_basic.yml`
**Best for:** Servers wanting simple mining progression
- Basic mining quests from tutorial to advanced
- Progressive difficulty (25 blocks to 200+ blocks)
- Focuses on common ores and stone

### `quests_mining_themed.yml`
**Best for:** Servers wanting variety in mining goals
- 20+ themed mining quests
- Organized by categories:
  - Stone Collection (Stone, Diorite, Granite, etc.)
  - Precious Gems (Diamond, Emerald, Lapis)
  - Metal Ores (Iron, Gold, Copper)
  - Redstone & Lapis
  - Nether dimension
  - End dimension

### `quests_mining_progression.yml`
**Best for:** Servers wanting a complete progression system
- 15 progressive quests from Novice to Grandmaster
- 4 tiers of difficulty
- Clear advancement path for players
- Final quest requires 2000+ blocks mined

### `quests_mixed.yml`
**Best for:** Most servers - balanced approach
- Mix of exploration and mining quests
- Includes basic, intermediate, and expert levels
- Themed quests (gems, redstone, Nether, deepslate)
- Good starting point for customization

### `quests_seasonal.yml`
**Best for:** Servers running special events
- Holiday quests (Halloween, Christmas, Easter, Summer)
- Event quests (Double XP, Speed Challenges, Server Anniversary)
- All set to `active: false` by default
- Enable specific quests during events

## How to Use

1. **Choose** an example file that fits your needs
2. **Copy** its contents to `plugins/QuestLogs/quests.yml`
3. **Customize** rewards and targets as needed
4. **Reload** the plugin with `/questadmin reload`

## Mixing Examples

You can combine multiple example files! For instance:

1. Take basic quests from `quests_mining_basic.yml`
2. Add a few themed quests from `quests_mining_themed.yml`
3. Include seasonal quests from `quests_seasonal.yml`
4. Mix in some exploration quests from `quests_mixed.yml`

Just make sure each quest has a **unique ID** (the quest key like `mining_beginner`, `explore_starter`, etc.).

## Customizing Rewards

Rewards are currently **display-only**. They appear in quest descriptions and completion messages. Examples:

```yaml
reward: "Diamond x5 + Iron Pickaxe"
reward: "Netherite Ingot + Fortune III Book"
reward: "Full Diamond Armor Set"
reward: "100 Gold + 50 XP Levels"
```

## Adjusting Difficulty

### Make Easier:
- Reduce block amounts by 50%
- Focus on common blocks (Stone, Coal)
- Lower target amounts to 10-50 blocks

### Make Harder:
- Increase block amounts by 200%
- Add more rare ores (Diamond, Emerald, Ancient Debris)
- Increase target amounts to 100-500 blocks

### Example Adjustment:
```yaml
# Original (Normal difficulty)
blocks:
  DIAMOND_ORE: 10
  EMERALD_ORE: 5

# Easy mode
blocks:
  DIAMOND_ORE: 5
  EMERALD_ORE: 2

# Hard mode
blocks:
  DIAMOND_ORE: 20
  EMERALD_ORE: 12
```

## Quest IDs

Each quest must have a unique ID. The ID is the key under `quests:`:

```yaml
quests:
  my_unique_quest:  # <-- This is the quest ID
    name: "Display Name"
    # ...
```

Quest IDs:
- Must be unique across all quests
- Can contain letters, numbers, and underscores
- Are used in commands: `/quest view my_unique_quest`
- Should be descriptive but short

## Tips

1. **Start Small**: Begin with 5-10 quests, add more later
2. **Test Targets**: Mine a bit yourself to check if targets are reasonable
3. **Progression**: Order quests from easy to hard
4. **Variety**: Mix different ore types in each quest
5. **Rewards**: Make rewards match difficulty
6. **Active Status**: Set `active: false` for quests you want to enable later

## Need Help?

- See `CONFIG_EXAMPLES.md` for configuration options
- See `MINING_QUESTS_GUIDE.md` for detailed mining quest guide
- Check the main `README.md` for commands and permissions


