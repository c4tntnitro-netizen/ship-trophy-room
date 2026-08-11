package shiptrophy.gallery;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

final class UiReflection {
    private UiReflection() {
    }

    static UIPanelAPI getCurrentCorePanel() {
        Object campaignUi = Global.getSector().getCampaignUI();
        Object dialog = Global.getSector().getCampaignUI().getCurrentInteractionDialog();
        Object core = dialog == null
                ? invoke(campaignUi, "getCore")
                : invoke(dialog, "getCoreUI");
        if (!(core instanceof UIPanelAPI)) return null;
        Object tab = invoke(core, "getCurrentTab");
        return tab instanceof UIPanelAPI ? (UIPanelAPI) tab : null;
    }

    @SuppressWarnings("unchecked")
    static List<UIComponentAPI> children(UIPanelAPI panel) {
        if (panel == null) return Collections.emptyList();
        Object value = invoke(panel, "getChildrenCopy");
        if (!(value instanceof List<?>)) return Collections.emptyList();
        List<UIComponentAPI> result = new ArrayList<UIComponentAPI>();
        for (Object child : (List<Object>) value) {
            if (child instanceof UIComponentAPI) result.add((UIComponentAPI) child);
        }
        return result;
    }

    static Object invoke(Object target, String name, Object... args) {
        if (target == null) return null;
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(name)
                    || method.getParameterTypes().length != args.length) continue;
            try {
                return method.invoke(target, args);
            } catch (Throwable ignored) {
            }
        }
        Class<?> type = target.getClass();
        while (type != null) {
            for (Method method : type.getDeclaredMethods()) {
                if (!method.getName().equals(name)
                        || method.getParameterTypes().length != args.length) continue;
                try {
                    method.setAccessible(true);
                    return method.invoke(target, args);
                } catch (Throwable ignored) {
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }

    static Object getField(Object target, String name) {
        if (target == null) return null;
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (Throwable ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }
}
