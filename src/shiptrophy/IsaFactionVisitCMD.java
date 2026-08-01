package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/** One-time faction station vignettes shown after Isa joins the fleet. */
public class IsaFactionVisitCMD implements CommandPlugin {
    private static final String CURRENT_FACTION = "$shipTrophyIsaFactionVisitId";
    private static final String KOL_MOD_ID = "knights_of_ludd";
    private static final String KOL_FACTION_ID = "knights_of_selkie";
    private static final String PAGSM_MOD_ID = "PAGSM";
    private static final String PIRATE_VISIT_REVISION_ID = "pirates_v2";
    private static final String PLAYER_TITLE = "$shipTrophyIsaPlayerTitle";
    private static final String TRITACHYON_HIGHLIGHT =
            "$shipTrophyIsaTriTachyonHighlight";
    private static final String HEGEMONY_HIGHLIGHT =
            "$shipTrophyIsaHegemonyHighlight";
    private static final String CHURCH_DONATION =
            "$shipTrophyIsaFactionVisitChurchDonation";
    private static final String HEGEMONY_REWARD =
            "$shipTrophyIsaFactionVisitHegemonyReward";
    private static final String LEAGUE_REBUKE =
            "$shipTrophyIsaFactionVisitLeagueRebuke";
    private static final String LEAGUE_PURCHASE =
            "$shipTrophyIsaFactionVisitLeaguePurchase";
    private static final String TRITACHYON_REWARD =
            "$shipTrophyIsaFactionVisitTriTachyonReward";

    private static final String HEGEMONY_SENSOR_MOD =
            "ship_trophy_isa_hegemony_observation_blister";
    private static final String TRITACHYON_SUPPLY_MOD =
            "ship_trophy_isa_tritachyon_maintenance_manual";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;

        String command = value(params, 0, memoryMap);
        String requestedFaction = canonicalFaction(value(params, 1, memoryMap));

        if ("shouldShow".equals(command)) {
            return shouldShow(dialog, requestedFaction);
        }
        if ("shouldShowDiktatVanilla".equals(command)) {
            return !isModEnabled(PAGSM_MOD_ID)
                    && shouldShow(dialog, Factions.DIKTAT);
        }
        if ("shouldShowDiktatPAGSM".equals(command)) {
            return isModEnabled(PAGSM_MOD_ID)
                    && shouldShow(dialog, Factions.DIKTAT);
        }

        MemoryAPI local = memoryMap == null ? null : memoryMap.get(MemKeys.LOCAL);
        if (local == null) return false;

        if ("prepare".equals(command)) {
            if (!shouldShow(dialog, requestedFaction)) return false;
            local.set(CURRENT_FACTION, requestedFaction, 0f);
            PersonAPI player = Global.getSector() == null
                    ? null
                    : Global.getSector().getPlayerPerson();
            String title = player == null ? null : player.getRank();
            local.set(
                    PLAYER_TITLE,
                    title == null || title.length() <= 0 ? "Commander" : title,
                    0f);
            if (Factions.TRITACHYON.equals(requestedFaction)
                    && Global.getSector() != null
                    && Global.getSector().getFaction(Factions.TRITACHYON) != null) {
                local.set(
                        TRITACHYON_HIGHLIGHT,
                        Global.getSector().getFaction(Factions.TRITACHYON)
                                .getBaseUIColor(),
                        0f);
            }
            if (Global.getSector() != null
                    && Global.getSector().getFaction(Factions.HEGEMONY) != null) {
                local.set(
                        HEGEMONY_HIGHLIGHT,
                        Global.getSector().getFaction(Factions.HEGEMONY)
                                .getBaseUIColor(),
                        0f);
            }
            showIsa(dialog);
            return true;
        }
        if ("markSeen".equals(command)) {
            String factionId = canonicalFaction(local.getString(CURRENT_FACTION));
            if (isSupportedFaction(factionId)) {
                IsaTrophyManager.setFactionVisitSceneShown(
                        visitStateId(factionId));
            }
            return true;
        }
        if ("showTriTachyonAd".equals(command)) {
            showTriTachyonAd(dialog);
            return true;
        }
        if ("showTriOSInterruption".equals(command)) {
            showTriOSInterruption(dialog);
            return true;
        }
        if ("grantHegemonyReward".equals(command)) {
            grantHegemonyReward();
            return true;
        }
        if ("grantLeagueRebuke".equals(command)) {
            grantLeagueRebuke();
            return true;
        }
        if ("grantLeaguePurchase".equals(command)) {
            grantLeaguePurchase();
            return true;
        }
        if ("grantTriTachyonReward".equals(command)) {
            grantTriTachyonReward();
            return true;
        }
        if ("grantChurchDonation".equals(command)) {
            grantChurchDonation();
            return true;
        }
        return false;
    }

    private static void grantChurchDonation() {
        MemoryAPI memory = getSectorMemory();
        if (memory == null || memory.getBoolean(CHURCH_DONATION)) return;
        memory.set(CHURCH_DONATION, true);

        CampaignFleetAPI fleet = Global.getSector() == null
                ? null : Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getCargo() == null) return;
        float credits = fleet.getCargo().getCredits().get();
        fleet.getCargo().getCredits().subtract(Math.min(1000f, credits));
    }

    /** Renders the targeted advertisement as an in-world corporate insert. */
    private static void showTriTachyonAd(InteractionDialogAPI dialog) {
        if (dialog == null || dialog.getTextPanel() == null) return;
        TextPanelAPI text = dialog.getTextPanel();
        java.awt.Color corporate = new java.awt.Color(90, 175, 255);
        if (Global.getSector() != null
                && Global.getSector().getFaction(Factions.TRITACHYON) != null) {
            corporate = Global.getSector().getFaction(Factions.TRITACHYON)
                    .getBaseUIColor();
        }

        text.setFontInsignia();
        text.addPara("Before Isa can answer, her slate vibrates again.\n\n"
                + "This time, the advertisement addresses her by name.");

        text.setFontOrbitron();
        text.addPara("ISAAC LEICESTER", corporate);
        text.setFontSmallInsignia();
        text.addPara("CUSTOMA SHIP ARCHITECTURES // PERSONNEL DISCOUNT",
                Misc.getGrayColor());

        String assessment = "Based on her recent technical searches, "
                + "professional history, fleet composition, estimated "
                + "liquidity, and observed pupil response during the "
                + "demonstration, Tri-Tachyon predicts an eighty-seven "
                + "percent likelihood that Isa would benefit from immediate "
                + "Afflictor ownership.";
        LabelAPI copy = text.addPara(assessment, Misc.getGrayColor());
        copy.setHighlight("eighty-seven percent likelihood",
                "immediate Afflictor ownership");
        copy.setHighlightColors(corporate, corporate);
        text.addPara("FINANCING STATUS: PRE-APPROVED",
                Misc.getPositiveHighlightColor());
        text.setFontInsignia();
    }

    /** Separates the showroom AI's speech from the surrounding narration. */
    private static void showTriOSInterruption(InteractionDialogAPI dialog) {
        if (dialog == null || dialog.getTextPanel() == null) return;
        TextPanelAPI text = dialog.getTextPanel();
        java.awt.Color corporate = new java.awt.Color(90, 175, 255);
        if (Global.getSector() != null
                && Global.getSector().getFaction(Factions.TRITACHYON) != null) {
            corporate = Global.getSector().getFaction(Factions.TRITACHYON)
                    .getBaseUIColor();
        }

        text.setFontInsignia();
        text.addPara("Isa hastily wipes the advert from her slate.\n\n"
                + "\"I didn't give them any of that information.\"\n\n"
                + "The showroom voice answers without being asked, speaking "
                + "in the polished cadence of a TriOS delta-level AI.");
        text.setFontOrbitron();
        text.addPara("TRIOS // MARKETING SERVICES", corporate);
        text.setFontSmallInsignia();
        text.addPara("\"You didn't need to, Ms. Leicester. Tri-Tachyon "
                + "Marketing Services is the finest in the Domain. We "
                + "have--\"", corporate);
        text.setFontInsignia();
        text.addPara("Isa cuts it off with a wave, scowling at the speakers "
                + "overhead.");
    }

    private static void grantHegemonyReward() {
        MemoryAPI memory = getSectorMemory();
        if (memory == null || memory.getBoolean(HEGEMONY_REWARD)) return;
        memory.set(HEGEMONY_REWARD, true);
        applyPersistentBonuses();
    }

    private static void grantLeagueRebuke() {
        MemoryAPI memory = getSectorMemory();
        if (memory == null || memory.getBoolean(LEAGUE_REBUKE)) return;
        memory.set(LEAGUE_REBUKE, true);

        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(IsaTrophyManager.findHomeMarket());
        if (isa != null && isa.getRelToPlayer() != null) {
            isa.getRelToPlayer().setRel(
                    Math.max(-1f, isa.getRelToPlayer().getRel() - 0.05f));
        }
    }

    private static void grantLeaguePurchase() {
        MemoryAPI memory = getSectorMemory();
        if (memory == null || memory.getBoolean(LEAGUE_PURCHASE)) return;
        memory.set(LEAGUE_PURCHASE, true);

        CampaignFleetAPI fleet = Global.getSector() == null
                ? null
                : Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getCargo() == null) return;
        float credits = fleet.getCargo().getCredits().get();
        fleet.getCargo().getCredits().subtract(Math.min(2000f, credits));
        fleet.getCargo().addFighters("thunder_wing", 1);
    }

    private static void grantTriTachyonReward() {
        MemoryAPI memory = getSectorMemory();
        if (memory == null || memory.getBoolean(TRITACHYON_REWARD)) return;
        memory.set(TRITACHYON_REWARD, true);
        applyPersistentBonuses();
    }

    public static void applyPersistentBonuses() {
        MemoryAPI memory = getSectorMemory();
        CampaignFleetAPI fleet = Global.getSector() == null
                ? null
                : Global.getSector().getPlayerFleet();
        if (memory == null || fleet == null) return;

        if (memory.getBoolean(HEGEMONY_REWARD)) {
            fleet.getStats().getSensorProfileMod().modifyPercent(
                    HEGEMONY_SENSOR_MOD,
                    -1f,
                    "Isa: concealed observation blister");
        }

        if (memory.getBoolean(TRITACHYON_REWARD)
                && fleet.getFleetData() != null) {
            for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
                if (member == null || member.getStats() == null) continue;
                member.getStats().getSuppliesPerMonth().modifyPercent(
                        TRITACHYON_SUPPLY_MOD,
                        -1f,
                        "Isa: phase-coil maintenance manual");
            }
        }
    }

    private static MemoryAPI getSectorMemory() {
        return Global.getSector() == null
                ? null
                : Global.getSector().getMemoryWithoutUpdate();
    }

    private static boolean shouldShow(InteractionDialogAPI dialog, String requestedFaction) {
        if (!IsaTrophyManager.wasOfficerGranted() || !isSupportedFaction(requestedFaction)) return false;

        SectorEntityToken target = dialog.getInteractionTarget();
        if (target == null || !target.hasTag(Tags.STATION)) return false;

        MarketAPI market = target.getMarket();
        if (market == null || market.isPlayerOwned()) return false;
        if (IsaHomecomingCMD.isShatteredRing(target, market)) return false;

        String actualFaction = canonicalFaction(market.getFactionId());
        return requestedFaction.equals(actualFaction)
                && !IsaTrophyManager.wasFactionVisitSceneShown(
                        visitStateId(actualFaction));
    }

    private static String visitStateId(String factionId) {
        return Factions.PIRATES.equals(factionId)
                ? PIRATE_VISIT_REVISION_ID
                : factionId;
    }

    private static void showIsa(InteractionDialogAPI dialog) {
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(home);
        if (isa != null) dialog.getVisualPanel().showPersonInfo(isa);
    }

    private static String canonicalFaction(String factionId) {
        return factionId == null ? "" : factionId;
    }

    private static boolean isSupportedFaction(String factionId) {
        if (Factions.HEGEMONY.equals(factionId)
                || Factions.PERSEAN.equals(factionId)
                || Factions.TRITACHYON.equals(factionId)
                || Factions.DIKTAT.equals(factionId)
                || Factions.LUDDIC_CHURCH.equals(factionId)
                || Factions.LUDDIC_PATH.equals(factionId)
                || Factions.PIRATES.equals(factionId)) {
            return true;
        }
        if (KOL_FACTION_ID.equals(factionId)) {
            return isOptionalFactionAvailable(KOL_MOD_ID, KOL_FACTION_ID);
        }
        return false;
    }

    private static boolean isOptionalFactionAvailable(String modId, String factionId) {
        try {
            return Global.getSettings() != null
                    && Global.getSettings().getModManager() != null
                    && Global.getSettings().getModManager().isModEnabled(modId)
                    && Global.getSector() != null
                    && Global.getSector().getFaction(factionId) != null;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isModEnabled(String modId) {
        try {
            return Global.getSettings() != null
                    && Global.getSettings().getModManager() != null
                    && Global.getSettings().getModManager().isModEnabled(modId);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static String value(List<Token> params, int index, Map<String, MemoryAPI> memoryMap) {
        if (index < 0 || index >= params.size()) return "";
        String result = params.get(index).getString(memoryMap);
        return result == null ? "" : result;
    }

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap) {
        return 0;
    }
}
