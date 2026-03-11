package encantadia.battle.skill;

import java.util.Random;

public class Skill {

    public enum SkillType {
        DAMAGE,
        HEAL
    }

    public enum EffectType {
        NONE,
        EXTRA_DAMAGE,
        HEAL,
        TURN_STEAL,
        COOLDOWN_INCREASE,
        DAMAGE_REDUCTION,
        RECOIL,
        COOLDOWN_REDUCTION
    }

    private String name;

    private int minValue;
    private int maxValue;

    private int cooldown;

    private SkillType skillType;
    private EffectType effectType;
    private double effectValue;
    private double effectChance;

    // Constructor for DAMAGE skills
    public Skill(String name, int minValue, int maxValue, int cooldown,
                 EffectType effectType, double effectValue, double effectChance) {
        this(name, minValue, maxValue, cooldown, SkillType.DAMAGE, effectType, effectValue, effectChance);
    }

    // Full constructor (used for HEAL skills like Adamus)
    public Skill(String name, int minValue, int maxValue, int cooldown,
                 SkillType skillType, EffectType effectType, double effectValue, double effectChance) {
        this.name = name;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.cooldown = cooldown;
        this.skillType = skillType;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.effectChance = effectChance;
    }

    public int rollValue() {
        Random rand = new Random();
        return rand.nextInt(maxValue - minValue + 1) + minValue;
    }

    public boolean effectTriggered() {
        Random rand = new Random();
        return rand.nextDouble() <= effectChance;
    }

    public String getName() { return name; }
    public int getCooldown() { return cooldown; }
    public SkillType getSkillType() { return skillType; }
    public EffectType getEffectType() { return effectType; }
    public double getEffectValue() { return effectValue; }
    public int getMinValue() { return minValue; }
    public int getMaxValue() { return maxValue; }
}