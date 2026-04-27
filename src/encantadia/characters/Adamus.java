package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Adamus extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    // TODO: Paste your exact GIF paths inside the quotes
    private static final String IDLE_PATH = "/resources/character/adamus/Adamus_Idle.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/adamus/Adamus_Skill1_WaterSpear.gif", // Skill 1: Water Spear
            "/resources/character/adamus/Adamus_Skill2_OceanWave.gif", // Skill 2: Ocean Wave
            "/resources/character/adamus/Adamus_Skill3_TsunamiCurse.gif"  // Skill 3: Tsunami Curse
    };

    // TODO: Adjust these millisecond values to match the exact length of your GIFs
    private static final int[] SKILL_DURATIONS_MS = { 1400, 1400, 1600 };

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

    // ── Animation Overrides ───────────────────────────────────────────────────
    @Override
    public String getIdleAnimationPath() {
        return IDLE_PATH.isEmpty() ? null : IDLE_PATH;
    }

    @Override
    public String[] getSkillAnimationPaths() {
        return SKILL_PATHS.clone();
    }

    @Override
    public int[] getSkillAnimationDurations() {
        return SKILL_DURATIONS_MS.clone();
    }
}