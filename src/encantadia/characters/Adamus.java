package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Adamus extends Character {

    public Adamus() {

        super(
                "Adamus",
                "Guardian of Rivers and Lakes",
                5000,
                "Adamus, guardian of rivers and lakes, carries the voice of the oceans themselves. " +
                        "Shaped by betrayal and loss, he channels both compassion and power. The oceans wail in " +
                        "response to Joygen's absence, yet Adamus remains resolute, determined to restore harmony " +
                        "to water and fire alike."
        );

        // FIX: These are DAMAGE, not HEAL
        skills.add(new Skill("Water Spear", 220, 300, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Ocean Wave", 300, 450, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Tsunami Curse", 600, 650, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.MISS_CHANCE,
                0.45,
                1.0));

    }
}