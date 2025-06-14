package net.runelite.client.plugins.microbot.superheatitem;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("superheat-item")
public interface SuperheatItemConfig extends Config {
    @ConfigItem(
            keyName = "Guide",
            name = "Guide",
            description = "how to set up this plugin",
            position = 0
    )
    default String GUIDE() {
        return "Superheat item worth it from steel smithing\n"
                + "Please make sure you have:\n"
                + "1. Staff of fire\n"
                + "2. Enough Nature rune\n"
                + "3. Enough ore, and if applicable, enough coal";
    }

    @ConfigItem(
            keyName = "Bar",
            name = "Bar",
            description = "Choose the bar that you want to create with super heating:",
            position = 1
    )
    default Bars BAR()
    {
        return Bars.BRONZE_BAR;
    }
}
