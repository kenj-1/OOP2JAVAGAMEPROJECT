package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Flamara extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    // TODO: Paste your exact GIF paths inside the quotes
    private static final String IDLE_PATH = "/resources/character/flamara/Flamara_Idle.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/flamara/Flamara_SKill1_FireBall.gif", // Skill 1: Fire Ball
            "/resources/character/flamara/Flamara_SKill2_FlameBurst.gif", // Skill 2: Flame Burst
            "/resources/character/flamara/Flamara_SKill3_InfernoStrike.gif"  // Skill 3: Inferno Strike
    };

    // TODO: Adjust these millisecond values to match the exact length of your GIFs
    private static final int[] SKILL_DURATIONS_MS = { 1400, 1400, 1600 };

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