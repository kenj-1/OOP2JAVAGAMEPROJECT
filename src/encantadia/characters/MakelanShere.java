package encantadia.characters;

import encantadia.battle.Skill;

public class MakelanShere extends Character {

    public MakelanShere() {

        super(
                "Makelan Shere",
                "Devourer of the Crimson Bloom",
                5000,
                "Born of hunger and divine indulgence, Makelan embodies excess and consequence. " +
                        "He views battle as a feast and pain as a sacred offering. " +
                        "Each strike feeds his power, yet every overwhelming assault demands a price from his own flesh."
        );

        // Skill 1: Lead Heel — no effect
        skills.add(new Skill("Lead Heel", 240, 320, 0,
                Skill.EffectType.NONE, 0, 0));

        // Skill 2: Savor Thy Flesh — no effect
        skills.add(new Skill("Savor Thy Flesh", 340, 450, 2,
                Skill.EffectType.NONE, 0, 0));

        // Skill 3: Gouged Petunia — 15% recoil damage (always triggers)
        // Secondary: reduces own damage by 30% for 2 turns (handled in TurnManager/StatusEffect)
        skills.add(new Skill("Gouged Petunia", 550, 700, 3,
                Skill.EffectType.RECOIL, 0.15, 1.0));
    }
}