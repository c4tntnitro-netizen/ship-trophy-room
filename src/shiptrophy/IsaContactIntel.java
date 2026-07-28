package shiptrophy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

/**
 * Isa's normal contact entry, with a remote call action once she joins the fleet.
 */
public class IsaContactIntel extends ContactIntel {
    private static final String BUTTON_CALL = "ship_trophy_isa_contact_call";
    private static final String REMOTE_CONTACT_TRIGGER = "ShipTrophyIsaRemoteContact";

    public IsaContactIntel(PersonAPI person, MarketAPI market) {
        super(person, market);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        super.createSmallDescription(info, width, height);
        if (IsaTrophyManager.wasOfficerGranted()) {
            info.addButton("Contact Isa", BUTTON_CALL, width, 20f, 10f);
        }
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BUTTON_CALL.equals(buttonId)
                && Global.getSector() != null
                && Global.getSector().getPlayerFleet() != null) {
            ui.showDialog(Global.getSector().getPlayerFleet(), REMOTE_CONTACT_TRIGGER);
            return;
        }
        super.buttonPressConfirmed(buttonId, ui);
    }
}
