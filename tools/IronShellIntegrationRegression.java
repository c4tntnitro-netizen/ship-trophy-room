package shiptrophy;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.Misc;

/** Checks the shipped CSV and JAR together, including Isa's actual menu gate. */
public final class IronShellIntegrationRegression {
    interface Call { Object invoke(String name, Object[] args); }

    static <T> T mock(Class<T> type, Call call) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
            if (method.getName().equals("equals")) return proxy == args[0];
            Object result = call.invoke(method.getName(), args);
            if (result != null) return result;
            if (method.getReturnType() == boolean.class) return false;
            if (method.getReturnType() == int.class) return 0;
            if (method.getReturnType() == float.class) return 0f;
            return null;
        }));
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        SettingsAPI previousSettings = Global.getSettings();
        JSONArray rows = new JSONArray(new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8));
        Set<String> enabled = new HashSet<>();
        ModManagerAPI mods = mock(ModManagerAPI.class, (name, values) ->
                name.equals("isModEnabled") ? enabled.contains(values[0]) : null);
        HullModSpecAPI doctrine = mock(HullModSpecAPI.class, (name, values) ->
                name.equals("getId") ? "ship_trophy_iron_shell_drill" : null);
        Global.setSettings(mock(SettingsAPI.class, (name, values) -> {
            if (name.equals("getModManager")) return mods;
            if (name.equals("getMergedSpreadsheetData")) return rows;
            if (name.equals("getHullModSpec") && "ship_trophy_iron_shell_drill".equals(values[0])) return doctrine;
            return null;
        }));
        URL[] urls = {Paths.get(args[0]).toUri().toURL()};
        try (URLClassLoader loader = new URLClassLoader(urls, IronShellIntegrationRegression.class.getClassLoader())) {
            Class<?> registry = Class.forName("shiptrophy.TrophySubtypeRegistry", true, loader);
            Object subtype = registry.getMethod("getSubtype", String.class).invoke(null, "iron_shell");
            check(subtype != null, "The shipped CSV must include the Iron Shell doctrine");
            CommandPlugin command = (CommandPlugin) Class.forName("shiptrophy.IsaContactRulesCMD", true, loader)
                    .getDeclaredConstructor().newInstance();
            Map<String, MemoryAPI> memory = new HashMap<>();
            memory.put(MemKeys.LOCAL, mock(MemoryAPI.class, (name, values) -> null));
            InteractionDialogAPI dialog = mock(InteractionDialogAPI.class, (name, values) -> null);
            for (String modId : new String[] {"selkie_eis", "timid_xiv"}) {
                enabled.clear();
                enabled.add(modId);
                check((Boolean) subtype.getClass().getMethod("isActive").invoke(subtype), modId + " must activate Discipline");
                check(command.execute("integration-regression", dialog, Misc.tokenize("hasSubtypePrograms modded"), memory),
                        modId + " must expose Isa's modded faction and subtype menu");
                ModSpecAPI source = mock(ModSpecAPI.class, (name, values) -> name.equals("getId") ? modId : null);
                ShipHullSpecAPI hull = mock(ShipHullSpecAPI.class, (name, values) -> {
                    if (name.equals("getSourceMod")) return source;
                    if (name.equals("getHullId") || name.equals("getBaseHullId")) return "custom_hull";
                    if (name.equals("getManufacturer")) return "Custom";
                    return null;
                });
                Class<?> discipline = Class.forName("shiptrophy.hullmods.IronShellDiscipline", true, loader);
                check((Boolean) discipline.getMethod("isIronShellHull", ShipHullSpecAPI.class).invoke(null, hull),
                        modId + " source hulls must qualify without relying on legacy hull names");
            }
            enabled.clear();
            check(!(Boolean) subtype.getClass().getMethod("isActive").invoke(subtype), "Discipline must be inactive without either mod");
            check(!command.execute("integration-regression", dialog, Misc.tokenize("hasSubtypePrograms modded"), memory),
                    "No installed supported integration must not expose the modded subtype menu");
        } finally {
            Global.setSettings(previousSettings);
        }
        System.out.println("PASS: Eternal and original Iron Shell each activate Discipline, expose Isa's modded subtype menu and qualify their source hulls; neither installed keeps the program hidden.");
    }
}
