// Ship Trophy Room - Isa dialogue working draft
// Edit this file when rewriting Isa. The # hook tags map each section back
// to Java/rules.csv. Text in {braces} is dynamic game data.

VAR market_name = "the colony"
VAR functional_rooms = 0
VAR unique_hull_types = 0
VAR unique_deployment_points = 0
VAR current_dp = 0
VAR unlock_dp = 0
VAR remaining_dp = 0
VAR subtype_showcase_name = "matching"
VAR hullmod_name = "the hullmod"
VAR masterwork_complete = false
VAR gaze_unlocked = false
VAR contempt_unlocked = false
VAR subtype_unlocked = false

-> isa_bar_prompt

=== isa_bar_prompt ===
# hook: IsaBarEvent.addPromptAndOption
A lean, orange-haired spacer with a salvage rig still dusted in machine soot watches the traffic from {market_name}'s docks. Her crew has staked out a bar table under a spread of projected hull sections.
+ [Approach the salvager watching the Trophy Room traffic.] -> isa_bar_intro

=== isa_bar_intro ===
# hook: IsaBarEvent.showIntro
"Isa," she says, offering a hand with a few old burn scars across the knuckles. "Small team, old habits. We pull ships out of places where they were meant to stay buried, then make them worth looking at again."

"Your colony's got ambition. More importantly, it has a Trophy Room. That's rare enough that my crew and I are willing to set up here, if you'll have us."

She nods toward the docks. "Keep the displays fed. I'll keep the ledgers readable, the refit notes indexed, and the really strange ideas from getting lost in the machinery."
+ ["Welcome aboard."] -> isa_bar_accept
+ [Leave her to her crew.] -> END

=== isa_bar_accept ===
# hook: IsaBarEvent.acceptIntro
# sfx: ui_contact_developed
Isa gives a brisk nod. "Good. I'll have a bench set up off the docks by morning. Ask around for Isa if you want a read on the collection."

[SYSTEM: Isa is now listed as a contact at {market_name}.]
+ [Return to the bar.] -> END

=== isa_contact_option ===
# hook: data/campaign/rules.csv shipTrophyIsaContactOption
+ [Talk about the Trophy Room ledgers.] -> isa_contact_main

=== isa_contact_main ===
# hook: IsaContactDialogPlugin.showMainMenu
[SYSTEM: Isa has the Trophy Room network up on a battered slate: {functional_rooms} functional rooms, {unique_hull_types} unique hull types, {unique_deployment_points} unique deployment points.]

"The trick is not owning ships," she says. "It's owning examples. Hulls with enough history that they teach the rest of the dockyard something."
+ [Review Isa's five-hull showcase.] -> isa_masterwork
+ [Ask about one-of-a-kind trophy hullmods.] -> isa_uniques
+ [Ask about doctrine and subtype trophy programs.] -> isa_subtypes
+ [Cut the comm link.] -> END

=== isa_masterwork ===
# hook: IsaContactDialogPlugin.showMasterwork
"For a proper capital-line provenance program, I need five anchors," Isa says. "Onslaught XIV. Paragon. Invictus. Conquest. Executor. Display them, not just park them in a fleet roster."

[STATUS: Onslaught XIV - complete or needed]
[STATUS: Paragon - complete or needed]
[STATUS: Invictus - complete or needed]
[STATUS: Conquest - complete or needed]
[STATUS: Executor - complete or needed]

{ masterwork_complete:
    "That's the set. A full spread: Domain armor gospel, high-tech cathedral work, League audacity, Diktat vanity, and the Hegemony's favorite blunt instrument."

    [SYSTEM: Unlocked: {hullmod_name}. Isa's yard certification doubles positive S-mod bonus effects from built-in hullmods.]
- else:
    "Bring me the missing hulls and I'll certify the program. The mark lets my team re-tune a ship around its built-in modifications, but only if the display history is strong enough to justify the work."
}
+ [Back.] -> isa_contact_main
+ [Cut the comm link.] -> END

=== isa_uniques ===
# hook: IsaContactDialogPlugin.showUniques
"Some hulls are so singular they don't need a doctrine category," Isa says. "The network either has the example, or it doesn't."

[STATUS: Ziggurat display: Gaze - complete or needed]
[STATUS: Onslaught Mk.I display: Contempt - complete or needed]

{ gaze_unlocked:
    "The Ziggurat ledger is ugly in ways I don't like staring at. But if you're mounting Omega weapons, Gaze will make room for them."
- else:
    "If you ever put the Ziggurat on display, come find me. Quietly."
}

{ contempt_unlocked:
    "The Mk.I is not subtle. Good. Contempt is for ships that need to make ugly weapons fit before anyone has time to object."
- else:
    "An Onslaught Mk.I would give the yard enough threat-pattern data for something mean."
}
+ [Back.] -> isa_contact_main
+ [Cut the comm link.] -> END

=== isa_subtypes ===
# hook: IsaContactDialogPlugin.showSubtypes
"Pick a family and I'll tell you whether the displays have enough mass to teach us anything useful."

// The game generates one option per active subtype:
// + [{subtype_display_name} ({current_dp}/{unlock_dp} DP)] -> isa_subtype_detail
+ [Back.] -> isa_contact_main
+ [Cut the comm link.] -> END

=== isa_subtype_detail ===
# hook: IsaContactDialogPlugin.showSubtype
// The importer inserts one subtype comment from isa_subtype_comment before this status block.

[SYSTEM: Showcase progress: {current_dp} / {unlock_dp} DP worth of {subtype_showcase_name} ships.]

{ subtype_unlocked:
    "That's enough. I can sign off on {hullmod_name}," Isa says. "Check your refit crews; the spec is in the system."
- else:
    "Not enough examples yet. Bring me roughly {remaining_dp} more DP worth and the pattern should stop lying to us."
}
+ [Back.] -> isa_contact_main
+ [Cut the comm link.] -> END

=== isa_subtype_comment ===
# hook: IsaContactDialogPlugin.getSubtypeComment
// The game chooses one of these by subtype id. Inky preview falls back to the default sample.
-> isa_subtype_default

=== isa_subtype_xiv ===
# subtype: xiv
"XIV hulls are doctrine with rivets," Isa says. "Crude, stubborn, and very hard to argue with once the armor starts moving."
-> DONE

=== isa_subtype_lp ===
# subtype: lp
"Path ships are warnings with engines. Dangerous lesson set, but useful if you respect how much they are willing to burn."
-> DONE

=== isa_subtype_lg ===
# subtype: lg
"Lion's Guard work is parade paint over surprisingly focused energy tuning. Ignore the gold leaf; read the capacitors."
-> DONE

=== isa_subtype_tt ===
# subtype: tt
"Tri-Tachyon legacy hulls hide their best tricks in the absence of obvious machinery. Clean baffling, clean lies."
-> DONE

=== isa_subtype_default ===
# subtype: default
"This family has its own habits," Isa says. "Get enough examples in one network and the repeated choices start showing through."
-> DONE
