package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Mary extends Character {

    public Mary() {

        super(
                "Claire",
                "Goddess of Tides",
                5000,
                "Born of moon and sea, Mary governs rhythm and balance. " +
                        """
                                She feels Joygen's absence as a broken tide—an imbalance that threatens to drown Encantadia itself.
                                
                                
                                """
        );

        // Skill 1: Luha — no effect
        skills.add(new Skill("Luha", 220, 300, 0,
                Skill.EffectType.NONE, 0, 0));

        // Skill 2: Flood Control — no effect
        skills.add(new Skill("Flood Control", 320, 420, 2,
                Skill.EffectType.NONE, 0, 0));

        // Skill 3: Tsunami — 50% chance to steal a turn
        skills.add(new Skill("Tsunami", 525, 650, 3,
                Skill.EffectType.TURN_STEAL, 1, 0.50));
    }
}