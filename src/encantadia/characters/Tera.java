package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Tera extends Character {

    public Tera() {

        super(
                "Tera",
                "Daughter of the First Mountain Stone",
                5000,
                "Tera was born from the first mountain stone kissed by dawn, embodying endurance and the unyielding " +
                        "spirit of the land. With Jelian missing, the earth cries out in instability, yet Tera's steadfast " +
                        "nature ensures she remains the shield that guards against total collapse."
        );

        // Skill 1: Rock Smash — no effect
        skills.add(new Skill("Rock Smash", 230, 310, 0,
                Skill.EffectType.NONE, 0, 0));

        // Skill 2: Earthquake — no effect
        skills.add(new Skill("Earthquake", 300, 380, 2,
                Skill.EffectType.NONE, 0, 0));

        // Skill 3: Nature's Wrath — always heals self for 300 HP after dealing damage
        skills.add(new Skill("Nature's Wrath", 500, 650, 3,
                Skill.EffectType.HEAL, 300, 1.0));
    }
}