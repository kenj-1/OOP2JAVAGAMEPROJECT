package encantadia.battle.status;

import encantadia.battle.skill.Skill;

public class StatusEffect {

    public enum EffectTarget {
        SELF,
        ENEMY
    }

    private final Skill.EffectType type;
    private final double value;
    private final EffectTarget target;
    private int turnsRemaining;

    public StatusEffect(Skill.EffectType type, double value,
                        EffectTarget target, int turnsRemaining) {

        this.type = type;
        this.value = value;
        this.target = target;
        this.turnsRemaining = Math.max(0, turnsRemaining);
    }

    public void tick() {
        if (turnsRemaining > 0) turnsRemaining--;
    }

    public boolean isExpired() {
        return turnsRemaining <= 0;
    }

    public Skill.EffectType getType() { return type; }
    public double getValue() { return value; }
    public EffectTarget getTarget() { return target; }
    public int getTurnsRemaining() { return turnsRemaining; }

    @Override
    public String toString() {
        return String.format("[%s | %.2f | %d turns | %s]",
                type, value, turnsRemaining, target);
    }
}