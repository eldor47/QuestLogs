# Quest Types

QuestLogs supports 6 different quest types. This page explains each type and provides examples.

## Quest Structure

All quests follow this basic structure:

```yaml
quest_id:
  name: "Display Name"
  description: "What the player needs to do"
  type: QUEST_TYPE
  target: TARGET_VALUE
  required_amount: NUMBER
  rewards:
    ITEM_NAME: AMOUNT
  xp_reward: AMOUNT
  gui_slot: SLOT_NUMBER      # Optional
  gui_icon: MATERIAL_NAME    # Optional
  prerequisites:             # Optional
    - other_quest_id
```

---

## 1. EXPLORE_BLOCKS

Track blocks explored (distance traveled).

### Configuration

```yaml
explorer_quest:
  name: "World Explorer"
  description: "Explore 5000 blocks"
  type: EXPLORE_BLOCKS
  target: ANY              # Always use ANY for exploration
  required_amount: 5000
  rewards:
    DIAMOND: 5
    ENDER_PEARL: 8
  xp_reward: 500
```

### How It Works

- Tracks horizontal movement (X and Z coordinates)
- Each new chunk coordinate = 1 block explored
- Works in all dimensions (Overworld, Nether, End)
- Player gets notifications every N blocks (configurable in `config.yml`)

### Example Progression

```yaml
novice_explorer:
  name: "Novice Explorer"
  type: EXPLORE_BLOCKS
  target: ANY
  required_amount: 1000
  rewards:
    IRON_INGOT: 16
  xp_reward: 100

apprentice_explorer:
  name: "Apprentice Explorer"
  type: EXPLORE_BLOCKS
  target: ANY
  required_amount: 5000
  prerequisites:
    - novice_explorer
  rewards:
    DIAMOND: 3
  xp_reward: 300
```

---

## 2. MINE_BLOCKS

Track specific ores or blocks mined.

### Configuration

```yaml
diamond_miner:
  name: "Diamond Miner"
  description: "Mine 50 diamond ore"
  type: MINE_BLOCKS
  target: DIAMOND_ORE      # Specific block type
  required_amount: 50
  rewards:
    DIAMOND_PICKAXE: 1
    ENCHANTED_BOOK: 1
  enchantments:
    ENCHANTED_BOOK:
      FORTUNE: 3
  xp_reward: 1000
```

### Supported Targets

Any mineable block:
- `STONE`, `COBBLESTONE`, `DEEPSLATE`
- `COAL_ORE`, `IRON_ORE`, `GOLD_ORE`, `DIAMOND_ORE`, `EMERALD_ORE`
- `DEEPSLATE_COAL_ORE`, `DEEPSLATE_IRON_ORE`, etc.
- `ANCIENT_DEBRIS`, `NETHER_QUARTZ_ORE`
- And more...

### Multiple Ore Types

Create separate quests for each ore type:

```yaml
iron_collector:
  name: "Iron Collector"
  type: MINE_BLOCKS
  target: IRON_ORE
  required_amount: 100
  # ...

coal_collector:
  name: "Coal Collector"
  type: MINE_BLOCKS
  target: COAL_ORE
  required_amount: 200
  # ...
```

---

## 3. BREAK_BLOCKS

Track any blocks broken (not just mined).

### Configuration

```yaml
lumberjack:
  name: "Lumberjack"
  description: "Break 500 oak logs"
  type: BREAK_BLOCKS
  target: OAK_LOG
  required_amount: 500
  rewards:
    DIAMOND_AXE: 1
    ENCHANTED_BOOK: 1
  enchantments:
    ENCHANTED_BOOK:
      EFFICIENCY: 5
  xp_reward: 300
```

### Difference from MINE_BLOCKS

- `MINE_BLOCKS`: Only counts blocks that drop items when mined
- `BREAK_BLOCKS`: Counts ANY block broken, including placed blocks

### Use Cases

- Woodcutting quests (logs)
- Clearing quests (leaves, grass)
- Harvesting quests (crops, melons)

---

## 4. PLACE_BLOCKS

Track blocks placed by the player.

### Configuration

```yaml
builder:
  name: "Master Builder"
  description: "Place 1000 cobblestone blocks"
  type: PLACE_BLOCKS
  target: COBBLESTONE
  required_amount: 1000
  rewards:
    DIAMOND: 5
    IRON_BLOCK: 10
  xp_reward: 500
```

### Use Cases

- Building quests
- Base construction challenges
- Decorative quests (place specific blocks)

---

## 5. CRAFT_ITEMS

Track items crafted by the player.

### Configuration

```yaml
craftsman:
  name: "Craftsman"
  description: "Craft 64 sticks"
  type: CRAFT_ITEMS
  target: STICK
  required_amount: 64
  rewards:
    DIAMOND: 2
  xp_reward: 100
```

### How It Works

- Tracks items crafted via crafting table or inventory
- Counts the output amount (crafting 1 stick counts as 4 sticks)
- Works with all craftable items

### Examples

```yaml
toolmaker:
  name: "Toolmaker"
  description: "Craft 16 iron pickaxes"
  type: CRAFT_ITEMS
  target: IRON_PICKAXE
  required_amount: 16
  rewards:
    DIAMOND_PICKAXE: 1
  xp_reward: 300

chef:
  name: "Chef"
  description: "Craft 128 bread"
  type: CRAFT_ITEMS
  target: BREAD
  required_amount: 128
  rewards:
    GOLDEN_APPLE: 5
  xp_reward: 200
```

---

## 6. KILL_MOBS

Track specific mobs killed.

### Configuration

```yaml
zombie_slayer:
  name: "Zombie Slayer"
  description: "Kill 50 zombies"
  type: KILL_MOBS
  target: ZOMBIE
  required_amount: 50
  rewards:
    DIAMOND_SWORD: 1
    ENCHANTED_BOOK: 1
  enchantments:
    ENCHANTED_BOOK:
      SHARPNESS: 5
  xp_reward: 500
```

### Supported Mobs

**Hostile:**
- `ZOMBIE`, `SKELETON`, `CREEPER`, `SPIDER`, `ENDERMAN`
- `WITCH`, `BLAZE`, `GHAST`, `PIGLIN`, `HOGLIN`
- And more...

**Passive:**
- `COW`, `PIG`, `SHEEP`, `CHICKEN`
- `VILLAGER`, `IRON_GOLEM`

**Bosses:**
- `ENDER_DRAGON`, `WITHER`

### Examples

```yaml
monster_hunter:
  name: "Monster Hunter"
  description: "Kill 25 creepers"
  type: KILL_MOBS
  target: CREEPER
  required_amount: 25
  rewards:
    TNT: 16
  xp_reward: 400

dragon_slayer:
  name: "Dragon Slayer"
  description: "Kill the Ender Dragon"
  type: KILL_MOBS
  target: ENDER_DRAGON
  required_amount: 1
  rewards:
    NETHERITE_INGOT: 5
    ENCHANTED_BOOK: 1
  enchantments:
    ENCHANTED_BOOK:
      MENDING: 1
  xp_reward: 5000
```

---

## Quest Prerequisites

Create sequential quest chains:

```yaml
novice_miner:
  name: "Novice Miner"
  type: MINE_BLOCKS
  target: COAL_ORE
  required_amount: 50
  rewards:
    IRON_PICKAXE: 1
  xp_reward: 100

apprentice_miner:
  name: "Apprentice Miner"
  type: MINE_BLOCKS
  target: IRON_ORE
  required_amount: 50
  prerequisites:
    - novice_miner      # Must complete novice_miner first
  rewards:
    DIAMOND_PICKAXE: 1
  xp_reward: 300

master_miner:
  name: "Master Miner"
  type: MINE_BLOCKS
  target: DIAMOND_ORE
  required_amount: 25
  prerequisites:
    - apprentice_miner  # Must complete apprentice_miner first
  rewards:
    NETHERITE_PICKAXE: 1
    ENCHANTED_BOOK: 1
  enchantments:
    ENCHANTED_BOOK:
      FORTUNE: 3
  xp_reward: 1000
```

---

## GUI Customization

Position quests in specific slots and use custom icons:

```yaml
starter_quest:
  name: "First Steps"
  type: EXPLORE_BLOCKS
  target: ANY
  required_amount: 100
  gui_slot: 10          # Top-left area (second row, second slot)
  gui_icon: COMPASS     # Use a compass as the icon
  rewards:
    DIAMOND: 1
  xp_reward: 50
```

### Slot Numbers

Chest inventory slots (0-53):
```
Row 1:  0  1  2  3  4  5  6  7  8
Row 2:  9 10 11 12 13 14 15 16 17
Row 3: 18 19 20 21 22 23 24 25 26
Row 4: 27 28 29 30 31 32 33 34 35
Row 5: 36 37 38 39 40 41 42 43 44
Row 6: 45 46 47 48 49 50 51 52 53
```

---

## Tips

1. **Start Simple**: Begin with a few easy quests to test
2. **Balance Rewards**: Don't give too many diamonds for simple quests
3. **Use Prerequisites**: Create natural progression paths
4. **Test Thoroughly**: Complete each quest type to verify it works
5. **Check Names**: Use `/questadmin list` to see all quest IDs

## Next Steps

- [Reward System](Reward-System) - Configure enchantments and XP
- [Examples](Examples) - See complete quest configurations
- [Commands](Commands-&-Permissions) - Manage quests as admin

