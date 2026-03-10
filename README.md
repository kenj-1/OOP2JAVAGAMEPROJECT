# OOP2JAVAPROJECT

### Game Title: Encantadia: Echoes of the Gem

## File Directory

```text
Encantadia-Origins/
│
├── README.md
├── .gitignore
├── LICENSE
│
├── docs/
│   ├── storyline.md
│   ├── characters.md
│   ├── mechanics.md
│   └── ui-flow.md
│
├── src/
│   └── encantadia/
│       ├── Main.java
│       │
│       ├── ui/
│       │   ├── MainMenuFrame.java
│       │   ├── BattleFrame.java
│       │   ├── CharacterSelectFrame.java
│       │   └── ResultDialog.java
│       │   └── gamemodeselection/
│       │       ├── ArcadeMode/
│       │       │   └──ArcadeMode.class
│       │       │   └──ArcadeMode.form  
│       │       ├── PVEMode/
│       │       │    └── PVEMode.java
│       │       │    └── PVEMode.form
│       │       │
│       │       └── PVPMode/
│       │           └── PVPMode.java
│       │           └── PVPMode.form
│       ├── characters/
│       │   ├── Character.java
│       │   ├── Mary.java
│       │   ├── Dirk.java
│       │   ├── MakelanShere.java
│       │   └── ...
│       │
│       ├── battle/
│       │   ├── TurnManager.java
│       │   ├── Skill.java
│       │   ├── CooldownManager.java
│       │   └── StatusEffect.java
│       │
│       └── utils/
│           ├── Dice.java
│           ├── InputValidator.java
│           └── Constants.java
│       
│       
│
└── assets/
    └── images/
        └── (optional later)