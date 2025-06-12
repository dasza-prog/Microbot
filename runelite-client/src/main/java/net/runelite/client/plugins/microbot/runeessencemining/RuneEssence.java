package net.runelite.client.plugins.microbot.runeessencemining;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;

@Getter
@RequiredArgsConstructor
public enum RuneEssence
{
    RUNE_ESSENCE("Rune Essence", ObjectID.RUNE_ESSENCE_34773, new WorldPoint(2932,4816,0), ItemID.RUNE_ESSENCE),
    AUBURY("Aubury", NpcID.AUBURY_11435, new WorldPoint(3253,3401,0), ObjectID.PORTAL);

    private final String name;
    private final int id;
    private final WorldPoint worldPoint;
    private final int itemID;

}
