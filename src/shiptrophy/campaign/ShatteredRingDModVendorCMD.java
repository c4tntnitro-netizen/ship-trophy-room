package shiptrophy.campaign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetMemberPickerListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.Sounds;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

/** Rules command for the Shattered Ring's deliberate D-mod installation service. */
public class ShatteredRingDModVendorCMD implements CommandPlugin {
    private static final String PICK_SHIP = "ship_trophy_dmod_vendor_pick_ship";
    private static final String BACK = "ship_trophy_dmod_vendor_back";
    private static final String PREV = "ship_trophy_dmod_vendor_prev";
    private static final String NEXT = "ship_trophy_dmod_vendor_next";
    private static final String LEAVE = "ship_trophy_dmod_vendor_leave";
    private static final String INSTALL_PREFIX = "ship_trophy_dmod_vendor_install_";

    private static final String SELECTED_MEMBER = "$shipTrophyDModVendorMember";
    private static final String PAGE = "$shipTrophyDModVendorPage";
    private static final int PAGE_SIZE = 8;

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (params == null || params.isEmpty()) return false;

        String action = value(params, 0, memoryMap);
        if ("isInstallOption".equals(action)) {
            return selectedOption(memoryMap).startsWith(INSTALL_PREFIX);
        }
        if (dialog == null) return false;

        if ("open".equals(action)) {
            showIntroduction(dialog);
            showMainMenu(dialog);
            return true;
        }
        if ("pickShip".equals(action)) {
            showShipPicker(dialog, memoryMap);
            return true;
        }
        if ("back".equals(action)) {
            clearSelection(memoryMap);
            showMainMenu(dialog);
            return true;
        }
        if ("previousPage".equals(action)) {
            changePage(dialog, memoryMap, -1);
            return true;
        }
        if ("nextPage".equals(action)) {
            changePage(dialog, memoryMap, 1);
            return true;
        }
        if ("installSelected".equals(action)) {
            installSelected(dialog, memoryMap);
            return true;
        }
        return false;
    }

    private static void showIntroduction(InteractionDialogAPI dialog) {
        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("The Ring's nanoforge engineer works out of a hullbreaking cradle overlooking "
                + "the wreck-farms. The gantries around it are equally suited to careful restoration "
                + "and carefully planned damage.");
        text.addPara("\"Most yards promise to take the scars out,\" the engineer says. \"I can put "
                + "exactly the right scar back in. One story point per defect. You name the ship and "
                + "the compromise.\"");
        text.addPara("This service installs one eligible D-mod of your choice. A ship may not exceed "
                + "the normal limit of %s D-mods.", Misc.getTextColor(), Misc.getHighlightColor(),
                "one eligible D-mod", String.valueOf(DModManager.MAX_DMODS_FROM_COMBAT));
    }

    private static void showMainMenu(InteractionDialogAPI dialog) {
        OptionPanelAPI options = dialog.getOptionPanel();
        options.clearOptions();
        options.addOption("Select a ship for commissioned damage.", PICK_SHIP,
                "Choose a ship, then choose the exact D-mod to install for one story point.");
        options.addOption("Leave.", LEAVE);
        dialog.setOptionOnEscape("Leave.", LEAVE);
    }

    private static void showShipPicker(final InteractionDialogAPI dialog,
            final Map<String, MemoryAPI> memoryMap) {
        List<FleetMemberAPI> eligible = getEligibleShips();
        if (eligible.isEmpty()) {
            dialog.getTextPanel().addPara("None of the ships in your fleet can accept another "
                    + "eligible D-mod.", Misc.getNegativeHighlightColor());
            showMainMenu(dialog);
            return;
        }

        dialog.showFleetMemberPickerDialog("Select a ship", "Continue", "Cancel",
                7, 8, 72f, true, false, eligible, new FleetMemberPickerListener() {
                    @Override
                    public void pickedFleetMembers(List<FleetMemberAPI> members) {
                        if (members == null || members.isEmpty()) return;
                        FleetMemberAPI member = members.get(0);
                        MemoryAPI local = local(memoryMap);
                        if (local != null) {
                            local.set(SELECTED_MEMBER, member);
                            local.set(PAGE, 0);
                        }
                        showDModPage(dialog, memoryMap);
                    }

                    @Override
                    public void cancelledFleetMemberPicking() {
                        showMainMenu(dialog);
                    }
                });
    }

    private static void showDModPage(InteractionDialogAPI dialog,
            Map<String, MemoryAPI> memoryMap) {
        FleetMemberAPI member = selectedMember(memoryMap);
        if (!isStillInFleet(member)) {
            dialog.getTextPanel().addPara("That ship is no longer available.",
                    Misc.getNegativeHighlightColor());
            clearSelection(memoryMap);
            showMainMenu(dialog);
            return;
        }

        List<HullModSpecAPI> choices = getEligibleDMods(member);
        if (choices.isEmpty() || DModManager.getNumDMods(member.getVariant())
                >= DModManager.MAX_DMODS_FROM_COMBAT) {
            dialog.getTextPanel().addPara("%s cannot accept another eligible D-mod.",
                    Misc.getNegativeHighlightColor(), member.getShipName());
            clearSelection(memoryMap);
            showMainMenu(dialog);
            return;
        }

        int maxPage = Math.max(0, (choices.size() - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(page(memoryMap), maxPage));
        MemoryAPI local = local(memoryMap);
        if (local != null) local.set(PAGE, page);

        dialog.getVisualPanel().showFleetMemberInfo(member, true);
        dialog.getTextPanel().addPara("The engineer calls up %s's structural plan. Choose the defect "
                + "to commission.", Misc.getTextColor(), Misc.getHighlightColor(), member.getShipName());

        OptionPanelAPI options = dialog.getOptionPanel();
        options.clearOptions();
        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, choices.size());
        for (int i = start; i < end; i++) {
            HullModSpecAPI spec = choices.get(i);
            String optionId = INSTALL_PREFIX + spec.getId();
            String description = spec.getDescription(member.getVariant().getHullSize());
            if (description == null || description.trim().isEmpty()) {
                description = "Install this D-mod for one story point.";
            }
            options.addOption(spec.getDisplayName() + " (1 story point)", optionId, description);
            dialog.makeStoryOption(optionId, 1, 1f, Sounds.STORY_POINT_SPEND);
        }

        if (page > 0) options.addOption("Previous defects.", PREV);
        if (page < maxPage) options.addOption("More defects.", NEXT);
        options.addOption("Choose another ship.", BACK);
        options.addOption("Leave.", LEAVE);
        dialog.setOptionOnEscape("Choose another ship.", BACK);
    }

    private static void changePage(InteractionDialogAPI dialog,
            Map<String, MemoryAPI> memoryMap, int delta) {
        MemoryAPI local = local(memoryMap);
        if (local != null) local.set(PAGE, Math.max(0, page(memoryMap) + delta));
        showDModPage(dialog, memoryMap);
    }

    private static void installSelected(InteractionDialogAPI dialog,
            Map<String, MemoryAPI> memoryMap) {
        FleetMemberAPI member = selectedMember(memoryMap);
        String option = selectedOption(memoryMap);
        String dmodId = option.startsWith(INSTALL_PREFIX)
                ? option.substring(INSTALL_PREFIX.length()) : "";
        HullModSpecAPI chosen = null;
        if (isStillInFleet(member)) {
            for (HullModSpecAPI spec : getEligibleDMods(member)) {
                if (spec.getId().equals(dmodId)) {
                    chosen = spec;
                    break;
                }
            }
        }

        if (member == null || chosen == null
                || DModManager.getNumDMods(member.getVariant())
                        >= DModManager.MAX_DMODS_FROM_COMBAT) {
            dialog.getTextPanel().addPara("The commission can no longer be completed.",
                    Misc.getNegativeHighlightColor());
            clearSelection(memoryMap);
            showMainMenu(dialog);
            return;
        }

        ShipVariantAPI variant = member.getVariant().clone();
        variant.setOriginalVariant(null);
        variant.setSource(VariantSource.REFIT);
        DModManager.setDHull(variant);
        variant.addPermaMod(chosen.getId());
        member.setVariant(variant, false, true);
        member.setStatUpdateNeeded(true);
        member.updateStats();
        Global.getSector().getPlayerFleet().getFleetData().setSyncNeeded();

        TextPanelAPI text = dialog.getTextPanel();
        text.addPara("The hullbreaking cradle closes around %s. Precision charges fire, gantries "
                + "twist stressed members out of alignment, and the engineer signs off on the damage.",
                Misc.getTextColor(), Misc.getHighlightColor(), member.getShipName());
        text.addPara("%s acquired %s.", Misc.getPositiveHighlightColor(),
                member.getShipName(), chosen.getDisplayName());

        if (DModManager.getNumDMods(member.getVariant()) >= DModManager.MAX_DMODS_FROM_COMBAT
                || getEligibleDMods(member).isEmpty()) {
            clearSelection(memoryMap);
            showMainMenu(dialog);
        } else {
            showDModPage(dialog, memoryMap);
        }
    }

    private static List<FleetMemberAPI> getEligibleShips() {
        List<FleetMemberAPI> result = new ArrayList<FleetMemberAPI>();
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return result;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData()
                .getMembersListCopy()) {
            if (member == null || member.isFighterWing() || member.isStation()) continue;
            if (DModManager.getNumDMods(member.getVariant())
                    >= DModManager.MAX_DMODS_FROM_COMBAT) continue;
            if (!getEligibleDMods(member).isEmpty()) result.add(member);
        }
        return result;
    }

    private static List<HullModSpecAPI> getEligibleDMods(FleetMemberAPI member) {
        List<HullModSpecAPI> result = new ArrayList<HullModSpecAPI>();
        if (member == null || member.getVariant() == null) return result;

        result.addAll(DModManager.getModsWithTags(Tags.HULLMOD_DMOD));
        DModManager.removeUnsuitedMods(member.getVariant(), result);
        DModManager.removeModsAlreadyInVariant(member.getVariant(), result);

        for (Iterator<HullModSpecAPI> iter = result.iterator(); iter.hasNext();) {
            HullModSpecAPI spec = iter.next();
            if (spec == null || spec.isHiddenEverywhere() || spec.hasTag("hide_in_codex")
                    || spec.hasTag("codex_require_related")
                    || "destroyed_mounts".equals(spec.getId())
                    || "ill_advised".equals(spec.getId())) {
                iter.remove();
            }
        }

        Collections.sort(result, new Comparator<HullModSpecAPI>() {
            @Override
            public int compare(HullModSpecAPI left, HullModSpecAPI right) {
                return left.getDisplayName().compareToIgnoreCase(right.getDisplayName());
            }
        });
        return result;
    }

    private static boolean isStillInFleet(FleetMemberAPI member) {
        return member != null && Global.getSector() != null
                && Global.getSector().getPlayerFleet() != null
                && Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()
                        .contains(member);
    }

    private static FleetMemberAPI selectedMember(Map<String, MemoryAPI> memoryMap) {
        MemoryAPI local = local(memoryMap);
        Object value = local == null ? null : local.get(SELECTED_MEMBER);
        return value instanceof FleetMemberAPI ? (FleetMemberAPI) value : null;
    }

    private static int page(Map<String, MemoryAPI> memoryMap) {
        MemoryAPI local = local(memoryMap);
        return local == null || !local.contains(PAGE) ? 0 : local.getInt(PAGE);
    }

    private static void clearSelection(Map<String, MemoryAPI> memoryMap) {
        MemoryAPI local = local(memoryMap);
        if (local == null) return;
        local.unset(SELECTED_MEMBER);
        local.unset(PAGE);
    }

    private static MemoryAPI local(Map<String, MemoryAPI> memoryMap) {
        return memoryMap == null ? null : memoryMap.get(MemKeys.LOCAL);
    }

    private static String selectedOption(Map<String, MemoryAPI> memoryMap) {
        MemoryAPI local = local(memoryMap);
        String option = local == null ? null : local.getString("$option");
        return option == null ? "" : option;
    }

    private static String value(List<Token> params, int index,
            Map<String, MemoryAPI> memoryMap) {
        if (index < 0 || index >= params.size()) return "";
        String result = params.get(index).getString(memoryMap);
        return result == null ? "" : result;
    }

    @Override
    public boolean doesCommandAddOptions() {
        return true;
    }

    @Override
    public int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap) {
        return 0;
    }
}
