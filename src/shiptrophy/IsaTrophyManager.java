package shiptrophy;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.loading.HullModSpecAPI;

public class IsaTrophyManager {
    public static final String PERSON_ID = "ship_trophy_isa";
    public static final String PORTRAIT = "graphics/portraits/ship_trophy_isa.png";
    public static final String PROVENANCE_HULLMOD_ID = "ship_trophy_isa_provenance";

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

    public static PersonAPI getOrCreateIsa(MarketAPI market) {
        if (Global.getSector() == null) return null;

        PersonAPI person = Global.getSector().getImportantPeople().getPerson(PERSON_ID);
        if (person == null) person = findPersonOnMarket(market);
        if (person == null) {
            person = Global.getFactory().createPerson();
            person.setId(PERSON_ID);
            person.setName(new FullName("Isa", "", FullName.Gender.FEMALE));
            person.setGender(FullName.Gender.FEMALE);
            person.setFaction(Factions.PLAYER);
            person.setRankId(Ranks.SPACE_CHIEF);
            person.setPostId(Ranks.POST_NANOFORGE_ENGINEER);
            person.setPersonality(Personalities.STEADY);
            person.setImportance(PersonImportance.LOW);
            person.setPortraitSprite(PORTRAIT);
            person.addTag(Tags.CONTACT_SCIENCE);
            person.addTag(Tags.CONTACT_TRADE);
            Global.getSector().getImportantPeople().addPerson(person);
        }

        if (market != null) {
            person.setMarket(market);
            if (!marketHasPerson(market, person)) market.addPerson(person);
            if (market.getCommDirectory() != null && market.getCommDirectory().getEntryForPerson(PERSON_ID) == null) {
                market.getCommDirectory().addPerson(person);
            }
        }
        return person;
    }

    public static void ensureContact(MarketAPI market, TextPanelAPI text) {
        if (Global.getSector() == null || market == null) return;
        PersonAPI person = getOrCreateIsa(market);
        if (person == null) return;

        for (Object item : Global.getSector().getIntelManager().getIntel(ContactIntel.class)) {
            ContactIntel intel = (ContactIntel) item;
            if (intel.getPerson() != null && PERSON_ID.equals(intel.getPerson().getId())) {
                intel.setState(ContactState.NON_PRIORITY);
                intel.ensureIsAddedToMarket();
                return;
            }
        }

        ContactIntel intel = new ContactIntel(person, market);
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
        boolean unlocked = isMasterworkComplete() && hullModExists(PROVENANCE_HULLMOD_ID);
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
