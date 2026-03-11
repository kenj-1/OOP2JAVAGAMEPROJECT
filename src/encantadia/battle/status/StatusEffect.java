package encantadia.battle.status;

import encantadia.battle.skill.Skill;

/**
 * Represents a single active status effect applied to a character.
 * Effects are created by TurnManager when a skill's EffectType triggers,
 * and are ticked/removed each turn.
 *
 * Examples:
 *   - Dirk's Linog       → DAMAGE_REDUCTION 40% on enemy, 1 turn
 *   - MakelanShere Recoil→ DAMAGE_REDUCTION 30% on self, 2 turns
 */
public class StatusEffect {

    public enum EffectTarget {
        SELF,    // effect applies to the character who cast the skill
        ENEMY    // effect applies to the opposing character
    }

    private final Skill.EffectType type;
    private final double value;         // e.g. 0.40 for 40% damage reduction
    private final EffectTarget target;
    private int turnsRemaining;

    public StatusEffect(Skill.EffectType type, double value, EffectTarget target, int turnsRemaining) {
        this.type           = type;
        this.value          = value;
        this.target         = target;
        this.turnsRemaining = turnsRemaining;
    }

    /**
     * Called at the end of each turn to count down the effect duration.
     */
    public void tick() {
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
    }

    public boolean isExpired() {
        return turnsRemaining <= 0;
    }

    // ── Getters ──────────────────────────────────────────────

    public Skill.EffectType getType()    { return type; }
    public double getValue()             { return value; }
    public EffectTarget getTarget()      { return target; }
    public int getTurnsRemaining()       { return turnsRemaining; }

    @Override
    public String toString() {
        return String.format("[%s | value=%.2f | turns left=%d | on=%s]",
                type, value, turnsRemaining, target);
    }
}