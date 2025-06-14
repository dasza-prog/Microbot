package net.runelite.client.plugins.microbot.superheatitem;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Bars {
    BRONZE_BAR("Bronze bar", 2349, 1, "Tin ore", 438, 1, "Copper ore", 436, 1),
    BLURITE_BAR("Blurite bar", 9467, 13, "Blurite ore", 668 ,1, "NotRequired", 0, 0),
    IRON_BAR("Irone bar", 2351, 15, "Iron ore", 440, 1, "NotRequired", 0, 0),
    SILVER_BAR("Silver bar", 2355, 20, "Silver ore", 442, 1, "NotRequired", 0, 0),
    ELEMENTAL_METAL("Elemental metal", 2893, 20, "Elemental ore", 2892, 1, "Coal", 453, 4),
    STEEL_BAR("Steel bar", 2353, 30, "Iron ore", 440, 1, "Coal", 453, 2),
    GOLD_BAR("Gold bar", 2357, 40, "Gold ore", 444, 1, "NotRequired", 0, 0),
    LOVAKITE_BAR("Lovakite bar", 13354, 45, "Lovakite ore", 13356, 1, "Coal", 453, 2),
    MITHRIL_BAR("Mithril bar", 2359, 50, "Mithril ore", 447, 1, "Coal", 453, 4),
    ADAMANTITE_BAR("Adamantite bar", 2361, 70, "Adamantite ore", 449, 1, "Coal", 453, 6),
    RUNITE_BAR("Runite bar", 2363, 85, "Runite ore", 451, 1, "Coal", 453, 8);

    private final String barName;
    private final int barId;
    private final int requiredSmithingLevel;
    private final String mainComponentName;
    private final int mainComponentId;
    private final int mainComponentRequiredAmount;
    private final String secondaryComponentName;
    private final int secondaryComponentId;
    private final int secondaryComponentRequiredAmount;
}
