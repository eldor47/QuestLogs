package com.questlogs.models;

/**
 * Types of challenges available
 */
public enum ChallengeType {
    MINE_BLOCKS("Mine the most"),
    BREAK_BLOCKS("Break the most"),
    PLACE_BLOCKS("Place the most"),
    CRAFT_ITEMS("Craft the most"),
    KILL_MOBS("Kill the most"),
    EXPLORE_BLOCKS("Explore the most");
    
    private final String displayPrefix;
    
    ChallengeType(String displayPrefix) {
        this.displayPrefix = displayPrefix;
    }
    
    public String getDisplayPrefix() {
        return displayPrefix;
    }
    
    public static ChallengeType fromString(String type) {
        for (ChallengeType challengeType : values()) {
            if (challengeType.name().equalsIgnoreCase(type)) {
                return challengeType;
            }
        }
        return null;
    }
}


