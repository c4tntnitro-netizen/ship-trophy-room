// Hall of Triumph - Isa Leicester dialogue writing aid
// data/campaign/rules.csv is the definitive writing and runtime source for Hall contact dialogue.
// This Ink file exists for human drafting and Inky preview; reconcile changes into rules.csv.
// Text in {braces} is supplied dynamically by the game.

VAR market_name = "the colony"
VAR player_title = "Captain"
VAR player_name = "Player"
VAR player_pronoun = "They"
VAR functional_rooms = 1
VAR unique_hull_types = 30
VAR unique_deployment_points = 941
VAR current_dp = 0
VAR unlock_dp = 60
VAR remaining_dp = 60
VAR subtype_showcase_name = "matching"
VAR hullmod_name = "the hullmod"

-> isa_preview_hub

=== isa_preview_hub ===
// Inky-only navigation. This menu is not shown in Starsector.
ISA DIALOGUE PREVIEW
+ First bar encounter -> isa_bar_prompt_new
+ Returning bar prompt -> isa_bar_prompt_known
+ Contact dialogue -> isa_contact_option
+ Post-recruitment contact dialogue -> isa_contact_officer_call
+ Hall completion transmission -> isa_faction_hall_complete
+ Officer recruitment -> isa_join_fleet
+ Combat Chatter pool -> isa_chatter_menu
+ End preview -> END

=== isa_bar_prompt_new ===
// hook: data/campaign/rules.csv shipTrophyIsaBarPrompt
A lean, red-haired spacer with her overalls still dusted in machine oil and soot watches the traffic from the dockside bar at {market_name}. Her crew has staked out a bar table under a spread of projected hull sections. They're in the middle of some lively debate over the feasibility of some kind of ship technology. Coaxial weaponry, by the looks of it. Her eyes glint as she sees hull after hull move in and out of the designated display berth.
+ Approach the salvager watching the Hall of Triumph traffic. -> isa_bar_intro
+ Back to preview hub. -> isa_preview_hub

=== isa_bar_prompt_known ===
// preview-only: after hiring, runtime dialogue is owned by the contact rules
Isa is at a dockside table, boots hooked around a chair leg, arguing quietly with a pair of salvagers over a rotating hull schematic from the Hall of Triumph network.
+ Talk to Isa about the Hall of Triumph ledgers. -> isa_contact_main
+ Back to preview hub. -> isa_preview_hub

=== isa_bar_intro ===
// hook: data/campaign/rules.csv shipTrophyIsaBarIntro
Her crew whispers as you approach and hurriedly clean up the blueprints. The spacer, however, turns and beams at you.

She sticks out a daintier hand than you were expecting.

"Call me Isa. Chief nanoforge architect of Angel Architectures. You're {player_title} {player_name}, right?"
+ "Welcome to {market_name}, ma'am and gentlemen." 
-> isa_intro_greeting
+ "Bright and true. Ludd's peace be on you." 
-> isa_intro_ludd
+ "So you've heard of me. Nice to know someone on this rock respects me." 
-> isa_intro_respect

=== isa_intro_greeting ===
// hook: data/campaign/rules.csv shipTrophyIsaBarGreeting
Isa does an exaggerated curtsy.

"Ma'am!" she repeats. "Hear that?" She calls over her shoulder, to the genial cheers of her men. "We're respectable citizens now!"

Isa straightens and gives your entourage an appraising look. Her gaze passes over the uniforms, the sidearms, and finally the power-armored marines at your back. You swear you can see her smile widen.
-> isa_intro_pitch

=== isa_intro_ludd ===
// hook: data/campaign/rules.csv shipTrophyIsaBarLudd


Isa places one hand over her heart and gives an exaggerated bow.

"And on you, {player_title}. May your reactors run cool, your seals hold pressure, and every unexploded missile stay that way until someone else finds it. Aaaaa-Men."

She smiles.

"Every spacer finds religion eventually. Usually right after the life-support alarms start."
-> isa_intro_pitch

=== isa_intro_respect ===
// hook: data/campaign/rules.csv shipTrophyIsaBarRespect
Isa glances at your power-armored bodyguards flanking you. 

She points.

"Respect costs extra," one of your oldest marines says.

"{player_title} don't even tip good." The other huffs.
 
+ "I hate you both." 
-> isa_intro_respect_2

== isa_intro_respect_2 ==
// hook: data/campaign/rules.csv shipTrophyIsaBarRespectTwo

"Uh huh..." Isa raises an eyebrow.
-> isa_intro_pitch

=== isa_intro_pitch ===
// hook: data/campaign/rules.csv shipTrophyIsaBarPitch
"Well, I'll cut to the chase, {player_title}." Isa looks up. "I saw your vanity project out there. Your "Hall of Triumph". All those amazing ships with hundreds of cycles of history... I want you to hire us to research those ship frames."

Isa looks out to the window wall that makes up the bar. For the first time, her eyes steady and her gaze turns far away. Another ship is floating in from dock, being towed into your museum. "It really is incredible..."
+ We've got ship engineers already. -> isa_intro_pitch_two

=== isa_intro_pitch_two ===
// hook: data/campaign/rules.csv shipTrophyIsaBarPitchTwo
"Not like us you don't." Isa grins. "We'll work on commish. Just bring my team enough examples of those specialized hullframes, and I can work our magicks on modularizing the upgrades."
+ "Holy Ludd. You can really do that?"-> isa_intro_pitch_three
+ Leave her to her crew. -> isa_preview_hub

=== isa_intro_pitch_three ===
// hook: data/campaign/rules.csv shipTrophyIsaBarPitchThree
"My team is the best in the sector. I'm the best in the entire Domain." Isa raises her fist and pulls you with her other hand into a bump. "I promise you that."
+ "Show me." -> isa_bar_accept
+ Leave her to her crew. -> isa_preview_hub

=== isa_bar_accept ===
// hook: data/campaign/rules.csv shipTrophyIsaBarAccepted
// sfx: ui_contact_developed
Isa gives a brisk nod. "Good. I'll rent an office 'round here by today. Ask around for Isa Leicester if you want a read on the collection."

SYSTEM: Isa is now listed as a contact at {market_name}.
+ Return to the bar. -> isa_preview_hub

=== isa_contact_option ===
// hook: data/campaign/rules.csv shipTrophyIsaContactOption
// before officer recruitment
+ Talk about the Hall of Triumph ledgers. -> isa_contact_main
// after officer recruitment
+ Call Isa's Hall office. -> isa_contact_officer_call
+ Back to preview hub. -> isa_preview_hub

=== isa_contact_officer_call ===
// hook: data/campaign/rules.csv shipTrophyIsaOfficerCall
The connection routes to Isa's office. Instead of a live feed, the comm screen displays her screensaver: a slow-turning wireframe of the Hall of Triumph while tiny ships glide between its berths. The office behind it is dark.
+ "Isa?" -> isa_contact_officer_answer
+ Cut the comm link. -> isa_preview_hub

=== isa_contact_officer_answer ===
// hook: data/campaign/rules.csv shipTrophyIsaOfficerAnswer
A moment later, the screensaver folds away. Isa appears on the feed from somewhere aboard your fleet, safety goggles pushed into her hair and a spanner still in one hand.

"Commander? Why are you calling my office? I'm right here with the fleet. You could've just come up and talked to me."

She glances off-screen. "Well? You wanted something?"
+ "Since we're connected, pull up the Hall ledgers." -> isa_contact_main
+ Cut the comm link. -> isa_preview_hub

=== isa_contact_main ===
// hook: data/campaign/rules.csv shipTrophyIsaContactOpen
SYSTEM: Isa has the Hall of Triumph network up on a battered slate: {functional_rooms} functional rooms, {unique_hull_types} unique hull types, {unique_deployment_points} unique deployment points.

"It's not about not owning ships," she says. "It's owning history. Hulls with enough legacy that they teach us all."
// ui: this option appears in yellow only when every active Hall ledger is complete
+ Isa is ready to join the fleet. -> isa_join_fleet
// ui: each category option appears in yellow when it contains an unlocked hullmod
+ Review Isa's masterwork. -> isa_masterwork
+ Ask about one-of-a-kind trophy hullmods. -> isa_uniques
+ Ask about doctrine and subtype trophy programs. -> isa_subtypes
+ Cut the comm link. -> isa_preview_hub

=== isa_masterwork ===
// hook: data/campaign/rules.csv shipTrophyIsaMasterworkHub
"I have an idea." Isa says. "Get me an Onslaught XIV. A Paragon. An Invictus. A Conquest. An Executor. Do that, and I can get to work."

STATUS: Complete or Needed: Onslaught XIV
STATUS: Complete or Needed: Paragon
STATUS: Complete or Needed: Invictus
STATUS: Complete or Needed: Conquest
STATUS: Complete or Needed: Executor
// ui: Complete and unlocked text is yellow; Needed text is red
+ Preview completed showcase response. -> isa_masterwork_complete
+ Preview incomplete showcase response. -> isa_masterwork_incomplete
+ Back. -> isa_contact_main

=== isa_masterwork_complete ===
// hook: data/campaign/rules.csv shipTrophyIsaMasterworkComplete
// first-time unlock scene
Isa has all five hull profiles hanging in the air around her: Onslaught XIV, Paragon, Invictus, Conquest, Executor. Lines of forge notation connect them into a web only she seems able to read.

"Would you look at that," she says, almost reverently. "Five different answers to the same question: how much ship can you build before the ship starts building you?"

+ "You found something useful, then?" -> isa_masterwork_complete_2

=== isa_masterwork_complete_2 ===
"Useful?" Isa grins. She pinches the web down to a single rotating modspec and flicks it from her terminal to yours. "Commander, this is my masterwork."

HIGHLIGHT: Received Awe modspec.

+ "Awe. Modest name." -> isa_masterwork_complete_3

=== isa_masterwork_complete_3 ===
"I considered 'Isa Leicester Was Right,'" she says. "Marketing talked me down."

She is still smiling when she turns back to the five ships.

+ Back. -> isa_masterwork
=== isa_masterwork_incomplete ===
// hook: data/campaign/rules.csv shipTrophyIsaMasterworkIncomplete
"Bring me the missing hulls and I'll get to work on my custom modspec."
+ Back. -> isa_masterwork

=== isa_uniques ===
// hook: data/campaign/rules.csv shipTrophyIsaUniquesHub and shipTrophyIsaModdedUniquesHub
"Some hulls are so singular they don't need a doctrine category," Isa says. "Either we have one, or we don't."

STATUS: Complete or Needed: Ziggurat display: Gaze
STATUS: Complete or Needed: Onslaught Mk.I display: Contempt
STATUS: Complete or Needed: Abundant Mercy display: Vow (featured even without Knights Hospitaller installed)
STATUS: Complete or Needed: The Black Lion display: Inheritance (featured even without Black Lion Ships installed)
// ui: Complete status text is yellow; Needed status text is red
+ Preview Gaze unlocked response. -> isa_unique_gaze_unlocked
+ Preview Gaze locked response. -> isa_unique_gaze_locked
+ Preview Contempt unlocked response. -> isa_unique_contempt_unlocked
+ Preview Contempt locked response. -> isa_unique_contempt_locked
+ Preview Vow unlocked response. -> isa_unique_mercy_unlocked
+ Preview Vow locked response. -> isa_unique_mercy_locked
+ Preview Inheritance unlocked response. -> isa_unique_lion_unlocked
+ Preview Inheritance locked response. -> isa_unique_lion_locked
+ Back. -> isa_contact_main

=== isa_unique_gaze_unlocked ===
// hook: data/campaign/rules.csv shipTrophyIsaGazeUnlocked
// first-time unlock scene
"This thing... Ziggurat... I don't like it. Commander, I recommend we isolate the thing. Seal off its display berth." Isa rubs her ears. "At least until I can figure out where that damn ringing is coming from inside it." Her rubbing intensifies.

+ "Take it easy, Isa." -> isa_unique_gaze_unlocked_2

=== isa_unique_gaze_unlocked_2 ===
"I know, I know." Isa waves at the hologram on her Tri-pad, and a modspec flies from her terminal to yours. "Those are my findings from that abomination."

HIGHLIGHT: Received Gaze modspec.

+ "Thanks, Chief. Take the day off." -> isa_unique_gaze_unlocked_3

=== isa_unique_gaze_unlocked_3 ===
"Thaaanks." Isa waves as she walks off.

You hear her mutter, "Some kind of structural acoustics?"

+ Back. -> isa_uniques
=== isa_unique_gaze_locked ===
// hook: data/campaign/rules.csv shipTrophyIsaGazeLocked
"If you ever find out what Tri-Tach was working on in their blacksites, come find me. Quietly."
+ Back. -> isa_uniques

=== isa_unique_contempt_unlocked ===
// hook: data/campaign/rules.csv shipTrophyIsaContemptUnlocked
// first-time unlock scene
The workshop is silent.

Isa's entire team has gathered beneath the Onslaught Mk.I's projected silhouette. Salvagers who normally argue over stripped bolts stand shoulder to shoulder with yard engineers, all staring upward. The shape is crude and strangely proportioned, but unmistakable.

Isa does not look away when you enter.

"Do you understand what you brought home?" she asks quietly. "The Onslaught. The one every other has spent thousands of cycles trying to remember."

+ "You sound like a Galactic Redeemer." -> isa_unique_contempt_unlocked_2

=== isa_unique_contempt_unlocked_2 ===
"We have our own saints." Isa finally lowers her eyes. "I've heard the Mk.I be called a myth. The Domain's navy, its forges, its gates, all hammered into one heroic hammer and sent to fight some impossible enemy in the dark."

She looks back at the ancient hull.

"Then one day the myth drifts out of the abyss."

+ "What remains?" -> isa_unique_contempt_unlocked_3

=== isa_unique_contempt_unlocked_3 ===
"Everything." The reverence in Isa's voice gives way to mounting professional excitement. She unfolds diagrams of the replaceable vambrace armor, the shieldless hull, and the electromechanical automation whispering through every major system.

"The crew kept rebuilding themselves alongside it. Wounded, augmented, replaced, until the people and the ship stopped being separate things. All that remained was a factory serial and one order repeated across the ages."

A fire-control pattern detaches from the diagrams. You recall what the ship said when you were there.

+ Threat detected. -> isa_unique_contempt_unlocked_4

=== isa_unique_contempt_unlocked_4 ===
Isa sends the pattern to your terminal. 

HIGHLIGHT: Received Contempt modspec.

+ "Contempt." -> isa_unique_contempt_unlocked_5

=== isa_unique_contempt_unlocked_5 ===
"Seemed appropriate." Isa glances toward the gathered engineers. "Give us another minute before you open the berth to visitors, Commander."

+ "Have you found what happened to the original crew?" -> isa_unique_contempt_unlocked_6

=== isa_unique_contempt_unlocked_6 ===

Isa shakes her head. "Anything... 'sentient' has long died out. There's nothing left in those drives but empty subroutines and a bunch of weights and neural nets." 

Isa puts her hand over her heart. "Rest in peace, now."

+ Back. -> isa_uniques
=== isa_unique_contempt_locked ===
// hook: data/campaign/rules.csv shipTrophyIsaContemptLocked
"Say, Cap. Let me know if you find any... legendary, near mythical ship frames from the early Domain Era out there. 'Kay?"
+ Back. -> isa_uniques

=== isa_unique_mercy_unlocked ===
// hook: data/campaign/rules.csv optional unique unlocked rules
// required mod: knights_hospitallar; hullmod: Vow
// first-time unlock scene
Isa stands beneath the Abundant Mercy's projected silhouette, unusually quiet.

"Most Invictus-patterns are built around deciding who dies," she says. "This one was built around deciding who gets another chance. Every launch rail, every damage-control route, every recovery station. It all points the same way."

+ "Can our crews reproduce it?" -> isa_unique_mercy_unlocked_2

=== isa_unique_mercy_unlocked_2 ===
"I told you, right? My team's the best in the Sec." Isa sends a pale-gold modspec to your terminal. "This is as close as I can get without asking the ship to grow a halo."

HIGHLIGHT: Received Vow modspec.

+ "That's close enough." -> isa_unique_mercy_unlocked_3

=== isa_unique_mercy_unlocked_3 ===
Isa nods toward the Mercy. "For a first draft."

+ Back. -> isa_uniques
=== isa_unique_mercy_locked ===
// hook: data/campaign/rules.csv optional unique locked rules
// required mod: knights_hospitallar; hullmod: Vow
"Keep an eye out for Abundant Mercy, one of the Knights Hospitaller's Invictus-pattern ships. If you get it into the network, I want a separate ledger for it."
+ Back. -> isa_uniques

=== isa_unique_lion_unlocked ===
// hook: data/campaign/rules.csv optional unique unlocked rules
// required mod: black_lion_ships; hullmod: Inheritance
// first-time unlock scene
The Black Lion's energy grid fills Isa's terminal in layers of gold and warning red. She peels them apart one by one, frowning harder each time.

"This ship is showing off," she says. "And the annoying part is that it has every right to. A temporal shell? On a ship that size?"

+ "High praise, coming from you." -> isa_unique_lion_unlocked_2

=== isa_unique_lion_unlocked_2 ===
"Professional recognition." Isa gathers the brightest layers into a new file and pushes it across the link. "I stripped out the vanity. Mostly."

HIGHLIGHT: Received Inheritance modspec.

+ "What did you leave in?" -> isa_unique_lion_unlocked_3

=== isa_unique_lion_unlocked_3 ===
"The part that wins arguments," she says.

+ Back. -> isa_uniques
=== isa_unique_lion_locked ===
// hook: data/campaign/rules.csv optional unique locked rules
// required mod: black_lion_ships; hullmod: Inheritance
"Keep an eye out for The Black Lion from Black Lion Ships. If you get it into the network, I want a separate ledger for it. That hull is not just another class entry."
+ Back. -> isa_uniques

=== isa_subtypes ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypesHub and shipTrophyIsaModdedSubtypesHub
"Pick a family and I'll tell you whether the displays have enough mass to teach us anything useful."

// The game generates these options from data/config/ship_trophy_room/subtypes.csv.
// ui: an option appears in yellow when its threshold is met
+ XIV Battlegroup ({current_dp}/{unlock_dp} DP) -> isa_subtype_xiv
+ Luddic Path ({current_dp}/{unlock_dp} DP) -> isa_subtype_lp
+ Lion's Guard ({current_dp}/{unlock_dp} DP) -> isa_subtype_lg
+ Tri-Tachyon ({current_dp}/{unlock_dp} DP) -> isa_subtype_tt
+ Knights of Ludd ({current_dp}/{unlock_dp} DP) -> isa_subtype_knights
+ United Aurora Federation ({current_dp}/{unlock_dp} DP) -> isa_subtype_uaf
+ Iron Shell ({current_dp}/{unlock_dp} DP) -> isa_subtype_iron_shell
+ Remnant ({current_dp}/{unlock_dp} DP) -> isa_subtype_remnant
+ Domain Derelicts ({current_dp}/{unlock_dp} DP) -> isa_subtype_domain_derelict
+ Preview inactive or missing-mod ledger. -> isa_subtype_inactive
+ Back. -> isa_contact_main

=== isa_subtype_xiv ===
// hook: data/campaign/rules.csv ShipTrophyIsaSubtypeComment rows
// subtype: xiv; hullmod: Legacy
"XIV battlegroup hulls are history stapled to big guns." Isa says. "Very hard to argue with once the armor starts moving."
~ subtype_showcase_name = "XIV Battlegroup"
~ hullmod_name = "Legacy"
-> isa_subtype_progress

=== isa_subtype_lp ===
// hook: data/campaign/rules.csv ShipTrophyIsaSubtypeComment rows
// subtype: lp; hullmod: Zeal
"Path ships are floating disasters with engines. Dangerous lesson set, but useful if you respect how much they are willing to martyr themselves."
~ subtype_showcase_name = "Luddic Path"
~ hullmod_name = "Zeal"
-> isa_subtype_progress

=== isa_subtype_lg ===
// hook: data/campaign/rules.csv ShipTrophyIsaSubtypeComment rows
// subtype: lg; hullmod: Pageantry
"Lion's Guard work is parade paint over surprisingly decent forge-level tuning. Ignoring the garbage wiring and that man-mulcher they call the 'Energy Bolt Coherer', it's got a surprisingly pratical layout. For some of the patterns, anways."
~ subtype_showcase_name = "Lion's Guard"
~ hullmod_name = "Pageantry"
-> isa_subtype_progress

=== isa_subtype_tt ===
// hook: data/campaign/rules.csv ShipTrophyIsaSubtypeComment rows
// subtype: tt; hullmod: Optimization
"Tri-Tachyon legacy hulls hide their best tricks in the absence of obvious machinery. Clean baffling, clean lies."
~ subtype_showcase_name = "Tri-Tachyon"
~ hullmod_name = "Optimization"
-> isa_subtype_progress

=== isa_subtype_knights ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeCommentGeneric
// subtype: knights_of_ludd; required mod: knights_of_ludd
// hullmod: Knights of Ludd Trophy Benediction
"This family has its own habits," Isa says. "Get enough examples into our network and the design patterns start showing through."
~ subtype_showcase_name = "Knights of Ludd"
~ hullmod_name = "Knights of Ludd Trophy Benediction"
-> isa_subtype_progress

=== isa_subtype_uaf ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeCommentGeneric
// subtype: uaf; required mod: uaf; hullmod: Resonance
"This family has its own habits," Isa says. "Get enough examples into our network and the design patterns start showing through."
~ subtype_showcase_name = "Auroran"
~ hullmod_name = "Resonance"
-> isa_subtype_progress

=== isa_subtype_iron_shell ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeCommentGeneric
// subtype: iron_shell; required mod: timid_xiv
// hullmod: Discipline
"This family has its own habits," Isa says. "Get enough examples into our network and the design patterns start showing through."
~ subtype_showcase_name = "Iron Shell"
~ hullmod_name = "Discipline"
-> isa_subtype_progress

=== isa_subtype_remnant ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeCommentGeneric
// subtype: remnant; hullmod: Humanity
"This family has its own habits," Isa says. "Get enough examples into our network and the design patterns start showing through."
~ subtype_showcase_name = "Remnant"
~ hullmod_name = "Humanity"
-> isa_subtype_progress

=== isa_subtype_domain_derelict ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeCommentGeneric
// subtype: domain_derelict; hullmod: Memory
"This family has its own habits," Isa says. "Get enough examples into our network and the design patterns start showing through."
~ subtype_showcase_name = "Explorarium"
~ hullmod_name = "Memory"
-> isa_subtype_progress

=== isa_subtype_progress ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeSelected
SYSTEM: Showcase progress: {current_dp} / {unlock_dp} DP worth of {subtype_showcase_name} ships.
+ Preview unlocked response. -> isa_subtype_unlocked
+ Preview locked response. -> isa_subtype_locked
+ Back. -> isa_subtypes

=== isa_subtype_unlocked ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeUnlocked
// first-time unlock scene; hullmod_name and subtype_showcase_name are dynamic
// ui: unlocked option, dialogue handoff, and HIGHLIGHT line are yellow
Isa drags the {subtype_showcase_name} display records into a single stack. For a moment the diagrams fight each other, then snap into one clean pattern.

"There. The samples finally agree on what they were trying to do," she says. "Or at least they are lying consistently now."

+ "What did they teach you?" -> isa_subtype_unlocked_2

=== isa_subtype_unlocked_2 ===
"Enough." Isa signs the file with one quick stroke and sends it to your terminal. "The yard crews have their modspec."

HIGHLIGHT: Received {hullmod_name} modspec.

"Frame it, will ya?" 

+ Back. -> isa_subtypes
=== isa_subtype_locked ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeLocked
"Not enough examples yet. Bring me {remaining_dp} more DP worth and the pattern should stop lying to us."
+ Back. -> isa_subtypes

=== isa_subtype_inactive ===
// hook: data/campaign/rules.csv shipTrophyIsaSubtypeInactive
"That ledger isn't active right now," Isa says. "Might be a missing mod, might be a bad index."
+ Back. -> isa_subtypes

=== isa_faction_hall_complete ===
// hook: data/campaign/rules.csv shipTrophyIsaHallCompletion
// trigger: every active faction/doctrine subtype hullmod is unlocked
// visual: illustrations/ship_trophy_hall_complete
// sfx: ui_contact_developed
A private channel opens on the fleet's command display. Isa is standing only a few steps away, but she has routed a live gallery feed from {market_name} across the screen before saying anything.

The view settles on ranks of restored hulls beneath the gallery lights, each faction's doctrine preserved in steel, circuitry, and old scars.

"I wanted to see it from out here," Isa says at last. "The whole thing at once. Now that I'm aboard, I finally can."

"You've done it, Captain. The Hall of Triumph is complete. Not finished, mind you; places like this are never finished. But every core ledger is preserved under one roof. Not under war, but peace."

Isa glances from the distant gallery to the fleet around you. "The Hall can preserve what came before. From here on, we get to find out what comes next. Thank you for giving me a place in it."
// ui: yellow option; tooltip: Isa has joined the fleet, and the Hall of Triumph stands complete.
+ Welcome aboard, Chief. -> isa_preview_hub

=== isa_join_fleet ===
// hook: data/campaign/rules.csv shipTrophyIsaJoinReady
// condition: Isa's masterwork, vanilla unique hulls, and vanilla subtype ledgers are complete
For once, Isa has no slate in her hands. No schematics, no lists, no half-disassembled parts balanced on the furniture. Just a travel case at her feet and a hard little smile she is trying, unsuccessfully, to hide.

"The Hall can run without me staring over its shoulder now," she says. "Every ledger is clean. Every weird old hull has taught us what it was going to teach from a dock. So if you will have me, Captain, I want to join you. I want to see what legacies looks like when they're being made."

+ "Let's go write history." -> isa_join_success
+ Preview successful recruitment. -> isa_join_success
+ Preview already-recruited response. -> isa_join_already
+ Preview no-longer-ready response. -> isa_join_incomplete

=== isa_join_success ===
// hook: data/campaign/rules.csv shipTrophyIsaJoinConfirm
// ui: the dialogue confirmation is yellow; the campaign notification uses the player color; Isa joins at level 8 with a steady personality
SYSTEM: Isa has joined your fleet as an officer.
CAMPAIGN NOTIFICATION: Isa Leicester has joined your fleet as an officer.
+ Back to contact dialogue. -> isa_contact_main

=== isa_join_already ===
// hook: data/campaign/rules.csv shipTrophyIsaJoinAlready
SYSTEM: Isa has already joined your fleet as an officer.
+ Back to contact dialogue. -> isa_contact_main

=== isa_join_incomplete ===
// hook: data/campaign/rules.csv shipTrophyIsaJoinIncomplete
The Hall of Triumph is not quite ready for Isa to leave her workshop yet.
+ Back to contact dialogue. -> isa_contact_main

=== isa_chatter_menu ===
// hook: data/config/chatter/characters/ship_trophy_isa.json
// Combat Chatter selects one line from the relevant category at runtime.
+ Battle start -> isa_chatter_start
+ Boss battle start -> isa_chatter_start_boss
+ Retreat -> isa_chatter_retreat
+ Out of missiles -> isa_chatter_out_of_missiles
+ Engaged -> isa_chatter_engaged
+ Needs help -> isa_chatter_need_help
+ Pursuing -> isa_chatter_pursuing
+ Running -> isa_chatter_running
+ Hull at 90 percent -> isa_chatter_hull_90
+ Hull at 50 percent -> isa_chatter_hull_50
+ Hull at 30 percent -> isa_chatter_hull_30
+ Overload -> isa_chatter_overload
+ Death -> isa_chatter_death
+ Victory -> isa_chatter_victory
+ Boss victory -> isa_chatter_victory_boss
+ Back to preview hub. -> isa_preview_hub

=== isa_chatter_start ===
// chatter category: start
CHATTER: Chief Engineer Leicester, assuming command. 
CHATTER: Telemetry is live. Let's see what these old hulls can still teach us.
CHATTER: Time for some field tests.
+ Back. -> isa_chatter_menu

=== isa_chatter_start_boss ===
// chatter category: start_boss
CHATTER: That signature is antimatter-grade trouble. Get a clean scan of it before it explodes. Or we do.
CHATTER: Careful with that one!
+ Back. -> isa_chatter_menu

=== isa_chatter_retreat ===
// chatter category: retreat
CHATTER: Pulling back. I can fix her up.
CHATTER: Disengaging. Learned enough today.
+ Back. -> isa_chatter_menu

=== isa_chatter_out_of_missiles ===
// chatter category: out_of_missiles
CHATTER: Missile racks dry!
CHATTER: That's the last missile! Ugh, hope it was worth the ordnance. All that money...
+ Back. -> isa_chatter_menu

=== isa_chatter_engaged ===
// chatter category: engaged
CHATTER: Engaging. Ludd above, their loadouts are straight ass. 
CHATTER: Let's take them apart! 
CHATTER: Contact! I want to know what their engineers thought they were doing.
+ Back. -> isa_chatter_menu

=== isa_chatter_need_help ===
// chatter category: need_help
CHATTER: I could use a hand! Before all my hard work gets blasted off!
CHATTER: Support needed!
+ Back. -> isa_chatter_menu

=== isa_chatter_pursuing ===
// chatter category: pursuing
CHATTER: They're running! Please keep them intact!
CHATTER: After them!
+ Back. -> isa_chatter_menu

=== isa_chatter_running ===
// chatter category: running
CHATTER: Backing off! 
CHATTER: Falling back! 
+ Back. -> isa_chatter_menu

=== isa_chatter_hull_90 ===
// chatter category: hull_90
CHATTER: Armor's opened up!
CHATTER: Breach! Breach! Seal bulkheads and send damage control! 
+ Back. -> isa_chatter_menu

=== isa_chatter_hull_50 ===
// chatter category: hull_50
CHATTER: Half hull!
CHATTER: Structural integrity below fifty!
+ Back. -> isa_chatter_menu

=== isa_chatter_hull_30 ===
// chatter category: hull_30
CHATTER: Hull critical! The ship's singing the fat lady's song!
CHATTER: Son of a bitch! If we survive, I'm welding the patches up myself!
+ Back. -> isa_chatter_menu

=== isa_chatter_overload ===
// chatter category: overload
CHATTER: Overloaded. Fantastic.
CHATTER: Flux grid is out. Cover us, I'm fixing her up!
+ Back. -> isa_chatter_menu

=== isa_chatter_death ===
// chatter category: death
CHATTER: Escape pods! Save as many as you ca - *static*
CHATTER: Closing time, Captain- *static*
+ Back. -> isa_chatter_menu

=== isa_chatter_victory ===
// chatter category: victory
CHATTER: Glad that's over. I want logs, samples, and coffee.
CHATTER: Good work. Try to leave me something recognizable in the salvage.
CHATTER: Just a kinetic peer review.
+ Back. -> isa_chatter_menu

=== isa_chatter_victory_boss ===
// chatter category: victory_boss
CHATTER: That wreck is going to sing her song.
CHATTER: No one touch the weird parts until I get there. That means you, Captain.
+ Back. -> isa_chatter_menu