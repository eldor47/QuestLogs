package com.questlogs.models;

public enum QuestType {
    EXPLORE_BLOCKS("Explore blocks"),
    MINE_BLOCKS("Mine specific blocks"),
    KILL_MOBS("Kill mobs"),
    BREAK_BLOCKS("Break blocks"),
    PLACE_BLOCKS("Place blocks"),
    CRAFT_ITEMS("Craft items");
    
    private final String displayName;
    
    QuestType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static QuestType fromString(String type) {
        for (QuestType questType : values()) {
            if (questType.name().equalsIgnoreCase(type)) {
                return questType;
            }
        }
        return null;
    }
}




