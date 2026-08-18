package shiptrophy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.hullmods.Contempt;
import shiptrophy.hullmods.Gaze;

public class IsaTrophyManager {
    public static final String PERSON_ID = "ship_trophy_isa";
    public static final String PORTRAIT_ID = "ship_trophy_isa";
    public static final String PORTRAIT_PATH = "graphics/portraits/ship_trophy_isa.png";
    public static final String OFFICER_PORTRAIT_ID = "ship_trophy_isa_officer";
    public static final String OFFICER_PORTRAIT_PATH = "graphics/hullmods/awe.png";
    public static final String PROVENANCE_HULLMOD_ID = "ship_trophy_isa_provenance";
    public static final String DEFENSIVE_SKILL_ID = "ship_trophy_isa_field_refit";
    public static final String MOBILITY_SKILL_ID = "ship_trophy_isa_redline";

    private static final ShowcaseRequirement[] MASTERWORK_REQUIREMENTS = new ShowcaseRequirement[] {
            new ShowcaseRequirement("Onslaught XIV", "onslaught_xiv", null),
            new ShowcaseRequirement("Paragon", "paragon", "paragon"),
            new ShowcaseRequirement("Invictus", "invictus", "invictus"),
            new ShowcaseRequirement("Conquest", "conquest", "conquest"),
            new ShowcaseRequirement("Executor", "executor", "executor")
    };

    public static void ensureBarEventCreator() {
        if (Global.getSector() == null || findHomeMarket() == null) return;
        BarEventManager manager = BarEventManager.getInstance();
        if (manager != null && !manager.hasEventCreator(IsaBarEventCreator.class)) {
            manager.addEventCreator(new IsaBarEventCreator());
        }
    }

    public static MarketAPI findHomeMarket() {
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) return null;

        String storedId = Global.getSector().getMemoryWithoutUpdate().getString(ShipTrophyRoomIds.MEMORY_ISA_HOME_MARKET);
        MarketAPI stored = findMarketById(storedId);
        if (isFunctionalTrophyMarket(stored)) return stored;

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!isFunctionalTrophyMarket(market)) continue;
            Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_ISA_HOME_MARKET, market.getId());
            return market;
        }
        return null;
    }

    public static boolean isIntroduced() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(ShipTrophyRoomIds.MEMORY_ISA_INTRODUCED);
    }

    public static void setIntroduced(boolean introduced) {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_ISA_INTRODUCED, introduced);
    }

    public static boolean wasMasterworkBriefed() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(ShipTrophyRoomIds.MEMORY_ISA_MASTERWORK_BRIEFED);
    }

    public static void setMasterworkBriefed() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_ISA_MASTERWORK_BRIEFED, true);
    }

    public static boolean wasMasterworkCompleted() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(ShipTrophyRoomIds.MEMORY_ISA_MASTERWORK_COMPLETED);
    }

    public static void setMasterworkCompleted() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_ISA_MASTERWORK_COMPLETED, true);
    }

    public static boolean wasUnlockDialogueSeen(String hullModId) {
        return Global.getSector() != null && hullModId != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds.MEMORY_ISA_UNLOCK_DIALOGUE_SEEN_PREFIX + hullModId);
    }

    public static void setUnlockDialogueSeen(String hullModId) {
        if (Global.getSector() == null || hullModId == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_UNLOCK_DIALOGUE_SEEN_PREFIX + hullModId, true);
    }

    public static boolean wasOfficerGranted() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(ShipTrophyRoomIds.MEMORY_ISA_OFFICER_GRANTED);
    }

    private static void setOfficerGranted() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(ShipTrophyRoomIds.MEMORY_ISA_OFFICER_GRANTED, true);
    }

    public static boolean wasFactionCompletionSceneShown() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE);
    }

    public static void setFactionCompletionSceneShown() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE, true);
        setFactionCompletionSceneValidated();
        Global.getSector().getMemoryWithoutUpdate().unset(
                ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE_NEEDS_REPLAY);
    }

    public static boolean wasFactionCompletionSceneValidated() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE_VALIDATED);
    }

    public static void setFactionCompletionSceneValidated() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE_VALIDATED, true);
    }

    public static boolean doesFactionCompletionSceneNeedReplay() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE_NEEDS_REPLAY);
    }

    public static void markFactionCompletionSceneForReplay() {
        if (Global.getSector() == null || wasFactionCompletionSceneValidated()) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_FACTION_COMPLETION_SCENE_NEEDS_REPLAY, true);
    }

    public static boolean wasFactionVisitSceneShown(String factionId) {
        return Global.getSector() != null && factionId != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds.MEMORY_ISA_FACTION_VISIT_SCENE_PREFIX + factionId);
    }

    public static void setFactionVisitSceneShown(String factionId) {
        if (Global.getSector() == null || factionId == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_FACTION_VISIT_SCENE_PREFIX + factionId, true);
    }

    public static boolean wasShatteredRingHomecomingShown() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate().getBoolean(
                        ShipTrophyRoomIds.MEMORY_ISA_SHATTERED_RING_HOMECOMING);
    }

    public static void setShatteredRingHomecomingShown() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                ShipTrophyRoomIds.MEMORY_ISA_SHATTERED_RING_HOMECOMING, true);
    }

    public static boolean isIsaOfficerInPlayerFleet() {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return false;
        return isIsaOfficerInRoster(Global.getSector().getPlayerFleet().getFleetData());
    }

    /**
     * Console/debug entry point that adds Isa without advancing her quest state.
     *
     * @return true if Isa was added, or false if she was already in the roster
     *         or the player fleet was unavailable
     */
    public static boolean addIsaOfficerImmediately() {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return false;

        FleetDataAPI fleetData = Global.getSector().getPlayerFleet().getFleetData();
        if (fleetData == null || isIsaOfficerInRoster(fleetData)) return false;

        PersonAPI isa = getOrCreateIsa(findHomeMarket());
        if (isa == null) return false;
        configureIsaOfficer(isa);
        fleetData.addOfficer(isa);
        return true;
    }

    public static boolean areAllFactionHullmodsComplete(TrophyNetwork.NetworkStats stats) {
        if (stats == null) stats = TrophyNetwork.computeNetworkStats();
        int activePrograms = 0;
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (!subtype.hasHullModUnlock() || !hullModExists(subtype.hullModId)) continue;
            activePrograms++;
            if (stats.getSubtypeDp(subtype.id) < subtype.unlockDp) return false;
        }
        return activePrograms > 0;
    }

    /** Returns true only after every base-game Hall ledger is complete. */
    public static boolean areAllVanillaProgramsComplete(TrophyNetwork.NetworkStats stats) {
        if (!isIntroduced() || !isMasterworkComplete()) return false;
        if (stats == null) stats = TrophyNetwork.computeNetworkStats();

        if (!TrophyNetwork.hasShowcasedHull(stats, Gaze.REQUIRED_BASE_HULL_ID)) return false;
        if (!TrophyNetwork.hasShowcasedHull(stats, Contempt.REQUIRED_BASE_HULL_ID)) return false;

        for (TrophyUniqueShowcases.ShowcaseSpec showcase : TrophyUniqueShowcases.getActiveShowcases()) {
            if (showcase.isModIntegration()) continue;
            if (!TrophyNetwork.hasShowcasedHull(stats, showcase.hullId)) return false;
        }

        int vanillaPrograms = 0;
        for (TrophySubtypeSpec subtype : TrophySubtypeRegistry.getActiveSubtypes()) {
            if (subtype.isModIntegration() || !subtype.hasHullModUnlock()) continue;
            vanillaPrograms++;
            if (!hullModExists(subtype.hullModId)) return false;
            if (stats.getSubtypeDp(subtype.id) < subtype.unlockDp) return false;
        }
        return vanillaPrograms > 0;
    }
    /** Awe is received through its dialogue after the five-hull showcase. */
    public static boolean isAweRecruitmentReady() {
        return isMasterworkComplete()
                && wasUnlockDialogueSeen(PROVENANCE_HULLMOD_ID);
    }

    public static boolean grantOfficerIfAweComplete() {
        if (Global.getSector() == null || wasOfficerGranted()) return false;
        if (!isAweRecruitmentReady()) return false;
        setMasterworkCompleted();

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null || fleet.getFleetData() == null) return false;

        PersonAPI isa = getOrCreateIsa(findHomeMarket());
        if (isa == null) return false;
        configureIsaOfficer(isa);

        FleetDataAPI fleetData = fleet.getFleetData();
        if (!isIsaOfficerInRoster(fleetData)) {
            fleetData.addOfficer(isa);
        }

        setOfficerGranted();

        if (Global.getSector().getCampaignUI() != null) {
            Global.getSector().getCampaignUI().addMessage(
                    "Isa Leicester has joined your fleet as an officer.", Misc.getBasePlayerColor());
        }
        return true;
    }

    private static boolean isIsaOfficerInRoster(FleetDataAPI fleetData) {
        if (fleetData == null) return false;
        for (OfficerDataAPI officer : fleetData.getOfficersCopy()) {
            if (officer != null && officer.getPerson() != null && PERSON_ID.equals(officer.getPerson().getId())) return true;
        }
        return false;
    }

    private static void configureIsaOfficer(PersonAPI person) {
        if (person == null) return;
        person.setName(new FullName(
                ShipTrophyL10n.get("isa_first_name"),
                ShipTrophyL10n.get("isa_last_name"),
                FullName.Gender.FEMALE));
        person.setGender(FullName.Gender.FEMALE);
        person.setFaction(Factions.PLAYER);
        person.setPortraitSprite(getIsaPortraitSprite());
        person.setRankId(Ranks.SPACE_CHIEF);
        person.setPostId(Ranks.POST_NANOFORGE_ENGINEER);
        person.setPersonality(Personalities.STEADY);

        MutableCharacterStatsAPI stats = person.getStats();
        if (stats == null) return;
        stats.setLevel(1);
        stats.setSkillLevel(Skills.ORDNANCE_EXPERTISE, 0f);
        stats.setSkillLevel(Skills.SYSTEMS_EXPERTISE, 0f);
        stats.setSkillLevel(Skills.DAMAGE_CONTROL, 0f);
        stats.setSkillLevel(Skills.COMBAT_ENDURANCE, 0f);
        stats.setSkillLevel(Skills.HELMSMANSHIP, 0f);
        stats.setSkillLevel(Skills.IMPACT_MITIGATION, 0f);
        stats.setSkillLevel(DEFENSIVE_SKILL_ID, 2f);
        stats.setSkillLevel(MOBILITY_SKILL_ID, 0f);
        stats.refreshCharacterStatsEffects();
    }

    public static void refreshIsaOfficerSkills() {
        if (Global.getSector() == null || !wasOfficerGranted()) return;
        PersonAPI isa = Global.getSector().getImportantPeople().getPerson(PERSON_ID);
        if (isa == null) isa = getOrCreateIsa(findHomeMarket());
        configureIsaOfficer(isa);
    }

    public static PersonAPI getOrCreateIsa(MarketAPI market) {
        if (Global.getSector() == null) return null;

        PersonAPI person = Global.getSector().getImportantPeople().getPerson(PERSON_ID);
        if (person == null) person = findPersonOnMarket(market);
        if (person == null) {
            person = Global.getFactory().createPerson();
            person.setId(PERSON_ID);
            person.setName(new FullName(
                    ShipTrophyL10n.get("isa_first_name"),
                    ShipTrophyL10n.get("isa_last_name"),
                    FullName.Gender.FEMALE));
            person.setGender(FullName.Gender.FEMALE);
            person.setFaction(Factions.PLAYER);
            person.setRankId(Ranks.SPACE_CHIEF);
            person.setPostId(Ranks.POST_NANOFORGE_ENGINEER);
            person.setPersonality(Personalities.STEADY);
            person.setImportance(PersonImportance.LOW);
            person.addTag(Tags.CONTACT_SCIENCE);
            person.addTag(Tags.CONTACT_TRADE);
            Global.getSector().getImportantPeople().addPerson(person);
        }
        person.setName(new FullName(
                ShipTrophyL10n.get("isa_first_name"),
                ShipTrophyL10n.get("isa_last_name"),
                FullName.Gender.FEMALE));
        person.setGender(FullName.Gender.FEMALE);
        person.setFaction(Factions.PLAYER);
        person.setPersonality(Personalities.STEADY);
        person.setPortraitSprite(getIsaPortraitSprite());

        if (market != null) {
            if (isIntroduced()) {
                person.setMarket(market);
                if (!marketHasPerson(market, person)) market.addPerson(person);
                if (market.getCommDirectory() != null && market.getCommDirectory().getEntryForPerson(PERSON_ID) == null) {
                    market.getCommDirectory().addPerson(person);
                }
            } else {
                removeFromMarketAndComms(market, person);
            }
        }
        return person;
    }

    public static String getIsaPortraitSprite() {
        return getPortraitSprite(PORTRAIT_ID, PORTRAIT_PATH);
    }

    public static String getIsaContactPortraitSprite() {
        if (!wasOfficerGranted()) return getIsaPortraitSprite();
        return getPortraitSprite(OFFICER_PORTRAIT_ID, OFFICER_PORTRAIT_PATH);
    }

    private static String getPortraitSprite(String id, String fallbackPath) {
        if (Global.getSettings() == null) return fallbackPath;
        try {
            return Global.getSettings().getSpriteName("characters", id);
        } catch (Exception ex) {
            return fallbackPath;
        }
    }

    public static void ensureContact(MarketAPI market, TextPanelAPI text) {
        if (Global.getSector() == null || market == null) return;
        PersonAPI person = getOrCreateIsa(market);
        if (person == null) return;

        IsaContactIntel customIntel = null;
        List<ContactIntel> staleIntel = new ArrayList<ContactIntel>();
        for (Object item : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
            ContactIntel intel = (ContactIntel) item;
            if (intel.getPerson() == null || !PERSON_ID.equals(intel.getPerson().getId())) continue;
            if (intel instanceof IsaContactIntel && customIntel == null) {
                customIntel = (IsaContactIntel) intel;
            } else {
                staleIntel.add(intel);
            }
        }
        for (ContactIntel intel : staleIntel) {
            Global.getSector().getIntelManager().removeIntel(intel);
        }
        if (customIntel != null) {
            customIntel.ensurePermanentState();
            customIntel.ensureIsAddedToMarket();
            return;
        }

        ContactIntel intel = new IsaContactIntel(person, market);
        intel.setState(ContactState.NON_PRIORITY);
        intel.ensureIsAddedToMarket();
        if (text == null) {
            Global.getSector().getIntelManager().addIntel(intel, false);
        } else {
            Global.getSector().getIntelManager().addIntel(intel, false, text);
        }
    }

    public static void refreshIsaHullmod() {
        if (Global.getSector() == null || Global.getSector().getPlayerFaction() == null) return;
        boolean unlocked = isMasterworkComplete()
                && hullModExists(PROVENANCE_HULLMOD_ID);
        if (unlocked && !Global.getSector().getPlayerFaction().knowsHullMod(PROVENANCE_HULLMOD_ID)) {
            Global.getSector().getPlayerFaction().addKnownHullMod(PROVENANCE_HULLMOD_ID);
        } else if (!unlocked && Global.getSector().getPlayerFaction().knowsHullMod(PROVENANCE_HULLMOD_ID)) {
            Global.getSector().getPlayerFaction().removeKnownHullMod(PROVENANCE_HULLMOD_ID);
        }
    }

    public static boolean isMasterworkComplete() {
        Set<String> displayed = getDisplayedHullKeys();
        for (ShowcaseRequirement requirement : MASTERWORK_REQUIREMENTS) {
            if (!requirement.isMet(displayed)) return false;
        }
        return true;
    }

    public static List<ShowcaseRequirement> getMasterworkRequirements() {
        List<ShowcaseRequirement> result = new ArrayList<ShowcaseRequirement>();
        Set<String> displayed = getDisplayedHullKeys();
        for (ShowcaseRequirement requirement : MASTERWORK_REQUIREMENTS) {
            result.add(requirement.withMet(requirement.isMet(displayed)));
        }
        return result;
    }

    public static String getHullModName(String hullModId) {
        HullModSpecAPI spec = getHullModSpec(hullModId);
        return spec == null ? hullModId : spec.getDisplayName();
    }

    public static boolean hullModExists(String hullModId) {
        return getHullModSpec(hullModId) != null;
    }

    private static HullModSpecAPI getHullModSpec(String hullModId) {
        if (hullModId == null || hullModId.length() <= 0 || Global.getSettings() == null) return null;
        try {
            return Global.getSettings().getHullModSpec(hullModId);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Set<String> getDisplayedHullKeys() {
        Set<String> result = new LinkedHashSet<String>();
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) return result;

        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (!isFunctionalTrophyMarket(market) || !market.hasSubmarket(ShipTrophyRoomIds.SUBMARKET)) continue;
            if (market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getCargo() == null
                    || market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getCargo().getMothballedShips() == null) {
                continue;
            }

            for (FleetMemberAPI member : market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getCargo().getMothballedShips().getMembersListCopy()) {
                if (member == null || member.isFighterWing()) continue;
                addKey(result, member.getHullId());
                addKey(result, TrophyNetwork.getBaseHullId(member));
                if (member.getHullSpec() != null) {
                    addKey(result, member.getHullSpec().getHullId());
                    addKey(result, member.getHullSpec().getBaseHullId());
                }
            }
        }
        return result;
    }

    private static void addKey(Set<String> keys, String value) {
        if (value == null) return;
        value = value.trim().toLowerCase();
        if (value.length() > 0) keys.add(value);
    }

    private static MarketAPI findMarketById(String id) {
        if (id == null || id.length() <= 0 || Global.getSector() == null || Global.getSector().getEconomy() == null) return null;
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {
            if (market != null && id.equals(market.getId())) return market;
        }
        return null;
    }

    private static boolean isFunctionalTrophyMarket(MarketAPI market) {
        if (market == null || !market.isPlayerOwned()) return false;
        Industry industry = market.getIndustry(ShipTrophyRoomIds.INDUSTRY);
        return TrophyRoomIndustry.isFunctionalTrophyRoom(industry);
    }

    private static PersonAPI findPersonOnMarket(MarketAPI market) {
        if (market == null) return null;
        for (PersonAPI person : market.getPeopleCopy()) {
            if (person != null && PERSON_ID.equals(person.getId())) return person;
        }
        return null;
    }

    private static boolean marketHasPerson(MarketAPI market, PersonAPI person) {
        if (market == null || person == null) return false;
        for (PersonAPI existing : market.getPeopleCopy()) {
            if (existing == person) return true;
            if (existing != null && PERSON_ID.equals(existing.getId())) return true;
        }
        return false;
    }

    private static void removeFromMarketAndComms(MarketAPI market, PersonAPI person) {
        if (market == null || person == null) return;
        if (marketHasPerson(market, person)) market.removePerson(person);
        if (market.getCommDirectory() != null && market.getCommDirectory().getEntryForPerson(PERSON_ID) != null) {
            market.getCommDirectory().removePerson(person);
        }
    }

    public static class ShowcaseRequirement {
        public final String displayName;
        public final String hullId;
        public final String baseHullId;
        public final boolean met;

        public ShowcaseRequirement(String displayName, String hullId, String baseHullId) {
            this(displayName, hullId, baseHullId, false);
        }

        private ShowcaseRequirement(String displayName, String hullId, String baseHullId, boolean met) {
            this.displayName = displayName;
            this.hullId = hullId == null ? "" : hullId.toLowerCase();
            this.baseHullId = baseHullId == null ? "" : baseHullId.toLowerCase();
            this.met = met;
        }

        public ShowcaseRequirement withMet(boolean met) {
            return new ShowcaseRequirement(displayName, hullId, baseHullId, met);
        }

        public boolean isMet(Set<String> displayed) {
            if (displayed == null) return false;
            if (hullId.length() > 0 && displayed.contains(hullId)) return true;
            return baseHullId.length() > 0 && displayed.contains(baseHullId);
        }
    }
}
