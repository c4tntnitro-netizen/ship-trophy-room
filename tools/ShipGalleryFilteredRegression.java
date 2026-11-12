package shiptrophy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.lazywizard.lazylib.JSONUtils.CommonDataJSONObject;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModManagerAPI;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;

/** Runs actual production and LunaLib classes behind Starsector's real script filter. */
public final class ShipGalleryFilteredRegression {
    private static final String GALLERY_SETTING = "shipTrophyShipGalleryEnabled";

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

    private static final class ErrorCounter extends AppenderSkeleton {
        int errors;
        protected void append(LoggingEvent event) {
            if (event.getLevel().isGreaterOrEqual(Level.ERROR)) errors++;
        }
        public boolean requiresLayout() { return false; }
        public void close() {}
    }

    private static final class Fixture implements AutoCloseable {
        Boolean setting = false;
        boolean lunaEnabled;
        boolean hasTarget = true;
        boolean hasMarket = true;
        boolean playerOwned = true;
        boolean hasHall = true;
        boolean building;
        boolean functional = true;
        PersonAPI activePerson;
        int galleryOpens;
        final URLClassLoader modLoader;
        final InteractionDialogAPI dialog;
        final CommandPlugin command;
        final Class<?> features;

        Fixture(String modJar, String lunaJar, boolean includeLuna) throws Exception {
            // Match ScriptStore's parent chain. The harness stays outside the filter.
            Class<?> filterClass;
            try {
                filterClass = Class.forName("com.fs.starfarer.loading.scripts.ScriptClassLoader");
            } catch (ClassNotFoundException olderEngine) {
                filterClass = Class.forName("com.fs.starfarer.loading.scripts.B");
            }
            ClassLoader filter = (ClassLoader) filterClass
                    .getConstructor(ClassLoader.class).newInstance(getClass().getClassLoader());
            URL[] urls = includeLuna
                    ? new URL[] {Paths.get(modJar).toUri().toURL(), Paths.get(lunaJar).toUri().toURL()}
                    : new URL[] {Paths.get(modJar).toUri().toURL()};
            modLoader = new URLClassLoader(urls, filter);
            ModManagerAPI mods = mock(ModManagerAPI.class, (name, args) ->
                    name.equals("isModEnabled") && "lunalib".equals(args[0]) ? lunaEnabled : null);
            Global.setSettings(mock(SettingsAPI.class, (name, args) -> {
                if (name.equals("getBoolean") && GALLERY_SETTING.equals(args[0])) {
                    if (setting == null) throw new IllegalArgumentException("Missing setting");
                    return setting;
                }
                if (name.equals("getBoolean")) return true;
                if (name.equals("getFloat") && "shipTrophyIsaCrewPayReductionPercent".equals(args[0])) return 12.5f;
                if (name.equals("getModManager")) return mods;
                if (name.equals("getScriptClassLoader")) return modLoader;
                return null;
            }));
            Industry hall = mock(Industry.class, (name, args) -> {
                if (name.equals("isBuilding")) return building;
                if (name.equals("isFunctional")) return functional;
                return null;
            });
            MarketAPI market = mock(MarketAPI.class, (name, args) -> {
                if (name.equals("isPlayerOwned")) return playerOwned;
                if (name.equals("getIndustry")) {
                    check("ship_trophy_room".equals(args[0]), "Only the Hall industry grants gallery access");
                    return hasHall ? hall : null;
                }
                return null;
            });
            SectorEntityToken target = mock(SectorEntityToken.class, (name, args) -> {
                if (name.equals("getActivePerson")) return activePerson;
                if (name.equals("getMarket")) return hasMarket ? market : null;
                return null;
            });
            dialog = mock(InteractionDialogAPI.class, (name, args) -> {
                if (name.equals("getInteractionTarget")) return hasTarget ? target : null;
                if (name.equals("showCustomDialog")) galleryOpens++;
                return null;
            });
            features = Class.forName("shiptrophy.HallOfTriumphFeatures", true, modLoader);
            command = (CommandPlugin) Class.forName("shiptrophy.ShipGalleryRulesCMD", true, modLoader)
                    .getDeclaredConstructor().newInstance();
            check(features.getClassLoader() == modLoader, "Production helper must run inside the game filter");
        }
        Object feature(String name) throws Exception {
            return features.getMethod(name).invoke(null);
        }
        boolean enabled() throws Exception {
            return (Boolean) feature("isShipGalleryEnabled");
        }
        boolean run(String action) {
            return command.execute("ship-gallery-feature-regression", dialog,
                    Misc.tokenize(action), Collections.emptyMap());
        }
        @SuppressWarnings("unchecked")
        CommonDataJSONObject loadLunaData(String savedSettings) throws Exception {
            JSONObject saved = Files.exists(Paths.get(savedSettings))
                    ? new JSONObject(new String(Files.readAllBytes(Paths.get(savedSettings)), StandardCharsets.UTF_8))
                    : new JSONObject();
            CommonDataJSONObject data = new CommonDataJSONObject("regression-only-never-saved.json");
            for (Iterator<?> keys = saved.keys(); keys.hasNext();) {
                String key = (String) keys.next();
                data.put(key, saved.get(key));
            }
            Class<?> loader = Class.forName("lunalib.backend.ui.settings.LunaSettingsLoader", true, modLoader);
            loader.getMethod("setHasLoaded", boolean.class).invoke(loader.getField("INSTANCE").get(null), true);
            ((Map<String, CommonDataJSONObject>) loader.getMethod("getSettings").invoke(null)).put("ship_trophy_room", data);
            return data;
        }
        Object actualLunaValue() throws Exception {
            Class<?> luna = Class.forName("lunalib.lunaSettings.LunaSettings", true, modLoader);
            check(luna.getClassLoader() == modLoader, "The actual LunaLib JAR must load inside the game filter");
            return luna.getMethod("getBoolean", String.class, String.class)
                    .invoke(null, "ship_trophy_room", GALLERY_SETTING);
        }
        public void close() throws Exception { modLoader.close(); }
    }

    private static void initializeEngineFilter() throws Exception {
        // The real filter checks devMode; a standalone JVM has no loaded engine config.
        for (Field field : Class.forName("com.fs.starfarer.settings.StarfarerSettings").getDeclaredFields()) {
            if (field.getType() == JSONObject.class && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                if (field.get(null) == null) field.set(null, new JSONObject().put("devMode", false));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SettingsAPI previousSettings = Global.getSettings();
        initializeEngineFilter();
        try {
            try (Fixture fixture = new Fixture(args[0], args[1], false)) {
                check(!fixture.enabled() && !fixture.run("shouldShow"), "The gallery defaults off without LunaLib");
                check(!fixture.run("show") && fixture.galleryOpens == 0, "Default-off blocks the direct open action");
                fixture.setting = null;
                check(!fixture.enabled(), "Missing settings fail closed");
                Global.setSettings(null);
                check(!fixture.enabled(), "Unavailable settings fail closed");
            }
            try (Fixture fixture = new Fixture(args[0], args[1], false)) {
                fixture.setting = true;
                check(fixture.run("shouldShow"), "settings.json true works with LunaLib absent from the classpath");
                fixture.setting = false;
                check(!fixture.run("shouldShow"), "Turning settings.json off hides the gallery immediately");
                fixture.setting = true;
                fixture.hasTarget = false;
                check(!fixture.run("shouldShow"), "No interaction target has no gallery entry");
                fixture.hasTarget = true;
                fixture.hasMarket = false;
                check(!fixture.run("shouldShow"), "No market has no gallery entry");
                fixture.hasMarket = true;
                fixture.playerOwned = false;
                check(!fixture.run("shouldShow"), "Other factions' colonies have no gallery entry");
                fixture.playerOwned = true;
                fixture.hasHall = false;
                check(!fixture.run("shouldShow"), "A colony without a Hall has no gallery entry");
                fixture.hasHall = true;
                fixture.building = true;
                check(!fixture.run("shouldShow"), "A Hall under construction has no gallery entry");
                fixture.building = false;
                fixture.functional = false;
                check(!fixture.run("shouldShow"), "A nonfunctional Hall has no gallery entry");
                fixture.functional = true;
                fixture.activePerson = mock(PersonAPI.class, (name, values) -> null);
                check(!fixture.run("shouldShow"), "The gallery is hidden in comm-directory conversations");
                fixture.activePerson = null;
                check(fixture.run("shouldShow"), "Restoring colony eligibility reveals the enabled gallery");
                fixture.lunaEnabled = true;
                check(fixture.run("shouldShow"), "A missing optional LunaLib class falls back to settings.json");
            }
            try (Fixture fixture = new Fixture(args[0], args[1], true)) {
                fixture.lunaEnabled = true;
                CommonDataJSONObject data = fixture.loadLunaData(args[2]);
                data.put(GALLERY_SETTING, true);
                check(Boolean.TRUE.equals(fixture.actualLunaValue()), "Actual LunaLib reads the saved true value");
                check(fixture.enabled() && fixture.run("shouldShow"),
                        "Actual LunaLib opt-in must reveal the gallery under Starsector's real filtered classloader");
                fixture.setting = true;
                data.put(GALLERY_SETTING, false);
                check(!fixture.enabled() && !fixture.run("shouldShow") && !fixture.run("show") && fixture.galleryOpens == 0,
                        "Actual LunaLib false overrides settings.json true and blocks the direct open action");
                data.put(GALLERY_SETTING, true);
                check(fixture.enabled() && fixture.run("shouldShow"), "Live LunaLib toggles are read without restarting");
                data.put("shipTrophyHullmodsEnabled", false);
                check(Boolean.FALSE.equals(fixture.feature("areTrophyHullmodsEnabled")), "Exposed reward toggle reads actual LunaLib false");
                data.put("shipTrophyHullmodsEnabled", true);
                check(Boolean.TRUE.equals(fixture.feature("areTrophyHullmodsEnabled")), "Exposed reward toggle reads actual LunaLib true");
                data.put("shipTrophyNexAchievementsEnabled", false);
                check(Boolean.FALSE.equals(fixture.feature("areNexAchievementsEnabled")), "Exposed achievement toggle reads actual LunaLib false");
                data.put("shipTrophyNexAchievementsEnabled", true);
                check(Boolean.TRUE.equals(fixture.feature("areNexAchievementsEnabled")), "Exposed achievement toggle reads actual LunaLib true");
                data.remove("shipTrophyIsaQualityOfLifeBonusesEnabled");
                data.remove("shipTrophyIsaCrewPayReductionPercent");
                Logger lunaLogger = Logger.getLogger("lunalib.lunaSettings.LunaSettings");
                ErrorCounter counter = new ErrorCounter();
                lunaLogger.addAppender(counter);
                try {
                    for (int call = 0; call < 5; call++) {
                        check(Boolean.TRUE.equals(fixture.feature("areIsaQualityOfLifeBonusesEnabled")), "Isa's JSON-only toggle uses settings.json");
                        check(Float.valueOf(12.5f).equals(fixture.feature("getIsaCrewPayReductionPercent")), "Isa's JSON-only float uses settings.json");
                        fixture.feature("getIsaSensorProfileReductionPercent");
                        fixture.feature("getIsaBurnLevelBonus");
                        fixture.feature("getIsaStoryPointGenerationBonusPercent");
                    }
                    check(counter.errors == 0, "JSON-only Isa fields must not query undeclared Luna settings or emit repeated errors");
                } finally { lunaLogger.removeAppender(counter); }
                data.remove(GALLERY_SETTING);
                check(fixture.enabled(), "A missing LunaLib value falls back to settings.json");
                data.put(GALLERY_SETTING, "invalid Boolean value");
                check(fixture.enabled(), "An invalid LunaLib value falls back to settings.json");
                fixture.setting = null;
                check(!fixture.enabled(), "Failure of both sources leaves the gallery disabled");
                fixture.lunaEnabled = false;
                fixture.setting = false;
                data.put(GALLERY_SETTING, true);
                check(!fixture.enabled(), "Disabled LunaLib does not override settings.json");
            }
        } finally { Global.setSettings(previousSettings); }
        System.out.println("PASS: actual LunaLib toggles work through Starsector's real filter; JSON-only Isa settings emit no Luna errors; gallery defaults off, preserves colony eligibility, blocks direct opening when off and works without LunaLib.");
    }
}
