package net.runelite.client.plugins.microbot.superheatitem;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.item.Rs2ItemManager;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Collections;
import java.util.List;
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
                            Microbot.showMessage("Missing Staff of fire");
                            initialise = false;
                            shutdown();
                            return;
                        }
                    }

                    if (!Rs2Bank.isOpen()) {
                        Rs2Bank.openBank();
                        Rs2Random.wait(800, 1600);
                    }

                    if(!Rs2Inventory.hasItem("Nature rune")) {
                        if (Rs2Bank.hasItem("Nature rune")) {
                            Rs2Bank.withdrawAll("Nature rune");
                            Rs2Random.wait(800, 1600);
                        } else {
                            Microbot.showMessage("Missing Nature rune.");
                            initialise = false;
                            shutdown();
                            return;
                        }
                    }

                    if (Rs2Bank.depositAllExcept("Nature rune")) {
                        Rs2Random.wait(800, 1600);
                    }

                    if (bar.getSecondaryComponentName() == null) {
                        if (Rs2Bank.hasItem(bar.getMainComponentId())) {
                            if(Rs2Bank.withdrawAll(bar.getMainComponentId())) {
                                Rs2Random.wait(800, 1600);
                            }
                        } else {
                            Microbot.showMessage("Ran out of " + bar.getMainComponentName());
                            initialise = false;
                            shutdown();
                            return;
                        }
                    } else {
                        double maximumMainComponentQuantity = Math.floor((double) Rs2Inventory.getEmptySlots() / (bar.getMainComponentRequiredAmount() + bar.getSecondaryComponentRequiredAmount()));
                        double maximumSecondaryComponentQuantity = maximumMainComponentQuantity * bar.getSecondaryComponentRequiredAmount();

                        if (Rs2Bank.hasItem(bar.getMainComponentId()) && Rs2Bank.hasItem(bar.getSecondaryComponentId())) {
                            if(Rs2Bank.withdrawX(bar.getMainComponentId(), (int) maximumMainComponentQuantity)) {
                                Rs2Random.wait(800, 1600);
                            }
                            int withdrawSecondaryComponentQuantity = Rs2Inventory.itemQuantity(bar.getMainComponentId()) * bar.getSecondaryComponentRequiredAmount();

                            if (Rs2Bank.hasItem(new int[]{bar.getSecondaryComponentId()}, withdrawSecondaryComponentQuantity)) {
                                if (Rs2Bank.withdrawX(bar.getSecondaryComponentId(), withdrawSecondaryComponentQuantity)) {
                                    Rs2Random.wait(800, 1600);
                                }
                            } else {
                                Microbot.showMessage("Not enough " + bar.getSecondaryComponentName());
                                initialise = false;
                                shutdown();
                                return;
                            }

                        } else {
                            Microbot.showMessage("Ran out of " + bar.getMainComponentName() + " or ran out of " + bar.getSecondaryComponentName());
                            initialise = false;
                            shutdown();
                            return;
                        }
                    }

                    while (Rs2Inventory.hasItemAmount(bar.getMainComponentId(), bar.getMainComponentRequiredAmount()) && Rs2Inventory.hasItemAmount(bar.getSecondaryComponentId(), bar.getSecondaryComponentRequiredAmount())) {
                        Rs2Magic.superHeat(bar.getMainComponentId());
                        sleepUntilTick(3);
                    }
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