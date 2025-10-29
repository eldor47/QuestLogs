# Commands & Permissions

Complete reference for all QuestLogs commands and permissions.

## Player Commands

### `/questbook`
Opens the Quest Book GUI.

**Aliases:** `/qbook`, `/quests`  
**Permission:** `questlogs.use`  
**Usage:**
```
/questbook
```

---

### `/quest`
View quest information and progress.

**Permission:** `questlogs.use`  
**Subcommands:**

#### `/quest list`
List all available quests.
```
/quest list
```

#### `/quest info <quest_id>`
Show detailed information about a specific quest.
```
/quest info novice_miner
```

#### `/quest progress`
Show your current quest progress.
```
/quest progress
```

---

### `/queststats [player]`
View player statistics.

**Aliases:** `/qstats`, `/stats`  
**Permission:** `questlogs.use`  
**Permission (view others):** `questlogs.stats.others`  

**Usage:**
```
/queststats              # View your own stats
/queststats PlayerName   # View another player's stats (requires permission)
```

**Output:**
- Quest statistics (quests completed)
- Challenge statistics (won, participated)
- Playtime (current session + total)
- Exploration stats
- Mining breakdown (by ore type)
- Combat stats (mobs killed)
- Crafting stats
- Building stats

---

### `/challenge`
View active challenge information and leaderboard.

**Aliases:** `/ch`, `/chal`  
**Permission:** `questlogs.use`  

**Usage:**
```
/challenge
```

**Output:**
- Challenge name and description
- Time remaining
- Current leaderboard
- Your current progress

---

## Admin Commands

### `/questadmin`
Administrative quest management.

**Aliases:** `/questadm`, `/qadmin`  
**Permission:** `questlogs.admin`  
**Subcommands:**

#### `/questadmin reload`
Reload all configuration files without restarting.
```
/questadmin reload
```
Reloads:
- `config.yml`
- `quests.yml`
- `challenges.yml`
- Does NOT reset player progress

#### `/questadmin complete <quest> <player>`
Force complete a quest for a player.
```
/questadmin complete novice_miner Steve
```
- Marks the quest as completed
- Gives rewards to the player
- Updates statistics

#### `/questadmin reset <player>`
Reset all quest progress for a player.
```
/questadmin reset Steve
```
- Clears all quest progress
- Does NOT reset statistics
- Cannot be undone

#### `/questadmin list`
List all registered quests.
```
/questadmin list
```

---

### `/challengeadmin`
Administrative challenge management.

**Aliases:** `/chadmin`, `/challengea`  
**Permission:** `questlogs.admin`  
**Subcommands:**

#### `/challengeadmin start <challenge>`
Manually start a specific challenge.
```
/challengeadmin start diamond_dash
```
- Stops any active challenge
- Starts the specified challenge
- Broadcasts to all players

#### `/challengeadmin stop`
Stop the current active challenge.
```
/challengeadmin stop
```
- Ends the challenge immediately
- Does NOT give rewards
- Clears leaderboard

#### `/challengeadmin list`
List all available challenges.
```
/challengeadmin list
```
Shows challenge IDs from `challenges.yml`

#### `/challengeadmin reload`
Reload challenge configuration.
```
/challengeadmin reload
```
Reloads `challenges.yml` without restarting.

---

## Permissions

### Player Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `questlogs.use` | Access to all player commands | `true` |
| `questlogs.stats.others` | View other players' statistics | `op` |

### Admin Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `questlogs.admin` | Access to all admin commands | `op` |

### Permission Setup Examples

#### LuckPerms

**For all players:**
```
/lp group default permission set questlogs.use true
```

**For moderators:**
```
/lp group mod permission set questlogs.use true
/lp group mod permission set questlogs.stats.others true
```

**For admins:**
```
/lp group admin permission set questlogs.admin true
```

#### PermissionsEx

```yaml
groups:
  default:
    permissions:
      - questlogs.use
  
  moderator:
    permissions:
      - questlogs.use
      - questlogs.stats.others
  
  admin:
    permissions:
      - questlogs.admin
```

#### GroupManager

```yaml
groups:
  Default:
    permissions:
      - questlogs.use
  
  Moderator:
    permissions:
      - questlogs.stats.others
  
  Admin:
    permissions:
      - questlogs.admin
```

---

## Tab Completion

All commands have full tab completion support:

```
/quest <TAB>        → list, info, progress
/quest info <TAB>   → Shows all quest IDs
/questadmin <TAB>   → reload, complete, reset, list
/questadmin complete <TAB>   → Shows quest IDs
/questadmin complete novice_miner <TAB>   → Shows online players
/challengeadmin <TAB>   → start, stop, list, reload
/challengeadmin start <TAB>   → Shows all challenge IDs
```

---

## Command Examples

### For Players

**Opening the Quest Book:**
```
/questbook
```

**Checking Progress:**
```
/quest progress
```

**Viewing Stats:**
```
/queststats
```

**Checking Active Challenge:**
```
/challenge
```

### For Admins

**Reloading Config:**
```
/questadmin reload
```

**Completing Quest for Player:**
```
/questadmin complete novice_explorer Steve
/questadmin complete diamond_miner Alex
```

**Starting a Challenge:**
```
/challengeadmin start stone_sprint
/challengeadmin start zombie_hunter
```

**Viewing Challenge List:**
```
/challengeadmin list
```

**Resetting Player Progress:**
```
/questadmin reset Steve
```

---

## Common Use Cases

### Testing Quests

1. Create a test quest in `quests.yml`
2. Reload: `/questadmin reload`
3. Complete it for yourself: `/questadmin complete test_quest YourName`
4. Check the rewards were given

### Managing Player Progress

View a player's progress:
```
/quest progress <player>
```

Force complete a stuck quest:
```
/questadmin complete stuck_quest PlayerName
```

Reset a player who wants to start over:
```
/questadmin reset PlayerName
```

### Running Events

Start a challenge for an event:
```
/challengeadmin start diamond_dash
```

Stop it early if needed:
```
/challengeadmin stop
```

View who's winning:
```
/challenge
```

---

## Tips

1. **Use Tab Completion**: Press TAB to see available options
2. **Check IDs**: Use `/questadmin list` to see quest IDs before using them
3. **Test First**: Complete quests for yourself before giving to players
4. **Reload Often**: Use `/questadmin reload` after config changes
5. **Monitor Stats**: Check `/queststats <player>` to verify tracking works

## Next Steps

- [Quest Types](Quest-Types) - Learn about quest configuration
- [Configuration Files](Configuration-Files) - Understand config options
- [FAQ](FAQ) - Common questions and solutions

