package shiptrophy;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import com.fs.starfarer.api.util.Misc;
import shiptrophy.campaign.GanEdenTransitSystemGenerator;

/** Headless checks for the comm-to-battle handoff and arrival clearance. */
public class GanEdenEncounterRegression {
    static <T> T mock(Class<T> type, BiFunction<String, Object[], Object> call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, m, a) -> {
            Object value = call.apply(m.getName(), a);
            if (value != null) return value;
            if (m.getReturnType() == boolean.class) return false;
            if (m.getReturnType() == int.class) return 0;
            if (m.getReturnType() == float.class) return 0f;
            return null;
        }));
    }
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static class FleetDialog extends FleetInteractionDialogPluginImpl {
        int reinitializations, engagements;
        @Override public void notifyActivePersonChanged() {}
        @Override public void reinit(boolean addContinue) {
            check(!addContinue, "Must not introduce another Continue button");
            reinitializations++;
        }
        @Override public void optionSelected(String text, Object data) {
            check(!inConversation, "Must leave comm conversation before engaging");
            check(data == OptionId.ENGAGE, "Must invoke vanilla Engage");
            check(reinitializations == 1, "Must restore the existing fleet context first");
            engagements++;
        }
    }
    static Object constant(String name) throws Exception {
        Field field = GanEdenTransitSystemGenerator.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }
    public static void main(String[] args) throws Exception {
        SectorAPI previous = Global.getSector();
        SettingsAPI previousSettings = Global.getSettings();
        FactoryAPI previousFactory = Global.getFactory();
        Global.setSettings(mock(SettingsAPI.class, (name, values) ->
                name.equals("getColor") ? java.awt.Color.WHITE : null));
        Global.setFactory(mock(FactoryAPI.class, (name, values) ->
                name.equals("createCargo") ? mock(CargoAPI.class, (n, v) -> null) : null));
        try {
            Map<String, Object> fleetFlags = new HashMap<String, Object>();
            MemoryAPI memory = mock(MemoryAPI.class, (name, values) -> {
                if (name.equals("set")) fleetFlags.put((String) values[0], values[1]);
                if (name.equals("unset")) fleetFlags.remove(values[0]);
                return null;
            });
            FleetDataAPI fleetData = mock(FleetDataAPI.class, (name, values) ->
                    name.equals("getMembersListCopy") ? java.util.Collections.emptyList() : null);
            CampaignFleetAPI fleet = mock(CampaignFleetAPI.class, (name, values) -> {
                if (name.equals("getMemoryWithoutUpdate")) return memory;
                if (name.equals("getFleetData")) return fleetData;
                return null;
            });
            Global.setSector(mock(SectorAPI.class, (name, values) -> {
                check(!name.equals("getMemoryWithoutUpdate"), "Battle handoff must not mutate sector quest flags");
                return name.equals("getPlayerFleet") ? fleet : null;
            }));
            FleetDialog plugin = new FleetDialog();
            VisualPanelAPI visual = mock(VisualPanelAPI.class, (name, values) -> null);
            InteractionDialogAPI dialog = mock(InteractionDialogAPI.class, (name, values) -> {
                if (name.equals("getPlugin")) return plugin;
                if (name.equals("getInteractionTarget")) return fleet;
                if (name.equals("getVisualPanel")) return visual;
                return null;
            });
            FleetInteractionDialogPluginImpl.inConversation = true;
            check(new GanEdenQuestCMD().execute("regression", dialog,
                    Misc.tokenize("beginHypershuntBattle"), new HashMap<String, MemoryAPI>()), "Battle handoff failed");
            check(plugin.engagements == 1, "One fight choice must perform exactly one Engage action");
            check(fleetFlags.containsValue(Boolean.TRUE), "Guard must be committed before battle");
            System.out.println("PASS: one fight choice restores vanilla context and engages once without another Continue.");
        } finally {
            Global.setSector(previous);
            FleetInteractionDialogPluginImpl.inConversation = false;
            Global.setSettings(previousSettings);
            Global.setFactory(previousFactory);
        }
        double angle = Math.toRadians((Float) constant("ARRIVAL_ANGLE"));
        double radius = (Float) constant("ARRIVAL_DISTANCE");
        double x = Math.cos(angle) * radius, y = Math.sin(angle) * radius;
        check(Math.hypot(x - 950, y) > 10000, "Arrival is too close to the parked Ordo");
        for (String group : new String[]{"DAMAGED_HYPERSHUNTS", "DAMAGED_GATE_HAULERS"}) {
            for (float[] ruin : (float[][]) constant(group)) {
                double r = Math.toRadians(ruin[0]);
                check(Math.hypot(x - Math.cos(r) * ruin[1], y - Math.sin(r) * ruin[1]) > 5000,
                        "Arrival is too close to a graveyard ruin");
            }
        }
        System.out.println("PASS: arrival is over 10,000 units from the Ordo and 5,000 from every ruin.");
    }
}
