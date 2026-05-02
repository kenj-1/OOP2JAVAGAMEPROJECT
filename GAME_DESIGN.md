# Encantadia: Echoes of the Gems — Game Design Document

---

## 📖 Storyline

### **Main Narrative (Game Lore)**

Encantadia was once a realm of balance, ruled by four legendary Sang'gres sisters (Amihan, Alena, Danaya, and Perina) who wielded elemental power through magical gems called the Brilyantes. Through their sacrifice and unity, they brought peace to the four kingdoms: Lireo, Sapiro, Adamya, and Hathoria.

However, centuries of pressure on the Brilyantes caused them to evolve unpredictably. The gems began responding to **raw emotions across the realms** instead of royal bloodlines—rage in Hathoria, grief in Sapiro, ambition in Lireo, and fear in Adamya.

**The Crisis:** The Goddesses Jelian and Joygen, who maintained the Brilyantes' balance, suddenly vanished. Without their oversight, elemental chaos erupted:
- Storms devastated Lireo's forests
- Tides swallowed Sapiro's lowlands
- Adamya's earth split open
- Hathoria's flames burned uncontrolled

**The Discovery:** Two ancient guardians (Dirk and Mary) uncovered the truth—a hidden dimension called the **Gem Void** had formed from accumulated elemental strain. The Goddesses weren't stolen; they were **trapped within the Void itself**.

**The Chosen:** Instead of selecting royal heirs, the Brilyantes chose eight warriors whose personal sacrifices aligned with each element's deepest truth:
- **Flamara** — Unyielding fire that refused to dim
- **Dea** — Wind that still carries hope despite hardship
- **Tera** — Earth bearing weight without flinching
- **Adamus** — Tides shaped with both compassion and fury
- **Tyrone** — A young Sang'gre burning hotter than grief
- **Claire** — Precision and currents that never rush or stop
- **Dirk** — Guardian of growth even through destruction
- **Makelan** — A dangerous hunger willing to pay the price

**The Stakes:** The elemental warriors must venture into the Gem Void—a realm where normal Encantadia rules don't apply, where courage alone isn't enough, and where losing oneself to corrupted resonance means failing not just the Goddesses, but the entire realm. If they fail, Encantadia will collapse under the weight of its own magic.

---

## 🎮 Game Play

### **Core Mechanics**

Encantadia is a **turn-based tactical battle game** where players select a character and engage in elemental combat against AI opponents or other players. Each character has three unique skills with varying damage ranges, cooldown mechanics, and special effects.

### **Battle System**
1. **Character Selection** — Players choose from 8 elemental warriors
2. **Turn Structure** — Players and opponents alternate turns
3. **Skill Execution** — Each turn, a player selects and executes a skill
4. **Damage/Healing** — Skills deal damage or restore HP based on rolls and effects
5. **Status Effects** — Special abilities trigger procs (miss chance, damage reduction, extra damage, turn steal, etc.)
6. **Victory Condition** — Reduce opponent's HP to 0 (PVE) or win 3 rounds (PVP/Arcade)

### **Key Mechanics**
- **Damage Rolling**: Base damage is randomly calculated between min and max values
  - Formula: `baseDamage = minDamage + random(0, maxDamage - minDamage)`
- **Cooldowns**: Skills on cooldown cannot be used (Basic skill = 0CD, Intermediate = 2CD, Ultimate = 3CD)
- **Status Effects**: Multi-turn effects (Damage Reduction, Heal, Extra Damage, Turn Steal, Cooldown Increase, Recoil, Miss Chance)
- **Health Pool**: All characters have 5000 HP

### **Three Game Modes**

#### **PVE (Player vs Environment)**
- Single-player campaign against AI enemies
- 3-round format where player must defeat enemy 3 times
- Story-driven with lore progression
- Difficulty: Moderate (AI uses strategic skill selection)

#### **PVP (Player vs Player)**
- Two human players compete on same screen
- Best-of-3 rounds
- Player 1 (WASD/Q/E keys) vs Player 2 (Arrow keys)
- Story: Warriors test their resonance control in training

#### **ARCADE (Tower Mode)**
- Endless opponent sequence (tower climbing)
- Single-player continuously facing new enemies
- Victory at 6+ successful defeats or tower completion
- Story: Ancient trials testing combat readiness
- **Ultimate Skill Unlock**: After 6 consecutive victories, players unlock a 4th ultimate ability

---

## 🎯 Objectives of the Game

### **Short-term Objectives (Per Battle)**
1. **Select a character** that matches your playstyle
2. **Defeat your opponent(s)** by reducing their HP to 0
3. **Master skill rotations** by understanding cooldowns and effects
4. **Exploit status effects** (e.g., use DAMAGE_REDUCTION defensively, EXTRA_DAMAGE offensively)
5. **Win rounds** to progress through the game mode

### **Long-term Objectives**
1. **PVE Campaign**: Complete all story-driven encounters and rescue the Goddesses from the Gem Void
2. **PVP Mastery**: Learn to read opponent patterns and achieve matchup victory across character selections
3. **Arcade Tower**: Climb as high as possible without defeat and unlock the legendary ultimate skill
4. **Character Mastery**: Learn each of the 8 characters deeply—their damage ranges, optimal skill rotations, and when to use effects

### **Thematic Objective**
- **Restore balance to Encantadia** by proving that chosen warriors, not just royalty, can channel elemental power responsibly and protect the realm

---

## ⚙️ Rationale / Objectives / Mechanics of the Game

### **Design Rationale**

**Why Turn-Based Combat?**
- Allows strategic thinking and skill selection planning
- Reduces twitch-reaction requirements, emphasizing player intelligence
- Enables balanced cooldown and status effect mechanics that would be chaotic in real-time

**Why 8 Characters with Different Mechanics?**
- Each character carries the "weight" of their element thematically:
  - **Flamara (Fire)** → Extra Damage (aggressive element)
  - **Dea (Wind)** → Cooldown Increase (element of control/restriction)
  - **Tera (Earth)** → Heal (element of growth/regeneration)
  - **Adamus (Water)** → Miss Chance (element of evasion/flow)
  - **Claire (Water)** → Turn Steal (element of currents that control time)
  - **Dirk (Earth)** → Damage Reduction (element of protection/defense)
  - **Tyrone (Fire)** → Extra Damage (aggressive element variant)
  - **Makelan (Void)** → Recoil (element of imbalance/self-sacrifice)

**Why Cooldowns?**
- Prevents infinite spam of powerful skills
- Forces strategic decision-making about when to use ultimates
- Creates turn economy (every 3 turns, ultimate becomes available again)

**Why Status Effects?**
- Adds depth beyond "damage = damage"
- Creates counter-play (e.g., if opponent spams miss chance, damage reduction helps you overcome it)
- Makes matches feel dynamic—same character can play differently based on effects

---

### **Game Objectives (From a Mechanical Perspective)**

The game teaches and reinforces:

1. **Resource Management** — Manage cooldowns like mana/energy in traditional RPGs
2. **Probability & Variance** — Not all skills deal the same damage; RNG creates unexpected outcomes
3. **Pattern Recognition** — Learn which opponents prefer which skills at which times
4. **Counter-Play** — If opponent is using damage reduction, switch to extra damage skills
5. **Turn Economy** — Every turn matters; you might sacrifice HP now to guarantee a kill later

---

### **Core Game Mechanics Breakdown**

#### **1. Damage Calculation**
```
finalDamage = baseDamage
             × (1 - targetDamageReduction)
             × (1 - attackerSelfReduction)

Example:
  Base Damage: 500
  Target has 40% DR active: 500 × (1 - 0.4) = 300
  Attacker has -30% damage from RECOIL: 300 × (1 - 0.3) = 210
  Final: 210 damage dealt
```

#### **2. Cooldown Lifecycle**
```
Turn 1: Use Ultimate (Cooldown = 3)
Turn 2: Cooldown = 2 (skill unavailable)
Turn 3: Cooldown = 1 (skill unavailable)
Turn 4: Cooldown = 0 (skill available again)
```

#### **3. Status Effect Application**
- **Guaranteed (100% proc)**: HEAL, RECOIL, EXTRA_DAMAGE (some skills)
- **Probabilistic**: MISS_CHANCE (45%), TURN_STEAL (50%), COOLDOWN_INCREASE (45%)
- **Duration**: Most effects last 2-3 turns before expiring
- **Stacking**: Multiple reductions stack multiplicatively with 90% cap (prevents 1-shot immunity)

#### **4. Nine Effect Types & Their Impact**

| Effect | Mechanic | Counter |
|--------|----------|---------|
| **NONE** | No secondary effect | — |
| **EXTRA_DAMAGE** | +flat damage bonus | Heal, Damage Reduction |
| **DAMAGE_REDUCTION** | -(%)incoming damage | Extra Damage, Ignore effects |
| **HEAL** | +HP restoration (100% proc) | Can't be countered; pure utility |
| **TURN_STEAL** | Skip opponent's next turn (50% proc) | Accept lost turn, plan around it |
| **COOLDOWN_INCREASE** | Force +1 cooldown on opponent (45% proc) | Accept delayed skills, adjust tactics |
| **MISS_CHANCE** | Force opponent's next attack to miss (45% proc) | Wait for effect to expire, use guaranteed skills |
| **RECOIL** | Attacker takes % of damage dealt + -30% DMG for 2 turns | Use sparingly; self-damaging but powerful |
| **COOLDOWN_REDUCTION** | Reset own cooldowns (reserved for future) | — |

#### **5. Victory Conditions by Mode**
- **PVE**: Reduce enemy HP to 0, repeat 3 times
- **PVP**: Win 3 rounds (best-of-3)
- **Arcade**: Defeat 6+ consecutive opponents; on 7th victory, unlock ultimate 4th skill

#### **6. Character Identity Through Numbers**

**Flamara (Fire - Offensive)**
- Highest base damage skills (280-360, 380-480, 560-700)
- Extra Damage proc adds 200 bonus
- Playstyle: Aggressive damage dealer

**Tera (Earth - Supportive)**
- Moderate damage (230-310, 300-380, 500-650)
- Ultimate heals 450 HP (guaranteed)
- Playstyle: Tank/healer hybrid

**Dirk (Earth - Defensive)**
- Moderate damage (230-310, 300-400, 500-650)
- Ultimate grants 40% damage reduction
- Playstyle: Wall/tank

**Adamus (Water - Evasive)**
- Balanced damage (220-300, 300-450, 600-650)
- Ultimate 45% miss chance on opponent's next attack
- Playstyle: Reactive/defensive

**Claire (Water - Control)**
- Balanced damage (220-300, 320-420, 525-650)
- Ultimate 50% chance to steal an extra turn
- Playstyle: Tempo control/momentum

**Dea (Wind - Restrictive)**
- Moderate damage (220-300, 320-400, 500-620)
- Ultimate 45% chance to increase opponent cooldowns
- Playstyle: Debuff/control

**Tyrone (Fire - Offensive)**
- High damage (260-340, 360-450, 525-650)
- Extra Damage 100% proc (+100 bonus)
- Playstyle: Reliable damage dealer

**Makelan (Void - Risky)**
- Highest ultimate damage (550-700)
- Recoil 15% self-damage (guaranteed)
- Playstyle: High-risk, high-reward burst

---

### **Why This Design Works**

1. **Accessibility**: New players can button-mash and still win through luck/high damage
2. **Depth**: Experienced players master cooldown timing, effect counters, and matchup-specific strategies
3. **Replayability**: RNG damage rolls + multiple characters = different outcomes even in same matchups
4. **Narrative Integration**: Each mechanic reinforces the lore (elemental themes, Gem Void chaos, character backstories)
5. **Balancing**: No character is strictly "best"—each has favorable and unfavorable matchups, creating a rock-paper-scissors-like meta

---

## 🔄 Game Flow Summary

```
Welcome Screen
    ↓
Main Menu (Play / Options / Exit)
    ↓
Mode Selection (PVE / PVP / Arcade)
    ↓
Story Prompt (Thematic narrative introduction)
    ↓
Character Selection (8 characters, 10-second timer)
    ↓
Battle Start
    ├─ Round 1: Player vs Opponent
    ├─ Round 2: Reset HP, repeat
    └─ Round 3: Determine winner
    ↓
Results Screen (Victory/Defeat modal)
    ↓
Return to Mode Selection or Main Menu
```

---

## 📊 Quick Reference: 8 Warriors & Their Elements

| Warrior | Element | Role | Ultimate Effect | HP |
|---------|---------|------|------------------|-----|
| Flamara | Fire | Attacker | +200 Bonus Damage | 5000 |
| Tyrone | Fire | Attacker | +100 Bonus Damage | 5000 |
| Adamus | Water | Evader | 45% Miss Chance | 5000 |
| Claire | Water | Controller | 50% Turn Steal | 5000 |
| Tera | Earth | Support | Heal 450 HP | 5000 |
| Dirk | Earth | Tank | 40% Damage Reduction | 5000 |
| Dea | Wind | Debuffer | 45% Cooldown +1 | 5000 |
| Makelan | Void | Glass Cannon | 15% Recoil Damage | 5000 |

---

**Document Version**: Game Design v1.0  
**Last Updated**: May 2, 2026  
**Project**: Encantadia – Echoes of the Gems

