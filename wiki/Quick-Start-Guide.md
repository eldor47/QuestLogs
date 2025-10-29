# Quick Start Guide

Get QuestLogs running on your server in 5 minutes!

## 1. Install (2 minutes)

```bash
# Stop your server
stop

# Copy the JAR to plugins folder
# (Download from GitHub Releases first)
cp QuestLogs-1.0.0.jar plugins/

# Start your server
start
```

## 2. Give Permissions (1 minute)

Using LuckPerms:
```
/lp group default permission set questlogs.use true
/lp group admin permission set questlogs.admin true
```

Or add to your permissions plugin's config:
```yaml
default:
  permissions:
    - questlogs.use

admin:
  permissions:
    - questlogs.admin
    - questlogs.stats.others
```

## 3. Test It (2 minutes)

### For Players:
```
/questbook
```
Opens the quest GUI with example quests.

### Walk Around:
- Move around to trigger exploration quests
- Mine some blocks to trigger mining quests
- Check progress in `/questbook`

### View Stats:
```
/queststats
```
Shows your statistics.

### Try a Challenge:
```
/challengeadmin start stone_sprint
```
Starts a 5-minute mining challenge.

## 4. Customize (Optional)

### Edit Default Quests

Open `plugins/QuestLogs/quests.yml` and modify:

```yaml
my_first_quest:
  name: "Welcome Explorer"
  description: "Explore 1000 blocks"
  type: EXPLORE_BLOCKS
  target: ANY
  required_amount: 1000
  rewards:
    DIAMOND: 3
    IRON_INGOT: 10
  xp_reward: 100
```

### Reload:
```
/questadmin reload
```

## That's It!

You now have:
- ✓ Working quest system
- ✓ Player statistics tracking
- ✓ Timed challenges
- ✓ Example quests

## Next Steps

**Learn More:**
- [Quest Types](Quest-Types) - Create different quest types
- [Timed Challenges](Timed-Challenges) - Configure server challenges
- [Reward System](Reward-System) - Set up enchantments and XP

**Get Examples:**
- Check the `examples/` folder on GitHub
- 12+ pre-made quest configurations
- Copy and customize for your server

**Join the Community:**
- Report bugs on [GitHub Issues](https://github.com/YOUR-USERNAME/QuestLogs/issues)
- Ask questions in [Discussions](https://github.com/YOUR-USERNAME/QuestLogs/discussions)

## Common First-Time Tasks

### Change Notification Frequency

In `config.yml`:
```yaml
settings:
  notification-interval: 100  # Notify every 100 blocks/items/kills
```

### Disable Challenges

In `challenges.yml`:
```yaml
settings:
  enabled: false
```

### Reset Player Progress

Delete or rename `progress.yml`, then reload:
```
/questadmin reload
```

### Reset Statistics

Delete or rename `stats.db`, then restart the server.

## Need Help?

- **Commands**: See [Commands & Permissions](Commands-&-Permissions)
- **Configuration**: See [Configuration Files](Configuration-Files)
- **Problems**: See [FAQ](FAQ)

