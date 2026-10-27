package shiptrophy;

import java.awt.Color;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.RuleBasedDialog;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.VisualPanelAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;

/** Executes the contact close scripts with the real Starsector rule commands. */
public final class IsaContactNavigationRegression {
    private static final String INTEL_DIALOG = "$shipTrophyIsaIntelDialog";

    interface Call { Object invoke(String name, Object[] args); }

    static <T> T mock(Class<T> type, Call call, Class<?>... otherTypes) {
        Class<?>[] types = new Class<?>[otherTypes.length + 1];
        types[0] = type;
        System.arraycopy(otherTypes, 0, types, 1, otherTypes.length);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), types, (proxy, method, args) -> {
            if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
            if (method.getName().equals("equals")) return proxy == args[0];
            Object result = call.invoke(method.getName(), args);
            if (result != null) return result;
            Class<?> returns = method.getReturnType();
            if (returns == boolean.class) return false;
            if (returns == int.class) return 0;
            if (returns == float.class) return 0f;
            if (returns == long.class) return 0L;
            if (returns == double.class) return 0d;
            return null;
        }));
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Fixture {
        final Map<String, Object> personValues = new HashMap<>();
        final Map<String, MemoryAPI> memoryMap = new HashMap<>();
        final List<String> firedTriggers = new ArrayList<>();
        PersonAPI activePerson;
        boolean portrait = true;
        boolean secondPerson = true;
        boolean thirdPerson = true;
        boolean defaultVisual;
        boolean dismissed;
        int personUpdates;
        final InteractionDialogAPI dialog;

        Fixture(boolean intel) {
            if (intel) personValues.put(INTEL_DIALOG, true);
            MemoryAPI personMemory = memory(personValues);
            MemoryAPI entityMemory = memory(new HashMap<String, Object>());
            memoryMap.put(MemKeys.LOCAL, personMemory);
            memoryMap.put(MemKeys.ENTITY, entityMemory);
            activePerson = mock(PersonAPI.class, (name, args) -> name.equals("getMemory") ? personMemory : null);
            PlanetAPI target = mock(PlanetAPI.class, (name, args) -> {
                if (name.equals("getActivePerson")) return activePerson;
                if (name.equals("setActivePerson")) activePerson = (PersonAPI) args[0];
                return null;
            });
            VisualPanelAPI visual = mock(VisualPanelAPI.class, (name, args) -> {
                if (name.equals("hideFirstPerson")) portrait = false;
                if (name.equals("hideSecondPerson")) secondPerson = false;
                if (name.equals("hideThirdPerson")) thirdPerson = false;
                if (name.equals("showPlanetInfo")) defaultVisual = true;
                return null;
            });
            InteractionDialogPlugin plugin = mock(InteractionDialogPlugin.class, (name, args) -> {
                if (name.equals("notifyActivePersonChanged")) {
                    personUpdates++;
                    // Match the engine: ending a conversation changes LOCAL memory.
                    memoryMap.put(MemKeys.LOCAL, entityMemory);
                    memoryMap.remove(MemKeys.ENTITY);
                }
                return null;
            }, RuleBasedDialog.class);
            dialog = mock(InteractionDialogAPI.class, (name, args) -> {
                if (name.equals("getInteractionTarget")) return target;
                if (name.equals("getVisualPanel")) return visual;
                if (name.equals("getPlugin")) return plugin;
                if (name.equals("dismiss")) dismissed = true;
                return null;
            });
            RulesAPI rules = mock(RulesAPI.class, (name, args) -> {
                if (name.equals("getAllMatching")) {
                    firedTriggers.add((String) args[1]);
                    return new ArrayList<>();
                }
                return null;
            });
            Global.setSector(mock(SectorAPI.class, (name, args) -> name.equals("getRules") ? rules : null));
        }

        void run(String script) throws Exception {
            for (String line : script.split("\\r?\\n")) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+", 2);
                if (parts[0].equals("IsaContactRulesCMD") && parts.length == 2
                        && parts[1].equals("showContactPortrait")) {
                    // Mock only portrait creation; execute the migration and close commands below.
                    portrait = true;
                    continue;
                }
                CommandPlugin command = (CommandPlugin) Class.forName(
                        "com.fs.starfarer.api.impl.campaign.rulecmd." + parts[0]).getDeclaredConstructor().newInstance();
                check(command.execute("isa-contact-regression", dialog,
                        Misc.tokenize(parts.length == 1 ? "" : parts[1]), memoryMap),
                        "Native close command succeeds: " + parts[0]);
            }
        }

        void cleanContact() {
            check(!portrait && !secondPerson && !thirdPerson, "Closing the contact hides every person portrait");
            check(activePerson == null && personUpdates == 1, "Closing clears the active contact and updates dialog memory once");
        }
    }

    static MemoryAPI memory(Map<String, Object> values) {
        return mock(MemoryAPI.class, (name, args) -> {
            if (name.equals("getBoolean")) return Boolean.TRUE.equals(values.get(args[0]));
            if (name.equals("contains")) return values.containsKey(args[0]);
            if (name.equals("get")) return values.get(args[0]);
            if (name.equals("set")) values.put((String) args[0], args[1]);
            if (name.equals("unset")) values.remove(args[0]);
            return null;
        });
    }

    public static void main(String[] args) throws Exception {
        JSONObject fixture = new JSONObject(new String(Files.readAllBytes(Paths.get(args[0])), StandardCharsets.UTF_8));
        SectorAPI previousSector = Global.getSector();
        SettingsAPI previousSettings = Global.getSettings();
        final boolean[] planet3d = {false};
        try {
            Global.setSettings(mock(SettingsAPI.class, (name, values) -> {
                if (name.equals("getColor")) return Color.WHITE;
                if (name.equals("getBoolean") && values[0].equals("3dPlanetBGInInteractionDialog")) return planet3d[0];
                return null;
            }));
            String backTarget = null;
            for (String option : fixture.getString("masterworkOptions").split("\\r?\\n")) {
                String[] parts = option.split(":", 3);
                if (parts.length == 3 && parts[2].equals("Back.")) backTarget = parts[1];
            }
            check(backTarget != null, "The incomplete masterwork provides a Back option");
            check(fixture.getString("mainConditions").equals("$option == " + backTarget),
                    "The incomplete masterwork Back option selects the main Hall menu");

            for (boolean threeD : new boolean[] {false, true}) {
                planet3d[0] = threeD;
                Fixture market = new Fixture(false);
                market.run(fixture.getString("marketClose"));
                market.cleanContact();
                check(market.defaultVisual == !threeD, "Market visual is restored according to the 3D planet setting");
                check(!market.dismissed && market.firedTriggers.contains("PopulateOptions"),
                        "A market contact returns to the market options without closing the dialog");
            }

            Fixture intel = new Fixture(true);
            intel.run(fixture.getString("intelClose"));
            intel.cleanContact();
            check(intel.dismissed && intel.firedTriggers.isEmpty(), "An Intel contact closes its overlay without opening market options");
            check(!intel.personValues.containsKey(INTEL_DIALOG), "Intel close clears the remote marker from the person's memory");
            for (String entry : new String[] {"contactEntry", "officerContactEntry"}) {
                Fixture oldSave = new Fixture(true);
                oldSave.run(fixture.getString(entry));
                check(!oldSave.personValues.containsKey(INTEL_DIALOG),
                        "Normal market contact repairs the stale Intel marker in an older save: " + entry);
                oldSave.run(fixture.getString("marketClose"));
                oldSave.cleanContact();
                check(!oldSave.dismissed && oldSave.firedTriggers.contains("PopulateOptions"),
                        "An older save's repaired market contact returns to market options: " + entry);
            }
        } finally {
            Global.setSector(previousSector);
            Global.setSettings(previousSettings);
        }
        System.out.println("PASS: masterwork Back reaches the Hall menu; market close restores visuals in 2D/3D; Intel close clears portraits, active person and remote state; normal market contacts repair stale remote state in older saves.");
    }
}
