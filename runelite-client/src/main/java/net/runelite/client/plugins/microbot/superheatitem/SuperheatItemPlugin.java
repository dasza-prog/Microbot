package net.runelite.client.plugins.microbot.superheatitem;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Default + "Superheat Item",
        description = "Microbot example plugin",
        tags = {"skilling", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class SuperheatItemPlugin extends Plugin {
    @Inject
    private SuperheatItemConfig config;
    @Provides
    SuperheatItemConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(SuperheatItemConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private SuperheatItemOverlay superheatItemOverlay;

    @Inject
    SuperheatItemScript superheatItemScript;


    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
            overlayManager.add(superheatItemOverlay);
        }
        superheatItemScript.run(config);
    }

    protected void shutDown() {
        superheatItemScript.shutdown();
        overlayManager.remove(superheatItemOverlay);
    }
    int ticks = 10;
    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));

        if (ticks > 0) {
            ticks--;
        } else {
            ticks = 10;
        }

    }

}
