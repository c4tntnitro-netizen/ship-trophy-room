package shiptrophy;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel.ContactState;
import com.fs.starfarer.api.campaign.StoryPointActionDelegate;
import com.fs.starfarer.api.ui.IntelUIAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

/** Isa's contact entry, with a direct action into her Hall dialogue. */
public class IsaContactIntel extends ContactIntel {
    private static final String BUTTON_CALL = "ship_trophy_isa_contact_call";
    private static final String REMOTE_CONTACT_TRIGGER = "ShipTrophyIsaRemoteContact";

    public IsaContactIntel(PersonAPI person, MarketAPI market) {
        super(person, market);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        float pad = 10f;
        PersonAPI isa = getPerson();
        if (isa != null) {
            String rank = isa.getRank() == null ? "Contact" : isa.getRank();
            String post = isa.getPost() == null ? "Nanoforge Engineer" : isa.getPost();
            TooltipMakerAPI portrait = info.beginImageWithText(
                    IsaTrophyManager.getIsaPortraitSprite(), 128f);
            portrait.addPara("Name: %s", 0f, Misc.getHighlightColor(), isa.getNameString());
            portrait.addPara("Rank: %s", 3f, Misc.getHighlightColor(), rank);
            portrait.addPara(post, 3f);
            info.addImageWithText(0f);
            info.addRelationshipBar(isa, width, pad);

            MarketAPI home = isa.getMarket();
            if (home == null) home = IsaTrophyManager.findHomeMarket();
            if (home != null && home.getFaction() != null) {
                info.addPara(
                        isa.getNameString() + " is a " + post.toLowerCase()
                                + " and can be found on " + home.getName()
                                + ", a size " + home.getSize() + " colony controlled by "
                                + home.getFaction().getDisplayName() + ".",
                        pad);
            }
        }

        info.addPara(
                "Permanent contact — Isa cannot be suspended or deleted.",
                pad,
                Misc.getHighlightColor(),
                "Permanent contact");

        String priorityLabel = getState() == ContactState.PRIORITY
                ? "Remove priority status"
                : "Make priority contact";
        info.addButton(priorityLabel, BUTTON_PRIORITY, width, 20f, pad);
        info.addButton("Contact Isa", BUTTON_CALL, width, 20f, 10f);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BUTTON_DELETE.equals(buttonId) || BUTTON_SUSPEND.equals(buttonId)) {
            return;
        }
        if (BUTTON_CALL.equals(buttonId) && Global.getSector() != null) {
            ui.showDialog(null, REMOTE_CONTACT_TRIGGER);
            return;
        }
        super.buttonPressConfirmed(buttonId, ui);
    }

    @Override
    public void storyActionConfirmed(Object buttonId, IntelUIAPI ui) {
        if (BUTTON_DELETE.equals(buttonId) || BUTTON_SUSPEND.equals(buttonId)) {
            return;
        }
        super.storyActionConfirmed(buttonId, ui);
    }

    @Override
    public boolean doesButtonHaveConfirmDialog(Object buttonId) {
        if (BUTTON_DELETE.equals(buttonId) || BUTTON_SUSPEND.equals(buttonId)) {
            return false;
        }
        return super.doesButtonHaveConfirmDialog(buttonId);
    }

    @Override
    public StoryPointActionDelegate getButtonStoryPointActionDelegate(Object buttonId) {
        if (BUTTON_DELETE.equals(buttonId) || BUTTON_SUSPEND.equals(buttonId)) {
            return null;
        }
        return super.getButtonStoryPointActionDelegate(buttonId);
    }

    @Override
    public void setState(ContactState state) {
        if (state == null
                || state == ContactState.SUSPENDED
                || state == ContactState.LOST_CONTACT
                || state == ContactState.LOST_CONTACT_DECIV) {
            state = ContactState.NON_PRIORITY;
        }
        super.setState(state);
    }

    public void ensurePermanentState() {
        setState(getState());
    }

    @Override
    public void loseContact(InteractionDialogAPI dialog) {
        setState(ContactState.NON_PRIORITY);
        ensureIsAddedToMarket();
    }

    @Override
    public boolean shouldRemoveIntel() {
        return false;
    }
}
