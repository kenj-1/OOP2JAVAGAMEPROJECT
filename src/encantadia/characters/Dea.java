package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Dea extends Character {

    public Dea() {




        super(
                "Dea",
                "Daughter of the Northern Winds",
                5000,
                "Born of the northern winds and nurtured by the whispers of the sky, Dea commands the currents " +
                        "with precision and foresight. Calm and observant, she senses disturbances across the realms long " +
                        "before they manifest. The disappearance of Jelian weighs heavily on her, as the winds now carry " +
                        "only silence where once they carried guidance."
        );

        skills.add(new Skill("Wind Slash", 220, 300, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Storm Fury", 320, 400, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Whirlwind", 500, 620, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.COOLDOWN_INCREASE,
                1,
                0.45));
    }
}