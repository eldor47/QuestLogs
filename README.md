# QuestLogs - Minecraft Spigot Plugin

A quest system plugin for Minecraft Java Edition 1.21.10 that allows players to complete quests and track their exploration progress.

## Features

- **📖 Quest Book GUI**: Beautiful visual interface - right-click a book to browse quests!
- **🎨 GUI Layout Customization**: Position quests anywhere & use custom icons in the Quest Book!
- **🔒 Quest Prerequisites**: Create sequential quest chains with locked/unlocked progression
- **📊 Player Statistics System**: Track lifetime player stats - blocks mined, mobs killed, quests completed, and more!
- **🎮 Timed Challenges**: Server-wide competitive mini-games with leaderboards and rewards!
- **Block Exploration Tracking**: Automatically tracks the number of blocks players explore
- **Mining Quest System**: Track specific ores and blocks mined with configurable targets
- **🆕 Mob Kill Tracking**: Track kills of specific mobs/entities with configurable targets
- **🎮 Break/Place/Craft Quests**: Track blocks broken, placed, and items crafted
- **Quest System**: Create and manage custom quests with rewards
- **Automatic Reward System**: Players automatically receive items when completing quests
- **Structured YAML Rewards**: Clean, organized reward format
- **Progress Tracking**: Visual progress bars and completion status (no overfilling!)
- **Smart Notifications**: Only unlocked quests track progress - no spam!
- **Dual Interface**: GUI Quest Book OR traditional commands - your choice!
- **Persistent Storage**: All quest data and player progress is saved to files

## Installation

1. Download the compiled `QuestLogs.jar` file
2. Place it in your server's `plugins` folder
3. Restart or reload your server
4. The plugin will create necessary configuration files on first start

## Configuration

The plugin creates several files in your server's data folder:

- `config.yml` - Main plugin configuration
- `quests.yml` - Quest definitions
- `progress.yml` - Player progress data
- `stats.db` - **SQLite database** for player statistics (fast & efficient!)

### Creating Quests

Edit `plugins/QuestLogs/quests.yml` to add new quests:

```yaml
quests:
  explore_1000:
    name: "Exploring the World"
    description: "Explore 1,000 blocks"
    type: EXPLORE_BLOCKS
    targetAmount: 1000
    active: true
    rewards:
      DIAMOND: 5
      IRON_SWORD: 1
      BREAD: 32
      TORCH: 64
  
  # Quest with prerequisite (locked until explore_1000 is complete)
  advanced_explorer:
    name: "Advanced Explorer"
    description: "Explore 5,000 blocks"
    type: EXPLORE_BLOCKS
    targetAmount: 5000
    active: true
    prerequisite: explore_1000  # 🔒 Requires explore_1000 first!
    rewards:
      DIAMOND: 20
      EMERALD: 10
```

### Quest Types

Supported quest types:
- `EXPLORE_BLOCKS` - Track blocks explored
- `MINE_BLOCKS` - Track specific blocks/ores mined (configurable per block type)
- `BREAK_BLOCKS` - **NEW!** Track specific blocks broken (configurable per block type)
- `PLACE_BLOCKS` - **NEW!** Track specific blocks placed (configurable per block type)
- `CRAFT_ITEMS` - **NEW!** Track specific items crafted (configurable per item type)
- `KILL_MOBS` - Track specific mob/entity kills (configurable per mob type)

#### Mining Quests

Mining quests allow you to track specific blocks or ores. Each block type can have its own target amount:

```yaml
quests:
  mining_beginner:
    name: "Beginner Miner"
    description: "Mine various ores and blocks"
    type: MINE_BLOCKS
    active: true
    blocks:
      STONE: 100
      DIORITE: 20
      COAL_ORE: 32
      COPPER_ORE: 16
      IRON_ORE: 10
      EMERALD_ORE: 3
      DIAMOND_ORE: 5
      GOLD_ORE: 8
      NETHERRACK: 64
    rewards:
      IRON_PICKAXE: 1
      IRON_SHOVEL: 1
      TORCH: 64
      COOKED_BEEF: 16
```

Supported block types include:
- `STONE`, `DIORITE`, `ANDESITE`, `GRANITE`
- `COAL_ORE`, `COPPER_ORE`, `IRON_ORE`, `GOLD_ORE`
- `DIAMOND_ORE`, `EMERALD_ORE`, `LAPIS_ORE`, `REDSTONE_ORE`
- `NETHERRACK`, `NETHER_GOLD_ORE`, `ANCIENT_DEBRIS`
- Any other Minecraft block material name

#### Mob Kill Quests 🆕

Mob kill quests allow you to track kills of specific mobs. Each mob type can have its own target amount:

```yaml
quests:
  monster_slayer:
    name: "Monster Slayer"
    description: "Defeat common hostile mobs"
    type: KILL_MOBS
    active: true
    mobs:
      ZOMBIE: 10
      SKELETON: 10
      CREEPER: 5
      SPIDER: 5
    rewards:
      DIAMOND_SWORD: 1
      COOKED_BEEF: 16
      TORCH: 32
```

Supported mob types include:
- **Hostile:** `ZOMBIE`, `SKELETON`, `CREEPER`, `SPIDER`, `ENDERMAN`, `WITCH`
- **Nether:** `ZOMBIFIED_PIGLIN`, `PIGLIN`, `BLAZE`, `GHAST`, `MAGMA_CUBE`, `HOGLIN`, `WITHER_SKELETON`
- **End:** `ENDERMAN`, `SHULKER`, `ENDER_DRAGON`
- **Boss:** `ENDER_DRAGON`, `WITHER`, `ELDER_GUARDIAN`
- **Raid:** `PILLAGER`, `VINDICATOR`, `EVOKER`, `RAVAGER`
- **Water:** `GUARDIAN`, `ELDER_GUARDIAN`, `DROWNED`
- **Passive:** `COW`, `PIG`, `CHICKEN`, `SHEEP`
- Any other Minecraft entity type

See **MOB_KILL_QUESTS_GUIDE.md** for complete documentation and 20+ examples!

## Commands

### Quest Book GUI (Recommended!)

- `/questbook` or `/qb` or `/quests` - Open the visual Quest Book menu
- `/questbook get` - Get a new Quest Book item
- **Right-click Quest Book item** - Open quest menu

### Player Commands (Text-Based)

- `/quest list` - List all available quests
- `/quest view <quest_id>` - View your progress on a specific quest

### Admin Commands

- `/questadmin reload` - Reload quest configuration
- `/questadmin complete <player> <quest_id>` - Complete a quest for a player (admin only)
- `/questadmin reset <player> <quest_id>` - Reset a player's progress on a specific quest (admin only)

### 📊 Statistics Commands (NEW!)

- `/queststats` or `/qstats` or `/stats` - View your player statistics
- `/queststats <player>` - View another player's statistics (requires permission)

### ✨ Tab Completion

All commands support **TAB completion**!
- Press TAB to see available options
- Type partial text and TAB to filter
- Works for commands, quest IDs, and player names

See **TAB_COMPLETION_GUIDE.md** for details.

## Permissions

- `questlogs.use` - Allows players to use quest commands and Quest Book (default: true)
- `questlogs.admin` - Allows admins to manage quests (default: op)
- `questlogs.stats.others` - Allows viewing other players' statistics (default: op)

## Quest Book GUI

The plugin includes a beautiful **Quest Book** interface that players can use instead of commands!

### Features:
- 📖 Physical Quest Book item (Written Book)
- 🖱️ Right-click to open visual quest menu
- 🎨 Color-coded quest status (✓ complete, ○ in progress, 🔒 locked)
- 📊 Visual progress bars for each quest
- 🧭 Quest type icons (Compass for exploration, Pickaxe for mining, Sword for combat)
- 👆 Click quests for detailed information
- **📊 NEW! Statistics Button** - Click to view your lifetime stats!

### How to Use:
1. Players receive a Quest Book when they join (configurable)
2. Right-click the Quest Book to open the GUI
3. Browse all quests visually in one menu
4. Click any quest to see details
5. **Click "Your Statistics" button (bottom-left) to view lifetime stats**
6. Use `/questbook get` to get a new book if lost

See **QUEST_BOOK_GUI_GUIDE.md** for complete documentation!

## 🎨 GUI Layout Customization (NEW!)

Take full control over **where quests appear** and **what icons they use** in the Quest Book!

### Custom Quest Positioning & Icons

Add these optional fields to any quest in `quests.yml`:

```yaml
quests:
  my_quest:
    name: "My Custom Quest"
    description: "A quest with custom GUI settings"
    type: MINE_BLOCKS
    active: true
    # ... other quest settings ...
    
    gui_slot: 0         # Position in GUI (0-44)
    gui_icon: EMERALD   # Material name for the icon
```

### GUI Slot Layout

The Quest Book has **54 slots total**:
- **Slots 0-44** (first 5 rows) are for quests
- **Slots 45-53** (bottom row) are reserved for UI buttons

```
Slot Layout:
 0   1   2   3   4   5   6   7   8
 9  10  11  12  13  14  15  16  17
18  19  20  21  22  23  24  25  26
27  28  29  30  31  32  33  34  35
36  37  38  39  40  41  42  43  44
─────────────────────────────────────
[Stats]                      [Help]
```

**Strategic Positions:**
- `gui_slot: 0` - Top-left (perfect for first quest)
- `gui_slot: 8` - Top-right (special/important quests)
- `gui_slot: 22` - Center (highlighted quest)
- `gui_slot: 44` - Bottom-right (final/legendary quest)
- Row starts: `0, 9, 18, 27, 36` (organize by category)

### Custom Icons

Use any Minecraft material name for quest icons:

```yaml
# Common quest icons
gui_icon: DIAMOND           # Valuable quest
gui_icon: NETHER_STAR       # Legendary quest
gui_icon: ENCHANTED_BOOK    # Knowledge quest
gui_icon: GOLDEN_APPLE      # Survival quest

# Progression examples
gui_icon: LEATHER_BOOTS     # Beginner
gui_icon: IRON_BOOTS        # Intermediate
gui_icon: DIAMOND_BOOTS     # Advanced
gui_icon: NETHERITE_BOOTS   # Master

# Category examples
gui_icon: WOODEN_SWORD      # Combat quest
gui_icon: STONE_PICKAXE     # Mining quest
gui_icon: OAK_PLANKS        # Building quest
gui_icon: CRAFTING_TABLE    # Crafting quest
```

### Organized Quest Layout Example

Create visually organized quest categories:

```yaml
quests:
  # === ROW 1: EXPLORATION (slots 0-2) ===
  exploration_1:
    gui_slot: 0
    gui_icon: LEATHER_BOOTS
    # ... quest config ...

  exploration_2:
    gui_slot: 1
    gui_icon: IRON_BOOTS
    prerequisite: exploration_1
    # ... quest config ...

  exploration_3:
    gui_slot: 2
    gui_icon: DIAMOND_BOOTS
    prerequisite: exploration_2
    # ... quest config ...

  # === ROW 2: MINING (slots 9-11) ===
  mining_1:
    gui_slot: 9
    gui_icon: COAL
    # ... quest config ...

  mining_2:
    gui_slot: 10
    gui_icon: IRON_ORE
    prerequisite: mining_1
    # ... quest config ...

  mining_3:
    gui_slot: 11
    gui_icon: DIAMOND_ORE
    prerequisite: mining_2
    # ... quest config ...

  # === ROW 3: COMBAT (slots 18-20) ===
  combat_1:
    gui_slot: 18
    gui_icon: WOODEN_SWORD
    # ... quest config ...

  combat_2:
    gui_slot: 19
    gui_icon: IRON_SWORD
    prerequisite: combat_1
    # ... quest config ...

  # === LEGENDARY QUEST (bottom-right corner) ===
  final_boss:
    gui_slot: 44
    gui_icon: NETHER_STAR
    prerequisite: combat_3
    # ... quest config ...
```

### How It Works

1. **Custom Slot Quests** are placed first at their specified positions
2. **Auto-Slot Quests** (without `gui_slot`) automatically fill empty slots
3. **Invalid slots** (< 0 or >= 45) are auto-assigned
4. **Custom Icons** override default type-based icons
5. **Default Icons** (if no `gui_icon` specified):
   - `EXPLORE_BLOCKS` → `COMPASS`
   - `MINE_BLOCKS` → `DIAMOND_PICKAXE`
   - `BREAK_BLOCKS` → `IRON_PICKAXE`
   - `PLACE_BLOCKS` → `BRICKS`
   - `CRAFT_ITEMS` → `CRAFTING_TABLE`
   - `KILL_MOBS` → `DIAMOND_SWORD`

### Design Tips

**1. Use Icon Progression** - Show advancement visually:
- `COAL` → `IRON_ORE` → `GOLD_ORE` → `DIAMOND_ORE`
- `WOODEN_SWORD` → `STONE_SWORD` → `IRON_SWORD` → `DIAMOND_SWORD` → `NETHERITE_SWORD`

**2. Group by Category** - Keep similar quests together:
- Row 1: Exploration quests
- Row 2: Mining quests
- Row 3: Combat quests
- Row 4: Building quests
- Row 5: Crafting quests

**3. Highlight Special Quests** - Use rare items for important quests:
- `NETHER_STAR` - Final quest
- `ENCHANTED_GOLDEN_APPLE` - Rare challenge
- `ELYTRA` - Special achievement
- `TOTEM_OF_UNDYING` - Hard challenge

**4. Sequential Chains** - Place prerequisite quests next to each other:
```yaml
quest_1:  gui_slot: 0   # Start
quest_2:  gui_slot: 1   # Middle
quest_3:  gui_slot: 2   # End
```

**5. Leave Visual Space** - Empty slots create organization:
```yaml
# Exploration: slots 0-3
# Empty row: slots 9-17
# Combat: slots 18-22
```

### Full Example

See `examples/quests_with_gui_customization.yml` for a complete example showing:
- ✅ Organized rows by category
- ✅ Custom icons for progression
- ✅ Special positioning for legendary quests
- ✅ Visual quest chains with prerequisites
- ✅ Empty space for visual organization

### Optional Fields

Both fields are **completely optional**:
- Without `gui_slot`: Quests auto-fill from left to right, top to bottom
- Without `gui_icon`: Uses quest type default icon

You can mix and match - some quests with custom settings, others automatic!

## Player Statistics System 📊

Track **ALL** player activities with the comprehensive statistics system powered by **SQLite**!

### What Gets Tracked (Everything, Not Just Quest Items!)
- ✓ Quest completions
- 🧭 Blocks explored (every block)
- ⛏️ Blocks mined (ALL ores - stone, coal, diamond, etc.)
- 🔨 Blocks broken (ALL blocks - oak planks, ice, dirt, etc.)
- 🧱 Blocks placed (ALL blocks)
- 🔧 Items crafted (ALL items)
- ⚔️ Mobs killed (ALL mobs - zombies, pigs, cows, etc.)
- 📅 Activity timestamps (first seen / last seen)

**Important:** Statistics track **everything you do**, not just blocks/items in active quests!

### How to View Stats

**Command Line:**
```bash
/queststats              # Your own stats
/queststats Steve        # Another player's stats (op only)
```

**Quest Book GUI:**
1. Open Quest Book (right-click the book item or `/questbook`)
2. Click the "Your Statistics" button (bottom-left corner)
3. Browse your lifetime achievements!
4. Click the back arrow to return to Quest Book

### Key Differences: Quest Progress vs. Statistics

| Quest Progress | Player Statistics |
|---------------|-------------------|
| Tracks toward quest goals | Tracks **ALL** activity (even non-quest items!) |
| Stops at target (50/50) | Never stops counting |
| Resets per quest | Permanent lifetime stats |
| Stored in `progress.yml` (YAML) | Stored in `stats.db` (SQLite) |
| Only quest-related blocks | Every block you break/place/mine |

**Example:**
- Quest Progress: `50/50 Coal Ore ✓` (quest complete, stops counting)
- Player Statistics: `Total Coal Mined: 2,458` (lifetime, includes ALL coal ever mined)
- Break oak planks with no quest? → Still tracked in statistics!

### Performance: SQLite Database ⚡

Statistics are stored in a high-performance **SQLite database** (`stats.db`):
- ✅ **10-100x faster** than YAML files
- ✅ **50-80% smaller** file size
- ✅ **On-demand loading** - only loads what's needed
- ✅ **ACID transactions** - no data corruption
- ✅ **Scalable** - handles 1000+ players easily
- ✅ **Queryable** - perfect for future leaderboards

See **PLAYER_STATISTICS_GUIDE.md** for complete documentation!

## Building from Source

1. Make sure you have Java 21+ and Maven installed
2. Clone this repository or download the source code
3. Run `mvn clean package` in the project directory
4. The compiled JAR will be in `target/QuestLogs-1.0.0.jar`
5. Copy the JAR to your server's `plugins` folder

**Note:** The plugin includes SQLite JDBC driver (bundled automatically via Maven Shade Plugin)

## Usage Examples

### Creating a Custom Quest

**Exploration Quest:**
```yaml
quests:
  my_custom_quest:
    name: "Journey to the Nether"
    description: "Explore 10,000 blocks to reach the Nether"
    type: EXPLORE_BLOCKS
    targetAmount: 10000
    active: true
    rewards:
      DIAMOND: 10
      ELYTRA: 1
      FIREWORK_ROCKET: 64
```

**Mining Quest:**
```yaml
quests:
  custom_mining_quest:
    name: "Diamond Hunter"
    description: "Find and mine rare gems"
    type: MINE_BLOCKS
    active: true
    blocks:
      DIAMOND_ORE: 10
      EMERALD_ORE: 5
      LAPIS_ORE: 20
    rewards:
      DIAMOND_HELMET: 1
      DIAMOND_CHESTPLATE: 1
      DIAMOND_LEGGINGS: 1
      DIAMOND_BOOTS: 1
```

## Rewards System

The plugin now **automatically gives items** to players when they complete quests! 

### Reward Formats

The plugin supports **TWO reward formats** - use whichever you prefer!

#### 🆕 Structured YAML Format (Recommended)
Clean, organized YAML structure with no parsing:

```yaml
quests:
  my_quest:
    name: "Quest Name"
    type: EXPLORE_BLOCKS
    targetAmount: 1000
    active: true
    rewards:
      DIAMOND: 10
      IRON_PICKAXE: 1
      ENCHANTED_BOOK: 1  # Enchanted books supported!
      TORCH: 64
      BREAD: 32
    enchantments:
      IRON_PICKAXE:
        EFFICIENCY: 3
      ENCHANTED_BOOK:  # Stored enchantments for books
        FORTUNE: 2
```

#### String Format (Still Supported)
Original text-based format:

```yaml
reward: "DIAMOND x10 + IRON_PICKAXE + TORCH x64"
```

### Mixed Format
You can even use both! Use structured for actual items, string for display:

```yaml
reward: "Starter Kit Bundle"  # Display text
rewards:
  IRON_PICKAXE: 1
  TORCH: 64
  BREAD: 32
```

### Features:
- ✅ **NEW:** Structured YAML rewards format
- ✅ **NEW:** Enchanted book support with stored enchantments
- ✅ Automatically parsed from reward strings (legacy)
- ✅ Supports quantities (any amount)
- ✅ Items added to player inventory
- ✅ Drops on ground if inventory full
- ✅ Configurable completion sound
- ✅ Optional broadcast to server
- ✅ Backward compatible with old format

See **STRUCTURED_REWARDS_GUIDE.md** and **REWARD_SYSTEM_SETUP.md** for complete documentation!

### Viewing Progress

Players can view their progress by typing:
```
/quest list
```
This shows all available quests and their completion status.

To see detailed progress:
```
/quest view explore_1000
```

For mining quests, players will see a detailed breakdown of each block type with visual progress bars:
```
/quest view mining_beginner
```

Example output:
```
=== Beginner Miner ===
Mine various ores and blocks
Block Progress:
✓ Stone: 100/100 [▰▰▰▰▰▰▰▰▰▰]
○ Coal Ore: 15/32 [▰▰▰▰▱▱▱▱▱▱]
○ Iron Ore: 3/10 [▰▰▰▱▱▱▱▱▱▱]
Overall: 1/5 objectives complete
Reward: Iron Pickaxe + 64 Torches
```

## Development

This plugin is built with:
- Java 21
- Spigot API 1.21.1
- Maven for build management
- SQLite JDBC 3.45.0.0 (for statistics storage)
- Maven Shade Plugin (bundles dependencies)

## License

This project is open source and available for modification.

## Contributing

Feel free to submit issues or pull requests if you'd like to contribute!

## Troubleshooting

**Quest progress not updating?**
- Make sure the quest is active in `quests.yml`
- Check that the player has the correct permissions
- Verify the quest type matches what the player is doing (e.g., EXPLORE_BLOCKS for exploration)

**Plugin not loading?**
- Check your server console for errors
- Ensure you're running Spigot/Paper 1.21.10 or compatible version
- Verify Java 21+ is installed

## 📚 Documentation

Comprehensive guides are available:

- **[PREREQUISITE_SYSTEM_GUIDE.md](PREREQUISITE_SYSTEM_GUIDE.md)** - Sequential quest chains & locked quests
- **[WHATS_NEW_PREREQUISITES.md](WHATS_NEW_PREREQUISITES.md)** - New prerequisite features overview
- **[QUEST_BOOK_GUI_GUIDE.md](QUEST_BOOK_GUI_GUIDE.md)** - Quest Book GUI usage
- **[MINING_QUESTS_GUIDE.md](MINING_QUESTS_GUIDE.md)** - Mining quest setup
- **[MOB_KILL_QUESTS_GUIDE.md](MOB_KILL_QUESTS_GUIDE.md)** - Mob kill quest setup
- **[STRUCTURED_REWARDS_GUIDE.md](STRUCTURED_REWARDS_GUIDE.md)** - Reward configuration
- **[TAB_COMPLETION_GUIDE.md](TAB_COMPLETION_GUIDE.md)** - Command tab completion
- **[CONFIG_EXAMPLES.md](CONFIG_EXAMPLES.md)** - Configuration examples
- **[MATERIAL_NAMES_REFERENCE.md](MATERIAL_NAMES_REFERENCE.md)** - Minecraft material names
- **[PLAYER_STATISTICS_GUIDE.md](PLAYER_STATISTICS_GUIDE.md)** - Player statistics system
- **examples/quests_with_gui_customization.yml** - GUI layout examples

## Future Features

- Multiple prerequisites (require A AND B)
- Alternative prerequisites (require A OR B)
- Quest categories/tags
- Integration with economy plugins
- Translation support




