package com.gelakinetic.CubeStats;

public class MagicConstants {
    public static final String WHITE = "White";
    public static final String BLUE = "Blue";
    public static final String BLACK = "Black";
    public static final String RED = "Red";
    public static final String GREEN = "Green";
    public static final String COLORLESS = "Colorless";
    public static final String GOLD = "Gold";

    public static final String CREATURE = "Creature";
    public static final String ARTIFACT = "Artifact";
    public static final String ENCHANTMENT = "Enchantment";
    public static final String INSTANT = "Instant";
    public static final String SORCERY = "Sorcery";
    public static final String PLANESWALKER = "Planeswalker";
    public static final String LAND = "Land";

    /**
     * All the types. They are in distinct buckets (i.e. Artifact Lands don't count
     * as Artifacts. The search is ordered, so first it searches for all lands, then
     * creatures NOT lands, then artifacts NOT creatures NOT lands, etc
     */
    public static final String[] TYPES = {CREATURE, ARTIFACT, ENCHANTMENT, INSTANT, SORCERY, PLANESWALKER, LAND};
    public static final String[] SINGLE_COLOR_KEYS = {WHITE, BLUE, BLACK, RED, GREEN};

}
