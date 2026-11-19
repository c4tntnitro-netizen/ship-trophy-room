package shiptrophy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc.Token;
import java.util.List;
import java.util.Map;

/** Optional bridge: rules can load without any Peerless Skills classes present. */
public final class HallPeerlessAwardCMD extends BaseCommandPlugin {
    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog,
            List<Token> params, Map<String, MemoryAPI> memoryMap) {
        if (Global.getSettings() == null || Global.getSettings().getModManager() == null
                || !Global.getSettings().getModManager().isModEnabled("peerless_skills")) {
            return false;
        }
        try {
            CommandPlugin command = (CommandPlugin) Global.getSettings().getScriptClassLoader()
                    .loadClass("peerless.rules.PeerlessSkillsCMD").getDeclaredConstructor().newInstance();
            return command.execute(ruleId, dialog, params, memoryMap);
        } catch (ReflectiveOperationException | LinkageError failure) {
            Global.getLogger(HallPeerlessAwardCMD.class).warn("Optional Peerless reward unavailable", failure);
            return false;
        }
    }
}
