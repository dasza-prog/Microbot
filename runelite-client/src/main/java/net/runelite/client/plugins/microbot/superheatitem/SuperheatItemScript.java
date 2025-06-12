package net.runelite.client.plugins.microbot.superheatitem;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.concurrent.TimeUnit;

public class SuperheatItemScript extends Script {

    private Bars bar;
    private boolean initialise;

    public boolean run(SuperheatItemConfig config) {
        Microbot.enableAutoRunOn = true;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyGeneralBasicSetup();
        Rs2Antiban.setActivity(Activity.ALCHING);

        bar = config.BAR();

        initialise = true;

        if (!Rs2Player.getSkillRequirement(Skill.MAGIC, 43)) {
            Microbot.showMessage("Missing required magic skill level");
            initialise = false;
        }

        if (!Rs2Player.getSkillRequirement(Skill.SMITHING, bar.getRequiredSmithingLevel())) {
            Microbot.showMessage("Missing required smithing skill level");
            initialise = false;
        }

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (initialise) {
                    if (!Rs2Equipment.isWearing(1387)) {
                        if (Rs2Inventory.hasItem(1387)) {
                            if (Rs2Inventory.equip(1387)) {
                                Rs2Random.wait(800, 1600);
                            };
                        } else {
                            Microbot.log("Missing Staff of fire");
                            initialise = false;
                            return;
                        }
                    }
                }

                if (!Rs2Bank.isOpen()) {
                    Rs2Bank.openBank();
                    Rs2Random.wait(800, 1600);
                }

                if (Rs2Inventory.hasItem("Mithril bar")) {
                    Rs2Bank.depositAll("Mithril bar");
                    Rs2Random.wait(800, 1600);
                }

                // 1st go to the nearest bank and take out necessary ores
                if (Rs2Bank.hasItem("Coal") && Rs2Bank.hasItem("Mithril ore") || !Rs2Inventory.hasItem("Nature rune")) {
                    Rs2Bank.withdrawX("Mithril ore", 5);
                    Rs2Random.wait(800, 1600);
                    Rs2Bank.withdrawX("Coal", 20);
                    Rs2Random.wait(800, 1600);
                    Rs2Bank.closeBank();
                } else {
                    this.shutdown();
                }

                if (!Rs2Inventory.hasItemAmount("Coal", 20) || !Rs2Inventory.hasItemAmount("Mithril ore", 5)) {
                    Rs2Bank.openBank();
                    Rs2Random.wait(800, 1600);
                    Rs2Bank.depositAllExcept("Nature rune");
                    Rs2Random.wait(800, 1600);
                    Rs2Bank.closeBank();
                    return;
                }


                while (Rs2Inventory.hasItemAmount("Mithril ore", 1) && Rs2Inventory.hasItemAmount("Coal", 2)) {
                    Rs2Magic.superHeat("Mithril ore");
                    sleepUntilTick(3);

                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}