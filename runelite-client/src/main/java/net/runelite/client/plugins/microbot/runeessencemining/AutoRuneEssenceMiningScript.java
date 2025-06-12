package net.runelite.client.plugins.microbot.runeessencemining;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.mouse.naturalmouse.util.Pair;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class AutoRuneEssenceMiningScript extends Script
{


    private States state;
    private RuneEssence runeEssence;
    private boolean initialise;
    public static int runsCompleted = 0;
    public static int initialRunecraftLevel;
    public static int runecraftLevel;
    public static int initialRunecraftXp;
    public static int runecraftXp;

    public static boolean clickFirstMatchingMenuEntry(String desiredOption, String desiredTarget) {
        MenuEntry[] menuEntries = Microbot.getClient().getMenuEntries();

        for (MenuEntry entry : menuEntries) {
            String option = entry.getOption().toLowerCase();
            String target = entry.getTarget().replaceAll("<.*?>", "").toLowerCase(); // Remove <col> etc.

            boolean optionMatch = option.contains(desiredOption.toLowerCase());
            boolean targetMatch = target.contains(desiredTarget.toLowerCase());

            boolean typeMatch = entry.getType() == MenuAction.GAME_OBJECT_FIRST_OPTION
                    || entry.getType() == MenuAction.NPC_FIRST_OPTION;

            if (optionMatch && targetMatch && typeMatch) {
                // Inject only this entry and click it
                Microbot.getClient().setMenuEntries(new MenuEntry[]{entry});
                Microbot.getMouse().click();
                Microbot.log("Clicked: " + entry.getOption() + " -> " + target);
                return true;
            }
        }
        Microbot.log("No matching MenuEntry found for: " + desiredOption + " " + desiredTarget);
        return false;
    }

    public boolean run(AutoRuneEssenceMiningConfig config)
    {
        Microbot.enableAutoRunOn = true;
        Rs2Antiban.resetAntibanSettings();
        Rs2Antiban.antibanSetupTemplates.applyMiningSetup();
        Rs2Antiban.setActivity(Activity.GENERAL_MINING);

        initialise = true;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() ->
        {
            try
            {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (Rs2AntibanSettings.actionCooldownActive) return;
                long startTime = System.currentTimeMillis();

                if (initialise)
                {
                    state = States.WALKING_TO_RUNE_ESSENCE;
                }

                if (Rs2Player.isMoving() || Rs2Player.isAnimating() || Microbot.pauseAllScripts) return;

                if (Rs2Player.isInteracting()) return;

                switch(state)
                {
                    case WALKING_TO_RUNE_ESSENCE:
                        initialise = false;
                        Microbot.status = "Walking to and mining nearest rune essence";

                        GameObject runeEssenceRock = Rs2GameObject.getGameObject(RuneEssence.RUNE_ESSENCE.getId());
                        if (runeEssenceRock == null || !Rs2Walker.canReach(runeEssenceRock.getWorldLocation())) {
                            Microbot.log("No near");
                            boolean walked = Rs2Walker.walkTo(RuneEssence.RUNE_ESSENCE.getWorldPoint(), 2);
                            if (!walked) return;
                            Rs2Random.wait(800, 1600);
                            state = States.RUNE_ESSENCE_MINING;
                        } else {
                            Microbot.log("Near");
                            boolean walked = Rs2Walker.walkTo(runeEssenceRock.getWorldLocation(), 2);
                            if (!walked) return;
                            Rs2Random.wait(800, 1600);
                            state = States.RUNE_ESSENCE_MINING;
                        }

                        break;

                    case RUNE_ESSENCE_MINING:
                        Microbot.status = "Minin Rune Essences";
                        if (Rs2Inventory.isFull()) {
                            state = States.TELEPORT_TO_AUBURY;
                            return;
                        }
                        if (Rs2GameObject.interact(RuneEssence.RUNE_ESSENCE.getId(),"Mine")) {
                            Rs2Player.waitForXpDrop(Skill.MINING, true);
                            Rs2Antiban.actionCooldown();
                            Rs2Antiban.takeMicroBreakByChance();
                        }
                        break;

                    case TELEPORT_TO_AUBURY:
                        Microbot.status = "Looking for portal back to Varrock";

                        Rs2NpcModel portalNpc = Rs2Npc.getNpcWithAction("Use");
                        String action = "Use";

                        if (portalNpc == null) {
                            portalNpc = Rs2Npc.getNpcWithAction("Exit");
                            action = "Exit";
                        }

                        int portalObjectId = -1;
                        String portalObjectAction = "";

                        if (portalNpc == null) {
                            GameObject portalObj = Rs2GameObject.getGameObject("Portal", false);
                            if (portalObj != null) {
                                portalObjectId = portalObj.getId();
                                if (Rs2GameObject.hasAction(Rs2GameObject.convertToObjectComposition(portalObjectId), "Use", false)) {
                                    portalObjectAction = "Use";
                                } else {
                                    portalObjectAction = "Exit";
                                }
                            }
                        }

                        boolean success = false;
                        if (portalNpc != null) {
                            success = Rs2Npc.interact(portalNpc, action);
                        } else if (portalObjectId != -1) {
                            success = Rs2GameObject.interact(portalObjectId, portalObjectAction);
                        }

                        if (success) {
                            Rs2Random.wait(800, 1600);
                            sleepUntil(() -> !Rs2Player.isMoving());
                            Rs2Random.wait(2000, 2400);
                            state = States.BANKING;
                        } else {
                            Microbot.log("No usable portal found.");
                        }

                        break;

                    case BANKING:
                        boolean isBankOpen = Rs2Bank.walkToBankAndUseBank();
                        Microbot.status = "Banking";
                        if (!isBankOpen || !Rs2Bank.isOpen()) return;

                        if (Rs2Inventory.hasItem(RuneEssence.RUNE_ESSENCE.getName(), false))
                        {
                            Microbot.status = "Depositing runes";
                            Rs2Bank.depositAll(RuneEssence.RUNE_ESSENCE.getName(), false);
                            Rs2Random.wait(800, 1600);
                        }

                        Rs2Random.wait(800, 1600);
                        state = States.TELEPORT_TO_RUNE_ESSENCE;
                        Rs2Bank.closeBank();
                        break;

                    case TELEPORT_TO_RUNE_ESSENCE:
                        initialise = false;
                        Microbot.status = "Walking to Aubury";
                        boolean walked = Rs2Walker.walkTo(RuneEssence.AUBURY.getWorldPoint(), 2);
                        if (!walked) return;
                        Rs2Random.wait(800, 1600);
                        if (Rs2Npc.interact(RuneEssence.AUBURY.getId(),"Teleport")) {
                            Rs2Random.wait(800, 1600);
                            sleepUntil(() -> !Rs2Player.isMoving());
                            Rs2Random.wait(2000, 2400);
                        }
                        state = States.WALKING_TO_RUNE_ESSENCE;
                        break;

                }

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex)
            {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        Rs2Antiban.resetAntibanSettings();
    }

    public void updateLevelXp()
    {
        runecraftLevel = Rs2Player.getRealSkillLevel(Skill.RUNECRAFT);
        runecraftXp = Microbot.getClient().getSkillExperience(Skill.RUNECRAFT);
    }
}
