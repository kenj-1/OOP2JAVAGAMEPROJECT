package encantadia.battle.skill;

import java.util.Random;

public class Skill {

    public enum SkillType {
        DAMAGE,
        HEAL
    }

    public enum EffectType {
        NONE,
        DAMAGE_REDUCTION,
        HEAL,
        EXTRA_DAMAGE,
        TURN_STEAL,
        COOLDOWN_INCREASE,
        COOLDOWN_REDUCTION,
        RECOIL,
        MISS_CHANCE
    }

    private final String name;
    private final int minDamage;
    private final int maxDamage;
    private final SkillType skillType;
    private final EffectType effectType;
    private final double effectValue;
    private final double procChance;
    private final int cooldown;

    private static final Random random = new Random();

    public Skill(String name,
                 int minDamage, int maxDamage,
                 int cooldown,
                 EffectType effectType,
                 double effectValue, double procChance) {

        this(name, minDamage, maxDamage, cooldown,
                SkillType.DAMAGE, effectType, effectValue, procChance);
    }

    public Skill(String name,
                 int minDamage, int maxDamage,
                 int cooldown,
                 SkillType skillType,
                 EffectType effectType,
                 double effectValue, double procChance) {

        this.name = name;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.cooldown = Math.max(0, cooldown);
        this.skillType = skillType;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.procChance = Math.max(0, Math.min(1, procChance));
    }

    public int rollValue() {
        return minDamage == maxDamage
                ? minDamage
                : minDamage + random.nextInt(maxDamage - minDamage + 1);
    }

    public boolean effectTriggered() {
        return procChance >= 1.0 || (procChance > 0 && random.nextDouble() < procChance);
    }

    public String getName() { return name; }
    public int getMinDamage() { return minDamage; }
    public int getMaxDamage() { return maxDamage; }
    public int getCooldown() { return cooldown; }
    public SkillType getSkillType() { return skillType; }
    public EffectType getEffectType() { return effectType; }
    public double getEffectValue() { return effectValue; }
    public double getProcChance() { return procChance; }
}