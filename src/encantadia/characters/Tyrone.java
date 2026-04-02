package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Tyrone extends Character {

    public Tyrone() {

        super(
                "Tyrone",
                "Fire Sang'gre of Hathoria",
                5000,
                "A Fire Sang'gre born in the kingdom of Hathoria, dreaming of becoming known throughout Encantadia. " +
                        "Though the fire grew weaker after the ritual, Tyrone vowed to restore its strength and uncover " +
                        "the truth behind Joygen's disappearance, embarking on a journey that would test his courage, skill, and heart."
        );

        skills.add(new Skill("Fire Blaze", 260, 340, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Fire Beam", 360, 450, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        // FIX: % damage simulated as flat bonus
        skills.add(new Skill("Fire Burst", 525, 650, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.EXTRA_DAMAGE,
                100,
                1.0));
    }
}