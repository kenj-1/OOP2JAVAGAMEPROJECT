package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Flamara extends Character {

    public Flamara() {

        super(
                "Flamara",
                "Bearer of Starlit Flames",
                5000,
                "Gifted with flames born from starlit embers, Flamara's fire burns with a fierce, unyielding passion. " +
                        "The absence of Joygen dims the brilliance of her power, yet it strengthens her resolve to rescue " +
                        "the goddess who once taught her the true mastery of fire."
        );

        skills.add(new Skill("Fire Ball", 280, 360, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Flame Burst", 380, 480, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        // FIX: replaced EXECUTE → EXTRA_DAMAGE
        skills.add(new Skill("Inferno Strike", 560, 700, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.EXTRA_DAMAGE,
                200,
                0.40));
    }
}