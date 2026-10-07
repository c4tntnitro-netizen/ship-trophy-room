package shiptrophy.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lazywizard.console.BaseCommand.CommandResult;
import org.lazywizard.console.Console;
import shiptrophy.ShipTrophyRoomIds;

/** Runs the real command and HoT roster helper without an interactive console. */
public final class AddIsaRecruitmentRegression {
    interface Call { Object invoke(String name, Object[] args); }
    static <T> T mock(Class<T> type, Call call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
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
        final Map<String, Object> saved = new HashMap<>();
        final List<OfficerDataAPI> officers = new ArrayList<>();
        boolean fleetAvailable = true;
        int additions;
        final PersonAPI isa = mock(PersonAPI.class, (name, args) ->
                name.equals("getId") ? "ship_trophy_isa" : null);

        Fixture() {
            MemoryAPI memory = mock(MemoryAPI.class, (name, args) -> {
                if (name.equals("get")) return saved.get(args[0]);
                if (name.equals("getBoolean")) return Boolean.TRUE.equals(saved.get(args[0]));
                if (name.equals("set")) saved.put((String) args[0], args[1]);
                return null;
            });
            FleetDataAPI roster = mock(FleetDataAPI.class, (name, args) -> {
                if (name.equals("getOfficersCopy")) return new ArrayList<>(officers);
                if (name.equals("addOfficer")) {
                    check(args[0] == isa, "Use the existing HoT person");
                    officers.add(mock(OfficerDataAPI.class, (method, values) -> method.equals("getPerson") ? isa : null));
                    additions++;
                }
                return null;
            });
            CampaignFleetAPI fleet = mock(CampaignFleetAPI.class, (name, args) -> name.equals("getFleetData") ? roster : null);
            ImportantPeopleAPI people = mock(ImportantPeopleAPI.class, (name, args) -> name.equals("getPerson") ? isa : null);
            Global.setSector(mock(SectorAPI.class, (name, args) -> {
                if (name.equals("getPlayerFleet")) return fleetAvailable ? fleet : null;
                if (name.equals("getMemoryWithoutUpdate")) return memory;
                if (name.equals("getImportantPeople")) return people;
                return null;
            }));
        }

        void recruited() {
            check(saved.size() == 1 && Boolean.TRUE.equals(saved.get(ShipTrophyRoomIds.MEMORY_ISA_OFFICER_GRANTED)),
                    "Only the durable recruitment flag changes; no quest completion is synthesized");
            check(officers.size() == 1 && additions == 1, "Isa is added exactly once");
        }
    }

    public static void main(String[] args) throws Exception {
        SectorAPI previousSector = Global.getSector();
        SettingsAPI previousSettings = Global.getSettings();
        try {
            ModManagerAPI mods = mock(ModManagerAPI.class, (name, values) -> null);
            Global.setSettings(mock(SettingsAPI.class, (name, values) -> {
                if (name.equals("getModManager")) return mods;
                if (name.equals("getString")) return "isa_first_name".equals(values[1]) ? "Isa" : "Leicester";
                if (name.equals("getSpriteName")) return "graphics/portraits/ship_trophy_isa.png";
                return null;
            }));
            // Console's interactive initialization normally creates its output buffer.
            Field output = Console.class.getDeclaredField("output");
            output.setAccessible(true);
            output.set(null, new StringBuilder());
            AddIsa command = new AddIsa();
            Fixture fresh = new Fixture();
            check(command.runCommand("", CommandContext.CAMPAIGN_MAP) == CommandResult.SUCCESS, "New addition succeeds");
            fresh.recruited();
            fresh.saved.clear();
            check(command.runCommand("", CommandContext.CAMPAIGN_MAP) == CommandResult.SUCCESS, "Existing officer repairs a missing recruitment flag");
            fresh.recruited();
            check(command.runCommand("", CommandContext.CAMPAIGN_MAP) == CommandResult.SUCCESS, "Repeating the command is idempotent");
            fresh.recruited();

            Fixture invalid = new Fixture();
            check(command.runCommand("unexpected", CommandContext.CAMPAIGN_MAP) == CommandResult.BAD_SYNTAX, "Reject unexpected arguments");
            check(command.runCommand("", CommandContext.MAIN_MENU) == CommandResult.WRONG_CONTEXT, "Reject non-campaign use");
            invalid.fleetAvailable = false;
            check(command.runCommand("", CommandContext.CAMPAIGN_MAP) == CommandResult.ERROR, "Unavailable fleet fails");
            check(invalid.saved.isEmpty() && invalid.officers.isEmpty(), "Failed commands leave flags and roster untouched");
            Global.setSector(null);
            check(command.runCommand("", CommandContext.CAMPAIGN_MAP) == CommandResult.ERROR, "Missing campaign fails safely");
        } finally {
            Global.setSector(previousSector);
            Global.setSettings(previousSettings);
        }
        System.out.println("PASS: AddIsa sets/repairs the recruitment flag, adds no duplicates and advances no other quest flags; rejected commands leave state untouched.");
    }
}
