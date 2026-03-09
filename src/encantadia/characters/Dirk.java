package encantadia.characters;

import encantadia.battle.Skill; // ✅ Fix 1: was "Skills"

public class Dirk extends Character {

    public Dirk() {

        super(
                "Dirk",
                "Guardian of Growth and Harvest",
                5000,
                "Emerging from the mountains, Dirk protects the land with unwavering devotion."
        );

        // ✅ Fix 2: Use correct 7-arg constructor with EffectType enum
        skills.add(new Skill("Bato Dela Rosa", 230, 310, 0,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Ding ang Bato!", 300, 400, 2,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Linog", 500, 650, 3,
                Skill.EffectType.DAMAGE_REDUCTION, 0.40, 1.0));
        //                                         ^^^^ 40% reduction, 100% proc chance
    }
}