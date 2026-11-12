package shiptrophy;

import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

/** Exercises the optional-mod gate without any Peerless classes on the classpath. */
public final class PeerlessOptionalRegression {
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
            if (method.getReturnType() == double.class) return 0d;
            if (method.getReturnType() == long.class) return 0L;
            return null;
        }));
    }

    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        SettingsAPI previous = Global.getSettings();
        final boolean[] enabled = {false};
        final int[] writes = {0};
        ModManagerAPI mods = mock(ModManagerAPI.class, (name, values) ->
                name.equals("isModEnabled") && "peerless_skills".equals(values[0]) ? enabled[0] : null);
        MemoryAPI local = mock(MemoryAPI.class, (name, values) -> {
            if (name.equals("set") || name.equals("unset")) writes[0]++;
            return null;
        });
        Map<String, MemoryAPI> memory = new HashMap<>();
        memory.put(MemKeys.LOCAL, local);
        InteractionDialogAPI dialog = mock(InteractionDialogAPI.class, (name, values) -> null);
        URL[] urls = {new java.io.File(args[0]).toURI().toURL()};
        try (URLClassLoader loader = new URLClassLoader(urls, PeerlessOptionalRegression.class.getClassLoader())) {
            try {
                Class.forName("peerless.rules.PeerlessSkillsCMD", false, loader);
                throw new AssertionError("This check must run without the optional mod on its classpath");
            } catch (ClassNotFoundException expected) {
                // The same runtime classpath works when Peerless is absent.
            }
            CommandPlugin command = (CommandPlugin) Class.forName("shiptrophy.IsaContactRulesCMD", true, loader)
                    .getDeclaredConstructor().newInstance();
            Global.setSettings(mock(SettingsAPI.class, (name, values) -> name.equals("getModManager") ? mods : null));
            java.util.List<Misc.Token> params = Misc.tokenize("peerlessEnabled");
            Global.setSettings(null);
            check(!command.execute("optional-peerless", dialog, params, memory),
                    "Missing settings must skip the optional award");
            Global.setSettings(mock(SettingsAPI.class, (name, values) -> null));
            check(!command.execute("optional-peerless", dialog, params, memory),
                    "Missing mod manager must skip the optional award");
            Global.setSettings(mock(SettingsAPI.class, (name, values) -> name.equals("getModManager") ? mods : null));
            check(!command.execute("optional-peerless", dialog, params, memory),
                    "An absent or disabled Peerless mod must skip the optional award");
            enabled[0] = true;
            check(command.execute("optional-peerless", dialog, params, memory),
                    "An enabled Peerless mod must allow the quest and receipt checks to proceed");
            check(writes[0] == 0, "Checking availability must not consume an award receipt or alter quest state");
        } finally {
            Global.setSettings(previous);
        }
        System.out.println("PASS: the optional Peerless gate works without Peerless classes, skips unavailable mods, permits enabled mods and leaves memory unchanged.");
    }
}
