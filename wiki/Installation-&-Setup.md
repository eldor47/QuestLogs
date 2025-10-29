# Installation & Setup

This guide walks you through installing and setting up QuestLogs on your server.

## Installation

### Step 1: Download

Download the latest `QuestLogs-X.X.X.jar` file from:
- [GitHub Releases](https://github.com/YOUR-USERNAME/QuestLogs/releases)
- [Modrinth](https://modrinth.com/plugin/questlogs)

### Step 2: Install

1. Stop your server
2. Place `QuestLogs-X.X.X.jar` in your server's `plugins/` folder
3. Start your server

The plugin will create the following folder structure:

```
plugins/
└── QuestLogs/
    ├── config.yml          # Main configuration
    ├── quests.yml          # Quest definitions (auto-generated)
    ├── challenges.yml      # Challenge pool (auto-generated)
    ├── progress.yml        # Player progress (auto-generated)
    └── stats.db            # SQLite database (auto-generated)
```

### Step 3: Verify Installation

Run this command in your server console or as an admin:

```
/questadmin reload
```

If you see `Configuration reloaded successfully`, the plugin is working!

## Initial Configuration

### 1. Set Up Permissions

Add these permissions to your permission plugin (LuckPerms, PermissionsEx, etc.):

**For all players:**
```yaml
permissions:
  - questlogs.use
```

**For moderators/admins:**
```yaml
permissions:
  - questlogs.use
  - questlogs.admin
  - questlogs.stats.others
```

See [Commands & Permissions](Commands-&-Permissions) for the full list.

### 2. Configure Settings

Edit `plugins/QuestLogs/config.yml`:

```yaml
settings:
  # How often to notify players about quest progress (in blocks/items/kills)
  notification-interval: 500
  
  # Whether to enable the quest prerequisite system
  prerequisites-enabled: true
  
  # Database settings (usually don't need to change)
  database:
    type: sqlite
    file: stats.db
```

### 3. Set Up Your First Quests

The plugin generates example quests on first run. You can:

**Option A: Use the examples**
- The default `quests.yml` includes working example quests
- Test them with `/questbook`

**Option B: Customize quests**
- Edit `plugins/QuestLogs/quests.yml`
- See [Quest Types](Quest-Types) for quest configuration
- See [Examples](Examples) for more quest templates

**Option C: Use an example file**
- Copy a file from the `examples/` folder in the GitHub repository
- Replace `quests.yml` with your chosen example
- Run `/questadmin reload`

### 4. Configure Challenges (Optional)

Edit `plugins/QuestLogs/challenges.yml`:

```yaml
settings:
  enabled: true
  frequency: 3600      # How often to start challenges (seconds)
  duration: 300        # How long each challenge lasts (seconds)
  minimum-players: 2   # Don't start if fewer players online
```

See [Timed Challenges](Timed-Challenges) for detailed configuration.

### 5. Reload Configuration

After making changes, reload without restarting:

```
/questadmin reload
```

## Testing

### Test as a Player

1. Join your server as a regular player
2. Run `/questbook` to open the quest GUI
3. Complete some quest objectives
4. Check your stats with `/queststats`

### Test Challenges

1. Manually start a challenge: `/challengeadmin start <challenge_id>`
2. Participate and check the leaderboard: `/challenge`
3. Wait for it to end and see rewards distributed

### Test Admin Commands

1. View a player's progress: `/quest progress <player>`
2. Complete a quest for testing: `/questadmin complete <quest_id> <player>`
3. List all challenges: `/challengeadmin list`

## Troubleshooting

### Plugin Won't Load

- Check your server is running Spigot or Paper 1.21+
- Verify Java 21+ is installed: `java -version`
- Check server console for error messages

### Commands Don't Work

- Verify permissions are set correctly
- Try `/questadmin reload`
- Check for permission plugin conflicts

### Quests Not Tracking

- Make sure the quest is not already completed
- Check if prerequisites are blocking progress
- Verify the quest type matches the activity (e.g., `MINE_BLOCKS` for mining)

### Database Errors

- The plugin will recreate `stats.db` if it's missing
- Back up `stats.db` before making changes
- Check file permissions on the QuestLogs folder

## Next Steps

- [Configure your quests](Quest-Types)
- [Set up challenges](Timed-Challenges)
- [Learn about rewards](Reward-System)
- [Read the FAQ](FAQ)

