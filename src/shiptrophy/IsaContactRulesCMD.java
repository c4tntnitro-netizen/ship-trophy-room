package shiptrophy;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;
import com.fs.starfarer.api.campaign.rules.MemKeys;

import shiptrophy.hullmods.AbundantMercyVow;
import shiptrophy.hullmods.BlackLionInheritance;
import shiptrophy.hullmods.Contempt;
import shiptrophy.hullmods.Gaze;

/** Dynamic data and state actions used by Isa's rules.csv dialogue. */
public class IsaContactRulesCMD implements CommandPlugin {
    private static final String SUBTYPE_PREFIX = "ship_trophy_isa_subtype:";
    private static final String UNIQUE_PREFIX = "ship_trophy_isa_unique:";

    private static final String CURRENT_SUBTYPE = "$shipTrophyIsaSubtypeId";
    private static final String CURRENT_UNIQUE = "$shipTrophyIsaUniqueId";
    private static final String JOIN_RESULT = "$shipTrophyIsaJoinResult";
    private static final String PRISTINE_KITE_HULL_ID = "kite_original";

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;
        MemoryAPI local = memoryMap == null ? null : memoryMap.get(MemKeys.LOCAL);
        if (local == null) return false;

        String command = value(params, 0, memoryMap);

        if ("showBodyguards".equals(command)) {
            showBodyguards(dialog);
            return true;
        }
        if ("showContactPortrait".equals(command)) {
            showContactPortrait(dialog);
            return true;
        }
        if ("prepare".equals(command)) {
            prepare(dialog);
            return true;
        }
        if ("prepareMain".equals(command)) {
            prepareMain(dialog, local);
            return true;
        }
        if ("refreshStats".equals(command)) {
            refreshStats();
            return true;
        }
        if ("prepareMasterwork".equals(command)) {
            prepareMasterwork(dialog, local);
            return true;
        }
        if ("showMasterworkStatus".equals(command)) {
            showMasterworkStatus(dialog.getTextPanel());
            return true;
        }
        if ("showMasterworkIntro".equals(command)) {
            showMasterworkIntro(dialog.getTextPanel());
            return true;
        }
        if ("masterworkComplete".equals(command)) {
            return IsaTrophyManager.isMasterworkComplete();
        }
        if ("canJoin".equals(command)) {
            return !IsaTrophyManager.wasOfficerGranted()
                    && IsaTrophyManager.isAweRecruitmentReady();
        }
        if ("alreadyJoined".equals(command)) {
            return IsaTrophyManager.wasOfficerGranted();
        }
        if ("isaInFleet".equals(command)) {
            return IsaTrophyManager.isIsaOfficerInPlayerFleet();
        }
        if ("grantOfficer".equals(command)) {
            grantOfficer(local);
            return true;
        }
        if ("joinResult".equals(command)) {
            return value(params, 1, memoryMap).equals(local.getString(JOIN_RESULT));
        }
        if ("unlockSeen".equals(command)) {
            return IsaTrophyManager.wasUnlockDialogueSeen(resolveHullMod(value(params, 1, memoryMap)));
        }
        if ("markUnlockSeen".equals(command)) {
            IsaTrophyManager.setUnlockDialogueSeen(resolveHullMod(value(params, 1, memoryMap)));
            return true;
        }
        if ("hasUniquePrograms".equals(command)) {
            return hasUniquePrograms(isModded(value(params, 1, memoryMap)));
        }
        if ("uniqueActive".equals(command)) {
            return isUniqueActive(value(params, 1, memoryMap));
        }
        if ("uniqueShowcased".equals(command)) {
            return isUniqueShowcased(value(params, 1, memoryMap));
        }
        if ("showUniqueStatus".equals(command)) {
            showUniqueStatus(dialog.getTextPanel(), isModded(value(params, 1, memoryMap)));
            return true;
        }
        if ("pristineKiteStored".equals(command)) {
            return TrophyNetwork.hasStoredHull(PRISTINE_KITE_HULL_ID);
        }
        if ("pristineKiteSeen".equals(command)) {
            return Global.getSector() != null
                    && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                            ShipTrophyRoomIds
                                    .MEMORY_ISA_PRISTINE_KITE_DIALOGUE_SEEN);
        }
        if ("markPristineKiteSeen".equals(command)) {
            if (Global.getSector() != null) {
                Global.getSector().getMemoryWithoutUpdate().set(
                        ShipTrophyRoomIds
                                .MEMORY_ISA_PRISTINE_KITE_DIALOGUE_SEEN,
                        true);
            }
            return true;
        }
        if ("isGenericUniqueOption".equals(command)) {
            String option = local.getString("$option");
            return option != null && option.startsWith(UNIQUE_PREFIX);
        }
        if ("prepareUniqueFromOption".equals(command)) {
            prepare(dialog);
            String option = local.getString("$option");
            prepareUnique(local, option == null ? "" : option.substring(UNIQUE_PREFIX.length()));
            return true;
        }
        if ("currentUniqueActive".equals(command)) {
            return local.getBoolean("$shipTrophyIsaUniqueActive");
        }
        if ("currentUniqueShowcased".equals(command)) {
            return local.getBoolean("$shipTrophyIsaUniqueShowcased");
        }
        if ("currentUniqueModded".equals(command)) {
            return local.getBoolean("$shipTrophyIsaUniqueModded");
        }
        if ("markCurrentUniqueSeen".equals(command)) {
            String id = local.getString(CURRENT_UNIQUE);
            TrophyUniqueShowcases.ShowcaseSpec spec = findShowcase(id);
            if (spec != null) IsaTrophyManager.setUnlockDialogueSeen(spec.hullModId);
            return true;
        }
        if ("hasSubtypePrograms".equals(command)) {
            return hasSubtypePrograms(isModded(value(params, 1, memoryMap)));
        }
        if ("isSubtypeOption".equals(command)) {
            String option = local.getString("$option");
            return option != null && option.startsWith(SUBTYPE_PREFIX);
        }
        if ("prepareSubtypeFromOption".equals(command)) {
            prepare(dialog);
            String option = local.getString("$option");
            prepareSubtype(local, option == null ? "" : option.substring(SUBTYPE_PREFIX.length()));
            return true;
        }
        if ("prepareCurrentSubtype".equals(command)) {
            prepare(dialog);
            prepareSubtype(local, local.getString(CURRENT_SUBTYPE));
            return true;
        }
        if ("currentSubtype".equals(command)) {
            return value(params, 1, memoryMap).equals(local.getString(CURRENT_SUBTYPE));
        }
        if ("currentSubtypeActive".equals(command)) {
            return local.getBoolean("$shipTrophyIsaSubtypeActive");
        }
        if ("currentSubtypeUnlocked".equals(command)) {
            return local.getBoolean("$shipTrophyIsaSubtypeUnlocked");
        }
        if ("currentSubtypeModded".equals(command)) {
            return local.getBoolean("$shipTrophyIsaSubtypeModded");
        }
        if ("currentSubtypeUnlockSeen".equals(command)) {
            return local.getBoolean("$shipTrophyIsaSubtypeUnlockSeen");
        }
        if ("markCurrentSubtypeSeen".equals(command)) {
            TrophySubtypeSpec subtype = TrophySubtypeRegistry.getSubtype(local.getString(CURRENT_SUBTYPE));
            if (subtype != null) IsaTrophyManager.setUnlockDialogueSeen(subtype.hullModId);
            return true;
        }

        return false;
    }

    private static String value(List<Token> params, int index, Map<String, MemoryAPI> memoryMap) {
        if (index < 0 || index >= params.size()) return "";
        String result = params.get(index).getString(memoryMap);
        return result == null ? "" : result;
    }

    private static void prepare(InteractionDialogAPI dialog) {
        dialog.getTextPanel().clear();
        dialog.getOptionPanel().clearOptions();
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(IsaTrophyManager.findHomeMarket());
        if (isa != null) dialog.getVisualPanel().showPersonInfo(contactDisplayPerson(dialog, isa));
    }

    private static void showContactPortrait(InteractionDialogAPI dialog) {
        PersonAPI isa = IsaTrophyManager.getOrCreateIsa(IsaTrophyManager.findHomeMarket());
        if (isa != null) {
            dialog.getVisualPanel().hideSecondPerson();
            dialog.getVisualPanel().showPersonInfo(contactDisplayPerson(dialog, isa));
        }
    }

    private static PersonAPI contactDisplayPerson(InteractionDialogAPI dialog, PersonAPI isa) {
        if (!isHallOfficeContact(dialog)) return isa;
        PersonAPI display = Global.getFactory().createPerson();
        display.setName(new FullName(
                ShipTrophyL10n.get("isa_first_name"),
                ShipTrophyL10n.get("isa_last_name"),
                FullName.Gender.FEMALE));
        display.setGender(FullName.Gender.FEMALE);
        display.setFaction(isa.getFaction().getId());
        display.setRankId(isa.getRankId());
        display.setPostId(isa.getPostId());
        display.setPersonality(isa.getPersonalityAPI().getId());
        display.setPortraitSprite(IsaTrophyManager.getIsaContactPortraitSprite());
        return display;
    }

    private static boolean isHallOfficeContact(InteractionDialogAPI dialog) {
        if (dialog == null || !IsaTrophyManager.wasOfficerGranted()) return false;

        SectorEntityToken target = dialog.getInteractionTarget();
        MarketAPI current = target == null ? null : target.getMarket();
        MarketAPI home = IsaTrophyManager.findHomeMarket();
        return current != null
                && current.isPlayerOwned()
                && home != null
                && current.getId().equals(home.getId());
    }

    private static void showBodyguards(InteractionDialogAPI dialog) {
        PersonAPI first = Global.getFactory().createPerson();
        first.setName(new FullName(
                ShipTrophyL10n.get("bodyguard_first_name"),
                ShipTrophyL10n.get("bodyguard_last_name"),
                FullName.Gender.ANY));
        first.setPortraitSprite("graphics/portraits/portrait_hegemony02.png");
        PersonAPI second = Global.getFactory().createPerson();
        second.setName(new FullName(
                ShipTrophyL10n.get("bodyguard_first_name"),
                ShipTrophyL10n.get("bodyguard_last_name"),
                FullName.Gender.ANY));
        second.setPortraitSprite("graphics/portraits/portrait_league07.png");
        dialog.getVisualPanel().showPersonInfo(first);
        dialog.getVisualPanel().showSecondPerson(second);
    }

    private static TrophyNetwork.NetworkStats refreshStats() {
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        TrophyNetwork.refreshPlayerHullmodUnlocks(stats);
        IsaTrophyManager.refreshIsaHullmod();
        return stats;
    }

    private static void prepareMain(InteractionDialogAPI dialog, MemoryAPI local) {
        prepare(dialog);
        TrophyNetwork.NetworkStats stats = refreshStats();
        set(local, "$shipTrophyIsaRooms", Integer.toString(stats.functionalRooms));
        set(local, "$shipTrophyIsaUniqueHulls", Integer.toString(stats.uniqueHullIds.size()));
        set(local, "$shipTrophyIsaUniqueDp", Integer.toString(Math.round(stats.uniqueDeploymentPoints)));
    }

    private static void prepareMasterwork(InteractionDialogAPI dialog, MemoryAPI local) {
        prepare(dialog);
        IsaTrophyManager.setMasterworkBriefed();
        boolean complete = IsaTrophyManager.isMasterworkComplete();
        if (complete) {
            IsaTrophyManager.setMasterworkCompleted();
            IsaTrophyManager.refreshIsaHullmod();
        }
        set(local, "$shipTrophyIsaMasterworkComplete", complete);
    }

    private static void showMasterworkStatus(TextPanelAPI text) {
        for (IsaTrophyManager.ShowcaseRequirement requirement : IsaTrophyManager.getMasterworkRequirements()) {
            addStatusLine(text, requirement.met,
                    localizedMasterworkName(requirement));
        }
    }

    private static void showMasterworkIntro(TextPanelAPI text) {
        List<IsaTrophyManager.ShowcaseRequirement> requirements = IsaTrophyManager.getMasterworkRequirements();
        int complete = 0;
        for (IsaTrophyManager.ShowcaseRequirement requirement : requirements) {
            if (requirement.met) complete++;
        }

        if (complete >= requirements.size()) {
            return;
        }

        if (complete == 0) {
            text.addPara(ShipTrophyL10n.get("masterwork_intro"));
            return;
        }

        String missing = getMissingMasterworkHullNames(requirements);
        LabelAPI line = text.addPara(
                ShipTrophyL10n.get("masterwork_progress"),
                Misc.getHighlightColor(),
                Integer.toString(complete),
                missing);
        line.setHighlight(Integer.toString(complete), missing);
        line.setHighlightColors(Misc.getHighlightColor(), Misc.getNegativeHighlightColor());
    }

    static void populateMasterworkOption(OptionPanelAPI options) {
        List<IsaTrophyManager.ShowcaseRequirement> requirements = IsaTrophyManager.getMasterworkRequirements();
        int complete = 0;
        for (IsaTrophyManager.ShowcaseRequirement requirement : requirements) {
            if (requirement.met) complete++;
        }

        int total = requirements.size();
        boolean unlocked = total > 0 && complete >= total;
        boolean unseen = unlocked && !IsaTrophyManager.wasUnlockDialogueSeen(IsaTrophyManager.PROVENANCE_HULLMOD_ID);
        Color color = unseen ? Misc.getHighlightColor() : Misc.getTextColor();

        String label = ShipTrophyL10n.format(
                "masterwork_review", complete, total);
        String tooltip;
        if (unlocked) {
            tooltip = unseen
                    ? ShipTrophyL10n.get("masterwork_complete_unseen")
                    : ShipTrophyL10n.get("masterwork_complete_seen");
        } else {
            tooltip = ShipTrophyL10n.format("masterwork_still_needed",
                    getMissingMasterworkHullNames(requirements));
        }
        options.addOption(label, "ship_trophy_isa_masterwork", color, tooltip);
    }

    private static String getMissingMasterworkHullNames(
            List<IsaTrophyManager.ShowcaseRequirement> requirements) {
        StringBuilder result = new StringBuilder();
        int missing = 0;
        for (IsaTrophyManager.ShowcaseRequirement requirement : requirements) {
            if (!requirement.met) missing++;
        }

        int seen = 0;
        for (IsaTrophyManager.ShowcaseRequirement requirement : requirements) {
            if (requirement.met) continue;
            seen++;
            if (result.length() > 0) {
                result.append(ShipTrophyL10n.get(
                        seen == missing && missing > 1
                                ? "list_final_separator" : "list_separator"));
            }
            result.append(localizedMasterworkName(requirement));
        }
        return result.toString();
    }

    private static void grantOfficer(MemoryAPI local) {
        String result;
        if (IsaTrophyManager.wasOfficerGranted()) {
            result = "already";
        } else if (!IsaTrophyManager.isAweRecruitmentReady()) {
            result = "incomplete";
        } else if (IsaTrophyManager.grantOfficerIfAweComplete()) {
            result = "joined";
        } else if (IsaTrophyManager.wasOfficerGranted()) {
            result = "already";
        } else {
            result = "incomplete";
        }
        set(local, JOIN_RESULT, result);
    }

    private static boolean hasUniquePrograms(boolean modded) {
        if (!modded) return true;
        for (TrophyUniqueShowcases.ShowcaseSpec spec : TrophyUniqueShowcases.getActiveShowcases()) {
            if (spec.isModIntegration()) return true;
        }
        return false;
    }

    private static boolean isUniqueActive(String id) {
        if ("gaze".equals(id) || "contempt".equals(id)) return true;
        TrophyUniqueShowcases.ShowcaseSpec spec = findShowcase(resolveShowcaseId(id));
        return spec != null && spec.isActive();
    }

    private static boolean isUniqueShowcased(String id) {
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        if ("gaze".equals(id)) return TrophyNetwork.hasShowcasedHull(stats, Gaze.REQUIRED_BASE_HULL_ID);
        if ("contempt".equals(id)) return TrophyNetwork.hasShowcasedHull(stats, Contempt.REQUIRED_BASE_HULL_ID);
        TrophyUniqueShowcases.ShowcaseSpec spec = findShowcase(resolveShowcaseId(id));
        return spec != null && spec.isActive() && TrophyNetwork.hasShowcasedHull(stats, spec.hullId);
    }

    private static void showUniqueStatus(TextPanelAPI text, boolean modded) {
        TrophyNetwork.NetworkStats stats = refreshStats();
        if (!modded) {
            addStatusLine(text, TrophyNetwork.hasShowcasedHull(stats, Gaze.REQUIRED_BASE_HULL_ID),
                    ShipTrophyL10n.format("unique_status_ziggurat",
                            IsaTrophyManager.getHullModName(Gaze.HULLMOD_ID)));
            addStatusLine(text, TrophyNetwork.hasShowcasedHull(stats, Contempt.REQUIRED_BASE_HULL_ID),
                    ShipTrophyL10n.format("unique_status_onslaught",
                            IsaTrophyManager.getHullModName(Contempt.HULLMOD_ID)));
        }
        for (TrophyUniqueShowcases.ShowcaseSpec spec : TrophyUniqueShowcases.getActiveShowcases()) {
            if (spec.isModIntegration() != modded) continue;
            addStatusLine(text, TrophyNetwork.hasShowcasedHull(stats, spec.hullId),
                    ShipTrophyL10n.format("unique_status_generic",
                            localizedShowcaseDisplayName(spec),
                            IsaTrophyManager.getHullModName(spec.hullModId)));
        }
    }

    static void populateGenericUniqueOptions(OptionPanelAPI options, boolean modded) {
        TrophyNetwork.NetworkStats stats = refreshStats();
        for (TrophyUniqueShowcases.ShowcaseSpec spec : TrophyUniqueShowcases.getActiveShowcases()) {
            if (spec.isModIntegration() != modded) continue;
            if ("kh_invictus".equals(spec.id) || "black_lion".equals(spec.id)) continue;
            boolean showcased = TrophyNetwork.hasShowcasedHull(stats, spec.hullId);
            String showcaseName = localizedShowcaseDisplayName(spec);
            addUnlockOption(options,
                    ShipTrophyL10n.format("unique_discuss", showcaseName),
                    UNIQUE_PREFIX + spec.id,
                    showcased, spec.hullModId,
                    showcased
                            ? ShipTrophyL10n.format("unlock_unlocked",
                                    IsaTrophyManager.getHullModName(spec.hullModId))
                            : ShipTrophyL10n.format("unlock_requires_display",
                                    showcaseName));
        }
    }

    private static void prepareUnique(MemoryAPI local, String id) {
        TrophyUniqueShowcases.ShowcaseSpec spec = findShowcase(id);
        boolean active = spec != null && spec.isActive();
        boolean showcased = active && TrophyNetwork.hasShowcasedHull(refreshStats(), spec.hullId);
        set(local, CURRENT_UNIQUE, id == null ? "" : id);
        set(local, "$shipTrophyIsaUniqueActive", active);
        set(local, "$shipTrophyIsaUniqueShowcased", showcased);
        set(local, "$shipTrophyIsaUniqueModded", active && spec.isModIntegration());
        set(local, "$shipTrophyIsaUniqueShowcaseName", active
                ? localizedShowcaseName(spec)
                : ShipTrophyL10n.get("generic_this_hull"));
        set(local, "$shipTrophyIsaUniqueHullmodName", active
                ? IsaTrophyManager.getHullModName(spec.hullModId)
                : ShipTrophyL10n.get("generic_unknown"));
        set(local, "$shipTrophyIsaUniqueReceipt", active
                ? ShipTrophyL10n.format("unlock_receipt",
                        IsaTrophyManager.getHullModName(spec.hullModId))
                : ShipTrophyL10n.get("unlock_receipt_generic"));
    }

    private static boolean hasSubtypePrograms(boolean modded) {
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (subtype.isModIntegration() != modded) continue;
            if (subtype.hasHullModUnlock() && IsaTrophyManager.hullModExists(subtype.hullModId)) return true;
        }
        return false;
    }

    static void populateSubtypeOptions(OptionPanelAPI options, boolean modded) {
        TrophyNetwork.NetworkStats stats = refreshStats();
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (subtype.isModIntegration() != modded) continue;
            if (!subtype.hasHullModUnlock() || !IsaTrophyManager.hullModExists(subtype.hullModId)) continue;

            int current = Math.round(stats.getSubtypeDp(subtype.id));
            int needed = Math.round(subtype.unlockDp);
            boolean unlocked = current >= needed;
            String label = ShipTrophyL10n.format("subtype_option",
                    subtype.displayName, current, needed);
            String tooltip = unlocked
                    ? ShipTrophyL10n.format("unlock_unlocked",
                            IsaTrophyManager.getHullModName(subtype.hullModId))
                    : ShipTrophyL10n.format("subtype_requires_dp",
                            needed, subtype.showcaseName);
            addUnlockOption(options, label, SUBTYPE_PREFIX + subtype.id, unlocked, subtype.hullModId, tooltip);
        }
    }

    private static void prepareSubtype(MemoryAPI local, String id) {
        TrophySubtypeSpec subtype = TrophySubtypeRegistry.getSubtype(id);
        TrophyNetwork.NetworkStats stats = refreshStats();
        boolean active = subtype != null && subtype.isActive() && subtype.hasHullModUnlock()
                && IsaTrophyManager.hullModExists(subtype.hullModId);
        float current = active ? stats.getSubtypeDp(subtype.id) : 0f;
        int currentRounded = Math.round(current);
        int needed = active ? Math.round(subtype.unlockDp) : 0;
        int remaining = Math.max(0, needed - currentRounded);
        boolean unlocked = active && current >= subtype.unlockDp;
        String hullModName = active
                ? IsaTrophyManager.getHullModName(subtype.hullModId)
                : ShipTrophyL10n.get("generic_unknown");

        set(local, CURRENT_SUBTYPE, id == null ? "" : id);
        set(local, "$shipTrophyIsaSubtypeActive", active);
        set(local, "$shipTrophyIsaSubtypeUnlocked", unlocked);
        set(local, "$shipTrophyIsaSubtypeModded", active && subtype.isModIntegration());
        set(local, "$shipTrophyIsaSubtypeUnlockSeen", active && IsaTrophyManager.wasUnlockDialogueSeen(subtype.hullModId));
        set(local, "$shipTrophyIsaSubtypeCurrentDp", Integer.toString(currentRounded));
        set(local, "$shipTrophyIsaSubtypeUnlockDp", Integer.toString(needed));
        set(local, "$shipTrophyIsaSubtypeRemainingDp", Integer.toString(remaining));
        set(local, "$shipTrophyIsaSubtypeShowcaseName", active
                ? subtype.showcaseName
                : ShipTrophyL10n.get("generic_this_family"));
        set(local, "$shipTrophyIsaSubtypeHullmodName", hullModName);
        set(local, "$shipTrophyIsaSubtypeReceipt",
                ShipTrophyL10n.format("unlock_receipt", hullModName));
    }

    private static void addUnlockOption(OptionPanelAPI options, String label, Object optionId,
            boolean unlocked, String hullModId, String tooltip) {
        Color color = Misc.getTextColor();
        if (unlocked && !IsaTrophyManager.wasUnlockDialogueSeen(hullModId)) {
            color = Misc.getHighlightColor();
        }
        options.addOption(label, optionId, color, tooltip);
    }

    private static void addStatusLine(TextPanelAPI text, boolean complete, String label) {
        Color color = complete ? Misc.getHighlightColor() : Misc.getNegativeHighlightColor();
        LabelAPI line = text.addPara(ShipTrophyL10n.format(
                complete ? "status_complete_line" : "status_needed_line", label));
        line.setHighlight(ShipTrophyL10n.get(
                complete ? "status_complete_highlight" : "status_needed_highlight"));
        line.setHighlightColor(color);
    }

    private static String localizedMasterworkName(
            IsaTrophyManager.ShowcaseRequirement requirement) {
        if (requirement == null) return ShipTrophyL10n.get("generic_unknown");
        if ("onslaught_xiv".equals(requirement.hullId)) {
            return ShipTrophyL10n.get("masterwork_hull_onslaught");
        }
        if ("paragon".equals(requirement.hullId)) {
            return ShipTrophyL10n.get("masterwork_hull_paragon");
        }
        if ("invictus".equals(requirement.hullId)) {
            return ShipTrophyL10n.get("masterwork_hull_invictus");
        }
        if ("conquest".equals(requirement.hullId)) {
            return ShipTrophyL10n.get("masterwork_hull_conquest");
        }
        if ("executor".equals(requirement.hullId)) {
            return ShipTrophyL10n.get("masterwork_hull_executor");
        }
        return requirement.displayName;
    }

    private static String localizedShowcaseDisplayName(
            TrophyUniqueShowcases.ShowcaseSpec spec) {
        if (spec == null) return ShipTrophyL10n.get("generic_unknown");
        if ("kh_invictus".equals(spec.id)) {
            return ShipTrophyL10n.get("showcase_abundant_mercy");
        }
        if ("black_lion".equals(spec.id)) {
            return ShipTrophyL10n.get("showcase_black_lion");
        }
        return spec.displayName;
    }

    private static String localizedShowcaseName(
            TrophyUniqueShowcases.ShowcaseSpec spec) {
        return localizedShowcaseDisplayName(spec);
    }

    private static TrophyUniqueShowcases.ShowcaseSpec findShowcase(String id) {
        if (id == null) return null;
        for (TrophyUniqueShowcases.ShowcaseSpec spec : TrophyUniqueShowcases.getAllShowcases()) {
            if (id.equals(spec.id)) return spec;
        }
        return null;
    }

    private static String resolveShowcaseId(String id) {
        if ("mercy".equals(id)) return "kh_invictus";
        if ("lion".equals(id)) return "black_lion";
        return id;
    }

    private static String resolveHullMod(String id) {
        if ("awe".equals(id)) return IsaTrophyManager.PROVENANCE_HULLMOD_ID;
        if ("gaze".equals(id)) return Gaze.HULLMOD_ID;
        if ("contempt".equals(id)) return Contempt.HULLMOD_ID;
        if ("mercy".equals(id)) return AbundantMercyVow.HULLMOD_ID;
        if ("lion".equals(id)) return BlackLionInheritance.HULLMOD_ID;
        TrophyUniqueShowcases.ShowcaseSpec spec = findShowcase(resolveShowcaseId(id));
        return spec == null ? id : spec.hullModId;
    }

    private static boolean isModded(String value) {
        return "modded".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    private static void set(MemoryAPI memory, String key, Object value) {
        memory.set(key, value, 0f);
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
