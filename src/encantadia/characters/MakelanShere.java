package encantadia.characters;

import encantadia.battle.skill.Skill;

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

        skills.add(new Skill("Lead Heel", 240, 320, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Savor Thy Flesh", 340, 450, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Gouged Petunia", 550, 700, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.RECOIL,
                0.15,
                1.0));
    }
}