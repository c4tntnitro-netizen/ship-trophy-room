package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

/** Dynamic option producer for Isa's rules.csv dialogue. */
public class IsaContactOptionsCMD implements CommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;

        String command = value(params, 0, memoryMap);
        boolean modded = isModded(value(params, 1, memoryMap));
        if ("populateMasterworkOption".equals(command)) {
            IsaContactRulesCMD.populateMasterworkOption(dialog.getOptionPanel());
            return true;
        }
        if ("populateGenericUniqueOptions".equals(command)) {
            IsaContactRulesCMD.populateGenericUniqueOptions(dialog.getOptionPanel(), modded);
            return true;
        }
        if ("populateSubtypeOptions".equals(command)) {
            IsaContactRulesCMD.populateSubtypeOptions(dialog.getOptionPanel(), modded);
            return true;
        }
        return false;
    }

    private static String value(List<Token> params, int index, Map<String, MemoryAPI> memoryMap) {
        if (index < 0 || index >= params.size()) return "";
        String result = params.get(index).getString(memoryMap);
        return result == null ? "" : result;
    }

    private static boolean isModded(String value) {
        return "modded".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    @Override
    public boolean doesCommandAddOptions() {
        return true;
    }

    @Override
    public int getOptionOrder(List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if ("populateMasterworkOption".equals(value(params, 0, memoryMap))) return 10;
        return 0;
    }
}
