# Timed Challenges

Timed challenges are server-wide competitive mini-games where players compete to achieve the most of a specific goal within a time limit.

## How Challenges Work

1. **Automatic Start**: Challenges start automatically based on the configured frequency
2. **Broadcast**: All players are notified when a challenge begins
3. **Compete**: Players compete to get the highest score
4. **Leaderboard**: Real-time tracking shows who's winning
5. **Rewards**: Top 3 players receive rewards when the challenge ends

## Configuration

Edit `plugins/QuestLogs/challenges.yml`:

### Global Settings

```yaml
settings:
  # Enable or disable the challenge system
  enabled: true
  
  # How often to start challenges (in seconds)
  # 3600 = 1 hour, 1800 = 30 minutes
  frequency: 3600
  
  # How long each challenge lasts (in seconds)
  # 300 = 5 minutes
  duration: 300
  
  # How often to announce progress (in seconds)
  # 60 = 1 minute
  announce-interval: 60
  
  # Whether to broadcast when challenges start
  broadcast-start: true
  
  # Whether to broadcast when challenges end
  broadcast-end: true
  
  # How many top players get rewards (1-10)
  reward-top-players: 3
  
  # Minimum players online to start a challenge
  minimum-players: 2
```

### Creating a Challenge

```yaml
challenge-pool:
  challenge_id:
    name: "Display Name"
    description: "What players need to do"
    type: CHALLENGE_TYPE
    target: TARGET_VALUE
    duration: 300  # Override global duration (optional)
    rewards:
      1st:
        ITEM_NAME: AMOUNT
      2nd:
        ITEM_NAME: AMOUNT
      3rd:
        ITEM_NAME: AMOUNT
```

## Challenge Types

### MINE_BLOCKS
Mine specific ores or blocks.

```yaml
diamond_dash:
  name: "Diamond Dash"
  description: "Mine the most diamond ore!"
  type: MINE_BLOCKS
  target: DIAMOND_ORE
  duration: 300
  rewards:
    1st:
      DIAMOND: 5
      IRON_INGOT: 16
      ENCHANTED_BOOK:
        enchantments:
          FORTUNE: 3
    2nd:
      DIAMOND: 3
      IRON_INGOT: 8
      ENCHANTED_BOOK:
        enchantments:
          SILK_TOUCH: 1
    3rd:
      DIAMOND: 1
      IRON_INGOT: 4
```

### KILL_MOBS
Kill specific mobs.

```yaml
zombie_hunter:
  name: "Zombie Hunt"
  description: "Kill the most zombies!"
  type: KILL_MOBS
  target: ZOMBIE
  duration: 300
  rewards:
    1st:
      IRON_INGOT: 12
      GOLD_INGOT: 4
      ENCHANTED_BOOK:
        enchantments:
          SHARPNESS: 2
    2nd:
      IRON_INGOT: 6
      GOLD_INGOT: 2
      ENCHANTED_BOOK:
        enchantments:
          SHARPNESS: 1
    3rd:
      IRON_INGOT: 3
      GOLD_INGOT: 1
```

### CRAFT_ITEMS
Craft specific items.

```yaml
crafting_master:
  name: "Crafting Master"
  description: "Craft the most items!"
  type: CRAFT_ITEMS
  target: STICK
  duration: 300
  rewards:
    1st:
      IRON_INGOT: 16
      GOLD_INGOT: 8
      ENCHANTED_BOOK:
        enchantments:
          MENDING: 1
    2nd:
      IRON_INGOT: 8
      GOLD_INGOT: 4
    3rd:
      IRON_INGOT: 4
      GOLD_INGOT: 2
```

### PLACE_BLOCKS
Place specific blocks.

```yaml
builder_challenge:
  name: "Speed Builder"
  description: "Place the most blocks!"
  type: PLACE_BLOCKS
  target: COBBLESTONE
  duration: 300
  rewards:
    1st:
      IRON_INGOT: 12
      OAK_PLANKS: 64
      ENCHANTED_BOOK:
        enchantments:
          EFFICIENCY: 3
    2nd:
      IRON_INGOT: 6
      OAK_PLANKS: 32
      ENCHANTED_BOOK:
        enchantments:
          EFFICIENCY: 2
    3rd:
      IRON_INGOT: 3
      OAK_PLANKS: 16
```

### BREAK_BLOCKS
Break specific blocks.

```yaml
wood_chopper:
  name: "Lumberjack Challenge"
  description: "Break the most logs!"
  type: BREAK_BLOCKS
  target: OAK_LOG
  duration: 300
  rewards:
    1st:
      IRON_INGOT: 24
      GOLDEN_APPLE: 8
      ENCHANTED_BOOK:
        enchantments:
          EFFICIENCY: 1
    2nd:
      IRON_INGOT: 12
      GOLDEN_APPLE: 4
    3rd:
      IRON_INGOT: 6
      GOLDEN_APPLE: 2
```

### EXPLORE_BLOCKS
Explore the most blocks.

```yaml
nether_explorer:
  name: "Nether Explorer"
  description: "Explore the most blocks in the Nether!"
  type: EXPLORE_BLOCKS
  target: ANY
  duration: 300
  rewards:
    1st:
      DIAMOND: 4
      GOLD_INGOT: 8
      ENDER_PEARL: 16
      ENCHANTED_BOOK:
        enchantments:
          PROTECTION: 4
    2nd:
      DIAMOND: 2
      GOLD_INGOT: 4
      ENDER_PEARL: 8
      ENCHANTED_BOOK:
        enchantments:
          PROTECTION: 3
    3rd:
      DIAMOND: 1
      GOLD_INGOT: 2
      ENDER_PEARL: 4
```

## Default Challenges

QuestLogs includes 10 balanced default challenges:

1. **Stone Mining Sprint** - Mine stone (easy)
2. **Lumberjack Challenge** - Break logs (easy)
3. **Zombie Hunt** - Kill zombies (easy)
4. **Coal Rush** - Mine coal ore (medium)
5. **Skeleton Sniper** - Kill skeletons (medium)
6. **Iron Seeker** - Mine iron ore (medium)
7. **Diamond Dash** - Mine diamond ore (hard)
8. **Nether Explorer** - Explore nether (hard)
9. **Speed Builder** - Place cobblestone (creative)
10. **Crafting Master** - Craft sticks (creative)

## Admin Commands

### Start a Challenge
```
/challengeadmin start <challenge_id>
```

Example:
```
/challengeadmin start diamond_dash
```

### Stop Active Challenge
```
/challengeadmin stop
```

### List All Challenges
```
/challengeadmin list
```

### Reload Challenge Config
```
/challengeadmin reload
```

## Player Commands

### View Active Challenge
```
/challenge
```

Shows:
- Challenge name and description
- Time remaining
- Current leaderboard
- Your progress

## Statistics Tracking

The plugin tracks:
- **Challenges Won**: How many times you placed 1st
- **Challenges Participated**: How many challenges you joined

View with `/queststats`.

## Reward Balancing

### Easy Challenges (Common Resources)
- Iron ingots, coal, arrows
- Low-tier enchanted books (Efficiency I, Sharpness I)
- 0-1 diamonds for winner only

### Medium Challenges (Moderate Resources)
- More iron, some gold
- 1-3 diamonds for top players
- Mid-tier books (Efficiency III, Power II-III, Unbreaking III)

### Hard Challenges (Premium Resources)
- 1-5 diamonds
- Emeralds, ender pearls
- High-tier books (Fortune III, Silk Touch, Protection IV, Mending)

## Tips

### For Server Owners

1. **Start with longer intervals** (1-2 hours) to avoid spam
2. **Test challenges manually** before enabling auto-start
3. **Balance rewards** based on difficulty and frequency
4. **Consider peak hours** for challenge timing
5. **Monitor participation** via statistics

### For Creating Challenges

1. **Make them achievable** - 50-100 for mining, 10-25 for mobs
2. **Use varied types** - Mix mining, combat, building
3. **Scale rewards** - 1st place gets best, 3rd gets decent
4. **Theme challenges** - "Nether Week", "Mining Monday"
5. **Test duration** - 5 minutes is usually good

### For Players

1. **Check `/challenge` regularly** to see what's active
2. **Prepare in advance** - Have tools ready
3. **Focus on one challenge** - Don't spread yourself thin
4. **Watch the leaderboard** - Know your competition
5. **Participation counts** - Even 3rd place gets rewards

## Troubleshooting

### Challenges Won't Start

- Check `enabled: true` in settings
- Verify `minimum-players` is met
- Check server console for errors

### Rewards Not Given

- Verify reward syntax in `challenges.yml`
- Check player inventory isn't full (items drop on ground)
- Review server console for errors

### Progress Not Tracking

- Make sure the challenge type matches the activity
- Check if player has completed it before
- Verify target material name is correct

## Examples

### Mining Event Day

Set up multiple mining challenges:

```yaml
settings:
  enabled: true
  frequency: 1800  # Every 30 minutes
  duration: 600    # 10 minutes each

challenge-pool:
  coal_event:
    name: "Coal Rush Event"
    type: MINE_BLOCKS
    target: COAL_ORE
    # ... rewards
  
  iron_event:
    name: "Iron Rush Event"
    type: MINE_BLOCKS
    target: IRON_ORE
    # ... rewards
  
  diamond_event:
    name: "Diamond Rush Event"
    type: MINE_BLOCKS
    target: DIAMOND_ORE
    # ... rewards
```

### Combat Weekend

Focus on mob hunting:

```yaml
challenge-pool:
  zombie_weekend:
    name: "Zombie Apocalypse"
    type: KILL_MOBS
    target: ZOMBIE
    # ... rewards
  
  skeleton_weekend:
    name: "Skeleton Army"
    type: KILL_MOBS
    target: SKELETON
    # ... rewards
```

## Next Steps

- [Reward System](Reward-System) - Configure enchantments and items
- [Player Statistics](Player-Statistics) - View tracked data
- [Commands](Commands-&-Permissions) - Manage challenges

