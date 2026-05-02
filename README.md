# Encantadia: Echoes of the Gem - Java Project Documentation

A turn-based battle game inspired by the Filipino fantasy series "Encantadia," built with Java Swing. The game features 8 playable characters with unique skills, three game modes (PVE, PVP, Arcade), and a comprehensive battle engine with cooldown and status effect management.

---

## 📁 Directory Structure

```
OOP2JAVAPROJECT/
├── src/
│   ├── encantadia/                          # Main package
│   │   ├── Main.java                        # Entry point
│   │   ├── ScreenManager.java               # Global screen management & fullscreen toggle
│   │   ├── BackstoryShowcase.java           # Backstory display UI
│   │   │
│   │   ├── characters/                      # Character classes
│   │   │   ├── Character.java               # Abstract base class for all characters
│   │   │   ├── Adamus.java                  # Guardian of Rivers & Lakes (Water)
│   │   │   ├── Tera.java                    # Daughter of First Mountain (Earth/Healing)
│   │   │   ├── Dirk.java                    # Guardian of Growth & Harvest (Earth)
│   │   │   ├── Flamara.java                 # Bearer of Starlit Flames (Fire)
│   │   │   ├── Dea.java                     # Daughter of Northern Winds (Wind)
│   │   │   ├── Tyrone.java                  # Fire Sang'gre of Hathoria (Fire)
│   │   │   ├── Mary.java                    # Claire - Goddess of Tides (Water)
│   │   │   ├── MakelanShere.java            # Devourer of Crimson Bloom (Enemy)
│   │   │   └── animation/
│   │   │       └── CharacterAnimator.java   # Manages character GIF animations
│   │   │
│   │   ├── battle/                          # Battle system
│   │   │   ├── EnemyFactory.java            # Generates enemy characters
│   │   │   ├── engine/
│   │   │   │   ├── TurnManager.java         # Core turn-based battle logic
│   │   │   │   └── CooldownManager.java     # Tracks skill cooldowns
│   │   │   ├── result/
│   │   │   │   └── TurnResult.java          # Encapsulates turn outcomes
│   │   │   ├── skill/
│   │   │   │   └── Skill.java               # Skill definition with effects
│   │   │   ├── status/
│   │   │   │   └── StatusEffect.java        # Damage reduction, cooldown effects, etc.
│   │   │   ├── ai/
│   │   │   │   └── EnemyAI.java             # AI decision logic for enemies
│   │   │   └── arcade/
│   │   │       └── ArcadeModeManager.java   # Manages arcade tower progression
│   │   │
│   │   ├── gamemode/                        # Game mode implementations
│   │   │   ├── GameModeType.java            # Enum: PVE, PVP, ARCADE
│   │   │   ├── BaseModeScreen.java          # Base class for all game modes
│   │   │   ├── PVEMode.java                 # Player vs Environment
│   │   │   ├── PVPMode.java                 # Player vs Player
│   │   │   └── ArcadeMode.java              # Tower climbing mode
│   │   │
│   │   ├── story/                           # Narrative elements
│   │   │   ├── StoryType.java               # Enum for story types
│   │   │   ├── CharacterStories.java        # Character backstories
│   │   │   └── GameStories.java             # PVE/Arcade lore
│   │   │
│   │   ├── ui/                              # User interface
│   │   │   ├── effects/
│   │   │   │   └── CursorTrailEffect.java   # Cursor particle effects
│   │   │   └── frames/
│   │   │       ├── WelcomeScreenPage.java   # Initial welcome screen
│   │   │       ├── MainMenuFrame.java       # Main menu
│   │   │       ├── CharacterSelectionFrame.java  # Character picker
│   │   │       ├── OptionFrame.java         # Settings/Options
│   │   │       ├── ExitConfirmDialog.java   # Exit confirmation
│   │   │       ├── ResultDialogFrame.java   # Battle results
│   │   │       ├── ArcadeLorePromptFrame.java   # Arcade intro
│   │   │       ├── ArcadeTowerFrame.java    # Arcade tower UI
│   │   │       ├── ArcadeVictoryFrame.java  # Arcade victory screen
│   │   │       └── battleModeFrames/
│   │   │           ├── PVEBattleFrame.java  # PVE battle UI
│   │   │           ├── PVPBattleFrame.java  # PVP battle UI
│   │   │           └── ArcadeModeBattleFrame.java  # Arcade battle UI
│   │   │
│   │   └── utils/                           # Utility classes (empty)
│   │
│   ├── resources/                           # Game assets
│   │   ├── character/                       # Character resources
│   │   │   ├── adamus/                      # Adamus animations & assets
│   │   │   ├── claire/                      # Claire/Mary animations & assets
│   │   │   ├── dea/                         # Dea animations & assets
│   │   │   ├── dirk/                        # Dirk animations & assets
│   │   │   ├── flamara/                     # Flamara animations & assets
│   │   │   ├── makelanShere/                # MakelanShere animations & assets
│   │   │   ├── tera/                        # Tera animations & assets
│   │   │   └── tyrone/                      # Tyrone animations & assets
│   │   └── [UI buttons, backgrounds, frames, etc.]
│   │
│   └── OOP2JAVAPROJECT.iml                  # Project configuration
│
├── out/                                      # Compiled output directory
│   └── production/OOP2JAVAPROJECT/          # Compiled classes & resources
│
├── assets/                                   # Additional assets
    └── AHHH/
        └── [Additional resources]
```

---

## 👥 Playable Characters & Stats

All playable characters have **5000 Max HP** and unique skill sets. Damage ranges represent minimum to maximum damage per skill.

### 1. **Adamus** - Guardian of Rivers and Lakes
- **Title:** Guardian of Rivers and Lakes
- **Element:** Water
- **HP:** 5000
- **Skill 1: Water Spear**
  - Damage: 220-300
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Ocean Wave**
  - Damage: 300-450
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Tsunami Curse**
  - Damage: 600-650
  - Cooldown: 3 turns
  - Effect: MISS_CHANCE (45% chance to cause enemy to miss next turn)

### 2. **Tera** - Daughter of the First Mountain Stone
- **Title:** Daughter of the First Mountain Stone
- **Element:** Earth
- **HP:** 5000
- **Skill 1: Rock Smash**
  - Damage: 230-310
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Earthquake**
  - Damage: 300-380
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Nature's Wrath**
  - Damage: 500-650
  - Cooldown: 3 turns
  - Effect: HEAL (450 HP healed to self - 100% proc chance)

### 3. **Dirk** - Guardian of Growth and Harvest
- **Title:** Guardian of Growth and Harvest
- **Element:** Earth
- **HP:** 5000
- **Skill 1: Bato Dela Rosa**
  - Damage: 230-310
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Ding ang Bato!**
  - Damage: 300-400
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Linog**
  - Damage: 500-650
  - Cooldown: 3 turns
  - Effect: DAMAGE_REDUCTION (40% damage reduction - 100% proc chance)

### 4. **Flamara** - Bearer of Starlit Flames
- **Title:** Bearer of Starlit Flames
- **Element:** Fire
- **HP:** 5000
- **Skill 1: Fire Ball**
  - Damage: 280-360
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Flame Burst**
  - Damage: 380-480
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Inferno Strike**
  - Damage: 560-700
  - Cooldown: 3 turns
  - Effect: EXTRA_DAMAGE (+200 damage bonus - 40% proc chance)

### 5. **Dea** - Daughter of the Northern Winds
- **Title:** Daughter of the Northern Winds
- **Element:** Wind
- **HP:** 5000
- **Skill 1: Wind Slash**
  - Damage: 220-300
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Storm Fury**
  - Damage: 320-400
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Whirlwind**
  - Damage: 500-620
  - Cooldown: 3 turns
  - Effect: COOLDOWN_INCREASE (+1 cooldown turn - 45% proc chance)

### 6. **Tyrone** - Fire Sang'gre of Hathoria
- **Title:** Fire Sang'gre of Hathoria
- **Element:** Fire
- **HP:** 5000
- **Skill 1: Fire Blaze**
  - Damage: 260-340
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Fire Beam**
  - Damage: 360-450
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Fire Burst**
  - Damage: 525-650
  - Cooldown: 3 turns
  - Effect: EXTRA_DAMAGE (+100 damage bonus - 100% proc chance)

### 7. **Mary (Claire)** - Goddess of Tides
- **Title:** Goddess of Tides
- **Element:** Water
- **HP:** 5000
- **Skill 1: Luha**
  - Damage: 220-300
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Flood Control**
  - Damage: 320-420
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Tsunami**
  - Damage: 525-650
  - Cooldown: 3 turns
  - Effect: TURN_STEAL (+1 extra turn - 50% proc chance)

### 8. **MakelanShere** - Devourer of the Crimson Bloom
- **Title:** Devourer of the Crimson Bloom
- **Element:** Void (Enemy)
- **HP:** 5000
- **Skill 1: Lead Heel**
  - Damage: 240-320
  - Cooldown: 0 turns
  - Effect: None
- **Skill 2: Savor Thy Flesh**
  - Damage: 340-450
  - Cooldown: 2 turns
  - Effect: None
- **Skill 3: Gouged Petunia**
  - Damage: 550-700
  - Cooldown: 3 turns
  - Effect: RECOIL (15% recoil damage to attacker - 100% proc chance)

---

## ⚙️ System Logic

### 1. **Battle Engine (TurnManager)**

The `TurnManager` class orchestrates all turn-based combat mechanics:

#### **Turn Flow**
- Players take turns executing skills against enemies
- Enemies use AI (EnemyAI) to select actions
- Turns alternate automatically unless a skill triggers TURN_STEAL
- Turn ends when:
  - A skill is successfully executed
  - A character is defeated (HP reaches 0)
  - Battle concludes

#### **Skill Execution Process**
```
executeSkill(attacker, target, skillIndex)
├── 1. Validate skill index
├── 2. Check cooldown status
├── 3. Roll skill damage (minDamage + random[0, maxDamage-minDamage])
├── 4. IF skill type == HEAL:
│   └── Apply healing with effects
├── 5. IF skill type == DAMAGE:
│   ├── Calculate miss chance
│   ├── If missed: Set cooldown and return
│   ├── Apply damage reductions (DAMAGE_REDUCTION effects)
│   ├── Deal damage to target
│   └── IF effect triggered: Apply special effect
├── 6. Tick active status effects
└── 7. Return TurnResult
```

#### **Damage Roll Formula**
Base damage is randomly generated from skill's damage range:

```
baseDamage = minDamage + random(0, maxDamage - minDamage)
```

**Example:** Water Spear (220-300 damage)
- If random generates 0: baseDamage = 220 + 0 = **220**
- If random generates 50: baseDamage = 220 + 50 = **270**
- If random generates 80: baseDamage = 220 + 80 = **300**

#### **Complete Damage Calculation**

After base damage is rolled, reductions are applied multiplicatively:

```
finalDamage = baseDamage × (1 - selfReduction) × (1 - targetReduction)
finalDamage = max(0, finalDamage)

Where:
  selfReduction    = Sum of all DAMAGE_REDUCTION effects on attacker (capped at 0.9 / 90%)
  targetReduction  = Sum of all DAMAGE_REDUCTION effects on target (capped at 0.9 / 90%)
```

**Example 1:** No reductions
```
Flamara uses Flame Burst (rolled 450 damage)
finalDamage = 450 × (1 - 0) × (1 - 0) = 450
Target takes 450 damage
```

**Example 2:** With target's damage reduction
```
Flamara uses Flame Burst (rolled 450 damage)
Target has 40% DAMAGE_REDUCTION active (from Dirk's Linog)
finalDamage = 450 × (1 - 0) × (1 - 0.40)
finalDamage = 450 × 1.0 × 0.60 = 270
Target takes 270 damage (40% reduction)
```

**Example 3:** With both reductions
```
Tyrone uses Fire Beam (rolled 400 damage)
Tyrone has 30% self-reduction (post-RECOIL effect)
Enemy has 40% DAMAGE_REDUCTION active
finalDamage = 400 × (1 - 0.30) × (1 - 0.40)
finalDamage = 400 × 0.70 × 0.60 = 168
Enemy takes 168 damage
```

**Example 4:** Stacked reductions
```
Adamus uses Tsunami Curse (rolled 620 damage)
Target has 40% reduction from Effect A + 50% reduction from Effect B
Total reduction = min(0.40 + 0.50, 0.90) = 0.90 (90% cap)
finalDamage = 620 × 1.0 × (1 - 0.90) = 62
Target takes 62 damage (90% reduction cap)
```

### 2. **Skill Effects (EffectType Enum)**

Nine distinct effect types modify skill behavior:

| Effect Type | Description | Trigger Mechanism |
|---|---|---|
| **NONE** | No secondary effect | - |
| **DAMAGE_REDUCTION** | Reduces incoming damage % | Applied to target for 1 turn |
| **HEAL** | Restores target HP | Always applied (100% proc) |
| **EXTRA_DAMAGE** | Bonus flat damage | Random proc chance |
| **TURN_STEAL** | Grants extra turn | Random proc chance; skips opponent's next turn |
| **COOLDOWN_INCREASE** | Extends enemy cooldown | Random proc chance per target skill |
| **COOLDOWN_REDUCTION** | Reduces own cooldown | Applied to attacker (reserved for future) |
| **RECOIL** | Attacker takes damage | % of damage dealt; 100% guaranteed |
| **MISS_CHANCE** | Forces attack miss | Random proc chance; affects next enemy turn |

#### **Effect Triggering**
- **Guaranteed (100% proc chance):** HEAL, RECOIL, EXTRA_DAMAGE (in some skills)
- **Probabilistic:** MISS_CHANCE, TURN_STEAL, COOLDOWN_INCREASE use `Random.nextDouble()`
- **Status-based:** Applied via StatusEffect manager for multi-turn effects

#### **Effect Probability Formula**
```
effectTriggered = (procChance >= 1.0) || (procChance > 0 && random() < procChance)

Where:
  procChance = skill's effect proc chance (0.0 to 1.0)
  random()   = Random value between 0.0 and 1.0
```

**Examples:**
- **40% proc chance:** Triggers if random() < 0.40 ≈ 40% of the time
- **100% proc chance:** Always triggers (guaranteed)
- **0% proc chance:** Never triggers

#### **DAMAGE_REDUCTION Effect**
Reduces incoming damage multiplicatively for the next turn only:

```
damageReduction = effectValue
totalReduction = min(sum of all reductions, 0.90)  // 90% cap

finalDamage = damage × (1 - totalReduction)
```

**Example:** Dirk's Linog skill triggers DAMAGE_REDUCTION (40%)
```
Enemy deals 500 damage
Dirk has active 40% DAMAGE_REDUCTION effect
Damage taken = 500 × (1 - 0.40) = 500 × 0.60 = 300
Effect lasts 1 turn, then expires
```

#### **EXTRA_DAMAGE Effect**
Adds bonus damage on top of base skill damage:

```
IF effectValue < 1.0:
  bonusDamage = maxHP × effectValue
ELSE:
  bonusDamage = effectValue (flat value)

totalDamage = baseDamage + bonusDamage
```

**Character Examples:**
- **Flamara's Inferno Strike** (40% proc, +200 flat): Adds 200 bonus damage if triggered
- **Tyrone's Fire Burst** (100% proc, +100 flat): Always adds 100 bonus damage

**Example Scenario:**
```
Flamara uses Inferno Strike (rolled 600 damage base)
Effect triggers (40% chance)
bonusDamage = 200 (flat value)
totalDamage = 600 + 200 = 800
Enemy takes 800 damage (vs 600 if effect doesn't trigger)
```

#### **RECOIL Effect**
Attacker takes percentage of damage they dealt as self-inflicted damage:

```
recoilDamage = baseDamageDealt × effectValue
recoilDamage = (int) recoilDamage  // Cast to integer

After RECOIL:
  Attacker applies 30% DAMAGE_REDUCTION to self for 2 turns
```

**Example:** MakelanShere's Gouged Petunia (15% recoil)
```
MakelanShere deals 600 damage to enemy
RECOIL effect triggers (100% guaranteed)
recoilDamage = 600 × 0.15 = 90
MakelanShere takes 90 damage to self
MakelanShere gains 30% DAMAGE_REDUCTION buff for 2 turns
```

#### **EXTRA_DAMAGE with Recoil Synergy**
```
Turn 1: MakelanShere's Gouged Petunia
  baseDamage = 650
  RECOIL triggers: recoil = 650 × 0.15 = 97.5 ≈ 97 damage to self
  Self DAMAGE_REDUCTION (30%) applies for next 2 turns

Turn 2: Enemy deals 500 damage
  finalDamage = 500 × (1 - 0.30) = 350 (thanks to recoil buff)
  
Turn 3: Enemy deals 500 damage
  finalDamage = 500 × (1 - 0.30) = 350 (buff still active)
  
Turn 4: Enemy deals 500 damage
  finalDamage = 500 × (1 - 0) = 500 (buff expired)
```

#### **MISS_CHANCE Effect**
Forces an attack to miss with given probability:

```
missChance = sum of all active MISS_CHANCE effects on attacker
missChance = min(missChance, 0.90)  // 90% cap

IF random() < missChance:
  damage = 0
  cooldown is still applied
  attack fails
ELSE:
  damage calculated normally
```

**Example:** Adamus's Tsunami Curse triggers MISS_CHANCE (45%)
```
Adamus uses Tsunami Curse on enemy
Effect triggers (45% proc chance)
Enemy's next attack:
  45% chance to completely MISS (0 damage dealt)
  55% chance to hit normally
Cooldown still applies either way
Effect lasts 1 turn
```

#### **TURN_STEAL Effect**
Grants attacker an extra turn if effect triggers:

```
IF effect triggers with procChance:
  turnStolen = true
  Attacker gets immediate extra turn
  Opponent's turn is skipped
```

**Example:** Mary's Tsunami triggers TURN_STEAL (50% proc)
```
Turn 1: Player uses Tsunami (Mary)
  50% chance effect triggers
  IF triggered:
    Status = "Extra turn gained!"
    Player gets Turn 2 immediately
    Enemy's Turn 2 is skipped
    Player goes again in Turn 3
  ELSE:
    Normal turn ends
    Enemy gets Turn 2
```

#### **COOLDOWN_INCREASE Effect**
Extends all enemy skill cooldowns by set amount:

```
FOR each skill in enemy's skill list:
  cooldown[skillIndex] += effectValue (usually +1 turn)
```

**Example:** Dea's Whirlwind triggers COOLDOWN_INCREASE (45% proc, +1 turn)
```
Enemy has cooldowns: [0, 1, 2]  (Skill 2 ready in 1 turn, Skill 3 ready in 2 turns)

Dea uses Whirlwind - Effect triggers (45% chance)
Enemy's new cooldowns: [0, 2, 3]
Effect forces each skill to wait 1 additional turn
Skill 2 now available at Turn 3 instead of Turn 2
Skill 3 now available at Turn 4 instead of Turn 3
```

#### **HEAL Effect**
Directly heals the character (usually self-heal):

```
healAmount = effectValue

currentHP = min(currentHP + healAmount, maxHP)
healApplied = min(healAmount, maxHP - oldHP)  // Cannot exceed max
```

**Example:** Tera's Nature's Wrath (100% proc, +450 heal)
```
Tera uses Nature's Wrath
baseSkillDamage = 575 (rolled)
effectValue = 450 (flat heal)
Effect triggers (100% guaranteed)
Tera heals 450 HP
Damage enemy for 575

If Tera's HP was 4600/5000:
  New HP = min(4600 + 450, 5000) = 5000 (capped at max)
  Actual heal applied = 400 (only needed 400 to reach max)
```

#### **Complete Turn with Multiple Effects (Complex Example)**

```
Round: Flamara (4500/5000 HP) vs Adamus (3800/5000 HP)

Flamara uses Inferno Strike (560-700 damage, 40% +200 bonus)
  1. Roll base damage: 650
  2. Check miss chance on Flamara: 0% (no active effects)
  3. Roll for effect: random() = 0.35 < 0.40 → Effect TRIGGERS
  4. Calculate bonus damage: 200 (flat value)
  5. Apply reductions:
     - Flamara's reduction: 0% (no effects)
     - Adamus's reduction: 0% (no effects)
     finalDamage = 650 × (1 - 0) × (1 - 0) = 650
  6. Deal damage: 3800 - 650 = 3150 HP to Adamus
  7. Apply EXTRA_DAMAGE: Adamus takes additional 200 damage
     3150 - 200 = 2950 HP to Adamus
  8. Set cooldown: Inferno Strike (cooldown=3)
  9. Return TurnResult:
     ├─ attackerName: "Flamara"
     ├─ skillUsed: "Inferno Strike"
     ├─ baseDamage: 650
     ├─ totalDamage: 650
     ├─ extraDamage: 200
     ├─ effectTriggered: true
     ├─ logMessages: ["🔥 Extra damage dealt!"]
     └─ targetDefeated: false

Adamus now at 2950/5000 HP
```

### 3. **Cooldown System (CooldownManager)**

Prevents skill spam by enforcing turn-based cooldown:

#### **Mechanics**
- Each character tracks cooldown for each skill (0-3 turns typical)
- Cooldown decrements after each turn
- Skill cannot be used if cooldown > 0
- Upon skill execution: `setCooldown(character, skillIndex, cooldownValue)`

#### **Cooldown Values**
- **Skill 1 (basic):** 0 turns (always available)
- **Skill 2 (intermediate):** 2 turns
- **Skill 3 (ultimate):** 3 turns

#### **Cooldown Decrement Formula**
```
BEFORE Turn:
  active_cooldowns = [0, 1, 0]  // Track for skills 1,2,3

IF turn belongs to character:
  FOR each cooldown in active_cooldowns:
    IF cooldown > 0:
      cooldown -= 1

AFTER Decrement:
  active_cooldowns = [0, 0, 0]  // Skill 2 now available

Skill execution:
  active_cooldowns[skillIndex] = skill.getCooldown()
  e.g., active_cooldowns[2] = 3  // Skill 3 resets to 3
```

#### **Complete Cooldown Lifecycle Example**
```
Turn 1: Adamus (owns Tsunami with cooldown=3)
  beforeState: [0, 2, 0]
  Adamus executes Tsunami (skillIndex=2)
  setCooldown(Adamus, 2, 3)
  afterState: [0, 2, 3]

Turn 2: Enemy takes turn
  (Adamus's cooldowns tick during enemy turn)
  Adamus decrement: [0, 2, 3] → [0, 2, 3] (only own turn causes decrement)
  No change (turn doesn't belong to Adamus)

Turn 3: Adamus's turn
  Decrement before action: [0, 2, 3] → [0, 1, 2]
  Adamus CAN use Skill 1 (CD=0) or Skill 2 (CD=1)
  Adamus CANNOT use Skill 3 (CD=2)

Turn 4: Enemy takes turn
  No change

Turn 5: Adamus's turn
  Decrement before action: [0, 1, 2] → [0, 0, 1]
  Adamus CAN use Skill 1 or 2
  Adamus CANNOT use Skill 3 (CD=1)

Turn 6: Enemy takes turn
  No change

Turn 7: Adamus's turn
  Decrement before action: [0, 0, 1] → [0, 0, 0]
  Adamus CAN use ANY skill (all available)
```

#### **COOLDOWN_INCREASE Effect with Formula**
When COOLDOWN_INCREASE triggers:
```
FOR each skill in target's skill list:
  target_cooldowns[skillIndex] += effectValue

Example: Dea's Whirlwind (45% proc, +1 turn cooldown increase)
Enemy has [0, 1, 2]
Effect triggers
Enemy now has [0, 2, 3]
Each skill delayed by 1 turn
```

### 4. **Status Effect Management**

Active effects persist across multiple turns:

#### **Status Effect Structure**
- **EffectType:** What effect (DAMAGE_REDUCTION, etc.)
- **EffectValue:** Magnitude (0.40 = 40% reduction, or integer for other effects)
- **Duration:** Turns remaining (when += 0, effect expires)
- **EffectTarget:** SELF or ENEMY (determines who is affected)
- **Owner:** Character that applied effect (prevents self-immunity quirks)

#### **Effect Lifecycle with Ticking**
```
1. Application Phase:
   addEffect(StatusEffect{
     type=DAMAGE_REDUCTION, 
     value=0.40, 
     duration=1,
     target=ENEMY,
     owner=attacker
   })
   activeEffects = [StatusEffect(...)]
   activeOwners = [attacker]

2. Persistence Phase:
   Effect remains active until duration = 0

3. Ticking Phase (after each turn):
   FOR each effect in activeEffects:
     effect.tick()  // duration -= 1
     IF effect.isExpired() (duration <= 0):
       activeEffects.remove(effect)
       activeOwners.remove(effect)

4. Removal Phase:
   When duration reaches 0, effect is purged from lists
```

#### **Effect Duration Examples**

| Effect | Duration | Behavior |
|---|---|---|
| DAMAGE_REDUCTION (from Dirk's Linog) | 1 turn | Applied to damage calculation NEXT enemy attack, then expires |
| MISS_CHANCE (from Adamus's Tsunami Curse) | 1 turn | Affects enemy's NEXT attack, then expires |
| Self DAMAGE_REDUCTION (from RECOIL ability) | 2 turns | Protects attacker for their next 2 defensive turns |

#### **Stacking Rules**
Multiple effects CAN stack additively (with caps):

```
Total Reduction = min(reduction1 + reduction2 + ... + reductionN, 0.90)
Total Miss = min(miss1 + miss2 + ... + missN, 0.90)
```

**Example:** Two reduction effects active
```
Effect 1: 40% DAMAGE_REDUCTION (Dirk's Linog)
Effect 2: 30% DAMAGE_REDUCTION (another source)
Total = min(0.40 + 0.30, 0.90) = 0.70

Incoming 500 damage:
finalDamage = 500 × (1 - 0.70) = 500 × 0.30 = 150
90% damage cap prevents exploitation
```

#### **Complex Effect Cascade Example**
```
Round 1: Dirk uses Linog (500-650 base, 40% DAMAGE_REDUCTION effect)
  Damage rolled: 620
  Target takes 620 damage
  StatusEffect created: {DAMAGE_REDUCTION, 0.40, duration=1, target=ENEMY, owner=Dirk}
  activeEffects = [StatusEffect(...)]

Round 2: MakelanShere attacks (incoming to Dirk)
  Before attack: Check Dirk's effects (none on self)
  Attack: 600 base damage
  finalDamage = 600 × (1 - 0) = 600
  Dirk takes 600 damage

Round 3: After MakelanShere's turn, tickEffects() called
  Linog's DAMAGE_REDUCTION effect: duration 1 → 0
  Effect expires, removed from activeEffects
  activeEffects = [] (empty)

Round 4: Dirk attacks again
  No DAMAGE_REDUCTION buffs remain
  If incoming attack: 600 base = 600 damage taken (no reduction)
```

### 5. **Damage Mitigation Mechanics**

#### **DAMAGE_REDUCTION Effect (Complete Formula)**
- Applies multiplicative reduction to incoming damage
- Formula: `finalDamage = damage × (1 - reductionValue)` per effect
- Multiple reductions stack: `totalReduction = min(Σ all reductions, 0.90)`
- Applied AFTER base damage is rolled

Example: 40% reduction on 500 damage
```
finalDamage = 500 × (1 - 0.40) = 500 × 0.60 = 300 damage taken
```

Max stacking example:
```
Multiple 50% reductions applied
totalReduction = min(0.50 + 0.50, 0.90) = 0.90
finalDamage = 500 × (1 - 0.90) = 50 (90% cap prevents infinite stacking)
```

#### **RECOIL Effect (Complete Formula)**
- Attacker takes percentage of damage they dealt
- Formula: `recoilDamage = baseDamageDealt × recoilPercentage`
- ALWAYS triggers (100% proc chance)
- After recoil, attacker gains 30% DAMAGE_REDUCTION buff for 2 turns

Detailed example with MakelanShere:
```
MakelanShere uses Gouged Petunia (550-700 base, 15% recoil)
  baseDamage rolled: 650
  noReductionDamage = 650 (no reductions applied to him)
  Damage opponent: 650
  
  RECOIL calculation:
  recoilDamage = 650 × 0.15 = 97.5 ≈ 97
  MakelanShere takes 97 damage to himself
  
  Counter-buff applied:
  StatusEffect {
    type=DAMAGE_REDUCTION,
    value=0.30,
    duration=2,  // Lasts 2 of opponent's turns
    target=SELF,
    owner=MakelanShere
  }
```

#### **MISS_CHANCE Effect (Complete Formula)**
- Accumulates miss chance from all active effects
- Formula: `missChance = min(Σ all miss chance effects, 0.90)`
- Checked BEFORE damage rolls: `if (random() < missChance): return miss`
- Cooldown still applies even on miss

Example with Adamus:
```
Adamus uses Tsunami Curse (600-650 base, 45% MISS_CHANCE)
  baseroll: 625
  Effect triggers (45% proc)
  StatusEffect added: {MISS_CHANCE, 0.45, duration=1, target=ENEMY}
  
Enemy's next attack:
  missChance = 0.45 (no stacking with others)
  random() = 0.32
  IF 0.32 < 0.45 → MISS! Zero damage dealt
  Cooldown still applies: cooldown[skillIndex] set normally
  Return "💨 Attack missed!"
```

### 6. **Game Modes**

#### **PVE (Player vs Environment)**
- Player selects character
- AI-controlled enemy (MakelanShere by default)
- 3-round format with round transitions
- Victory condition: Defeat enemy 3 times
- Includes story lore

#### **PVP (Player vs Player)**
- Two human players select characters
- Keyboard controls for each player
- Both see battle UI simultaneously
- First to win 3 rounds wins overall match

#### **ARCADE (Tower Mode)**
- Single-player tower climbing
- Opponents increase in difficulty
- Procedurally selected enemies (from available characters)
- Rounds accumulate
- Victory: Clear tower/reach max rounds

### 7. **Turn Result Tracking (TurnResult)**

Each skill execution produces a `TurnResult` with:

```java
public class TurnResult {
    String attackerName;              // Who attacked
    String skillUsed;                 // Skill name
    int baseDamageOrHeal;             // Rolled value before modifiers
    int totalDamageDealt;             // Final damage after reductions
    int totalHealApplied;             // HP restored (if applicable)
    int recoilDamage;                 // Damage attacker took
    int extraDamage;                  // Bonus damage applied
    boolean effectTriggered;          // Did effect proc?
    String effectDescription;         // Human-readable effect text
    boolean turnStolen;               // Was extra turn granted?
    boolean targetDefeated;           // Is target dead?
    List<String> logMessages;         // Battle log entries
}
```

### 8. **Complete Combat Walkthrough (Multi-Turn Example)**

This scenario shows how damage, effects, cooldowns, and status effects interact:

```
INITIAL STATE:
Player: Flamara (5000 HP, all skills available)
Enemy: Adamus (5000 HP, all skills available)

═══════════════════════════════════════════════════════════════════

TURN 1: Flamara's Turn
─────────────────────────────────────────────────────────────────
Action: Flamara uses Inferno Strike (560-700 damage, 40% extra +200 bonus)

Step 1: Roll base damage
  baseDamage = 560 + random(0 to 140) = 560 + 83 = 643

Step 2: Check cooldowns
  activeEffects on Flamara = [] (empty)
  missChance = 0 (no MISS_CHANCE on her)

Step 3: Check reductions
  selfReduction = 0 (no DAMAGE_REDUCTION on Flamara)
  targetReduction = 0 (no effects on Adamus yet)
  finalDamage = 643 × (1 - 0) × (1 - 0) = 643

Step 4: Apply damage
  Adamus: 5000 - 643 = 4357 HP

Step 5: Check effect trigger
  random() = 0.35 < 0.40 → EFFECT TRIGGERS ✓
  bonusDamage = 200
  Adamus: 4357 - 200 = 4157 HP

Step 6: Apply cooldown
  Inferno Strike cooldown set to 3

Step 7: Tick effects
  activeEffects = [] (still empty, no effects to tick)

RESULT:
  Adamus takes 643 + 200 = 843 total damage
  Adamus: 4157/5000 HP (16.9% damage dealt)
  Log: "643 DMG | 🔥 Extra damage dealt! | 200 BONUS"

═══════════════════════════════════════════════════════════════════

TURN 2: Adamus's Turn
─────────────────────────────────────────────────────────────────
Action: Adamus uses Tsunami Curse (600-650, 45% MISS_CHANCE)

Step 1: Roll base damage
  baseDamage = 600 + random(0 to 50) = 600 + 31 = 631

Step 2: Check reductions
  finalDamage = 631 × (1 - 0) × (1 - 0) = 631

Step 3: Apply damage
  Flamara: 5000 - 631 = 4369 HP

Step 4: Check effect trigger
  random() = 0.42 < 0.45 → EFFECT TRIGGERS ✓
  Create StatusEffect:
    type=MISS_CHANCE, value=0.45, duration=1, target=ENEMY
  activeEffects = [MISS_CHANCE(0.45, dur=1)]

Step 5: Apply cooldown
  Tsunami Curse cooldown set to 3

Step 6: Tick effects
  MISS_CHANCE: duration 1 → 0
  Effect expires (was for Flamara)
  activeEffects = [] (expired)

RESULT:
  Flamara takes 631 damage
  Flamara: 4369/5000 HP
  Flamara has 45% MISS_CHANCE (1 turn)
  Log: "631 DMG | 🌫 Enemy accuracy reduced!"

═══════════════════════════════════════════════════════════════════

TURN 3: Flamara's Turn
─────────────────────────────────────────────────────────────────
Action: Flamara uses Fire Ball (280-360, no effects)

Step 1: Check miss chance (from Adamus's Tsunami)
  missChance = 0.45 (active from previous turn)
  random() = 0.38 < 0.45 → MISS! ✗

Step 2: Set cooldown anyway
  Fire Ball cooldown set to 0
  (Basic attack has no cooldown, so stays at 0)

RESULT:
  Flamara's attack completely misses
  No damage dealt to Adamus
  Cooldown still applies (already 0)
  Log: "💨 Attack missed!"

═══════════════════════════════════════════════════════════════════

TURN 4: Adamus's Turn
─────────────────────────────────────────────────────────────────
(Miss chance from Turn 2 has expired)

Action: Adamus uses Ocean Wave (300-450, no effects)

Step 1: Roll base damage
  baseDamage = 300 + random(0 to 150) = 300 + 112 = 412

Step 2: No miss chance anymore
  random() = 0.88 (irrelevant, no MISS_CHANCE active)

Step 3: Apply damage
  Flamara: 4369 - 412 = 3957 HP

Step 4: Apply cooldown
  Ocean Wave cooldown set to 2

RESULT:
  Flamara takes 412 damage
  Flamara: 3957/5000 HP
  Log: "412 DMG"

═══════════════════════════════════════════════════════════════════

TURN 5: Flamara's Turn
─────────────────────────────────────────────────────────────────
Action: Flamara uses Fire Beam (360-450, no effects)

Step 1: Roll damage
  baseDamage = 360 + random(0 to 90) = 360 + 67 = 427

Step 2: Apply damage
  Adamus: 4157 - 427 = 3730 HP

Step 3: Apply cooldown
  Fire Beam cooldown set to 2

RESULT:
  Adamus takes 427 damage
  Adamus: 3730/5000 HP (25.4% total damage so far)
  Log: "427 DMG"

═══════════════════════════════════════════════════════════════════

SUMMARY AFTER 5 TURNS:
Player Status: Flamara 3957/5000 (1043 damage taken)
Enemy Status: Adamus 3730/5000 (1270 damage taken)
Active Effects: None
Flamara Cooldowns: [0, 1, 1] (Fire Ball ready, others in cooldown)
Adamus Cooldowns: [0, 0, 0] (All skills available - Ocean Wave CD expired)
Battle continues...
```

### 9. **AI Decision Logic (EnemyAI)**

Enemies use simple decision-making:

- **Skill Selection:** Greedy algorithm prioritizing highest damage available
- **Targeting:** Always targets the player (single opponent in PVE)
- **Skill Rotation:** Cycles through skills respecting cooldowns
- **Fallback:** Uses basic attack if all skills on cooldown

### 9. **Animation System**

Characters support optional GIF animations:

#### **Animation Hooks**
- `getIdleAnimationPath()` → Looping stand animation
- `getSkillAnimationPaths()` → Array of skill GIF paths (one per skill)
- `getSkillAnimationDurations()` → Duration in milliseconds per skill

#### **CharacterAnimator**
- Loads GIF frames from resources
- Schedules animation playback during turn
- Auto-reverts to idle after skill animation completes
- Gracefully handles missing animation assets

### 10. **Screen Manager & Fullscreen**

Global `ScreenManager` handles:
- Window management across all frames
- Toggle fullscreen with F11 key
- Coordinate transitions between game states
- Consistent resolution scaling

---

## 📐 Quick Reference: Damage & Effect Formulas

### Damage Calculation
```
baseDamage = minDamage + random(0, maxDamage - minDamage)
finalDamage = baseDamage × (1 - selfReduction) × (1 - targetReduction)
finalDamage = max(0, finalDamage)
```

### Effect Probability
```
triggered = (procChance >= 1.0) || (random() < procChance)
```

### Specific Effect Formulas

| Effect | Formula | Notes |
|--------|---------|-------|
| **BASE_DAMAGE_ROLL** | `min + rand(0, max-min)` | Integer roll within damage range |
| **DAMAGE_REDUCTION** | `damage × (1 - reduction)` | Applied multiplicatively; stacks with cap 0.9 |
| **EXTRA_DAMAGE** | `if value < 1: maxHP × value; else: value` | Added flat to total; can be % or flat |
| **RECOIL** | `damage × recoilPercent` | Attacker takes this much; grants 30% DR for 2 turn |
| **MISS_CHANCE** | `if random() < missChance: damage = 0` | Capped at 90%; cooldown still applies |
| **TURN_STEAL** | `if triggered: skip opponent's turn` | Grants immediate extra turn |
| **COOLDOWN_INCREASE** | `target_cooldown[i] += value` | Increases all enemy cooldowns |
| **HEAL** | `currentHP = min(currentHP + amount, maxHP)` | Direct heal; cannot exceed max |

### Stacking Rules
```
Total Reduction = min(Σ all reductions, 0.90)
Total Miss Chance = min(Σ all miss chances, 0.90)
Total Healing = sum of all heal effects (each applied separately)
All other effects: Apply independently (no stacking cap)
```

---

## 🎮 Game Flow

1. **Welcome Screen** → Shows initial splash
2. **Main Menu** → Play, Options, Exit
3. **Mode Selection** → PVE, PVP, or Arcade
4. **Character Selection** → Choose from 8 playable characters (5-10 sec timer)
5. **Battle Start** → First round begins
6. **Turn-based Combat** → Player + AI/Player 2 alternate turns
7. **Round/Match End** → Results displayed, reset for next round
8. **Victory/Defeat** → Return to menu or restart

---

## 🛠️ Technical Stack

- **Language:** Java 11+
- **GUI Framework:** Swing (JFrame, JPanel, JButton)
- **Build System:** IntelliJ IDEA (.iml project file)
- **Image Format:** PNG (UI buttons, backgrounds), GIF (character animations)

---

## 📊 Key Classes & Responsibilities

| Class | Purpose |
|---|---|
| `Character` | Base class defining HP, skills, damage logic |
| `Skill` | Encapsulates damage ranges, cooldowns, effects |
| `Skill.EffectType` | Enum of 9 effect types |
| `TurnManager` | Orchestrates turn-based combat |
| `CooldownManager` | Tracks & enforces skill cooldowns |
| `TurnResult` | Immutable result of turn execution |
| `StatusEffect` | Multi-turn effect persistence |
| `EnemyAI` | NPC decision logic |
| `BaseModeScreen` | Base for PVE/PVP/Arcade UIs |
| `ScreenManager` | Global window/screen management |

---

## 🚀 How to Run

```bash
javac -d bin src/**/*.java
java -cp bin encantadia.Main
```

Press **F11** during gameplay to toggle fullscreen.

---

## 📝 Notes

- All character stats (HP, damage) are hardcoded in constructor but can be easily modified
- Cooldown system prevents skill spam while maintaining strategic depth
- Effect system allows dynamic combat without hard-coding special abilities
- UI supports both windowed and fullscreen modes
- Animation system gracefully degrades if GIF assets are missing

---

**Project Version:** OOP2 Java Assignment  
**Last Updated:** April 29, 2026

