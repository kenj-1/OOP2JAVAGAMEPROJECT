package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Dirk extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    private static final String IDLE_PATH = "/resources/character/dirk/Dirk - Idle 96x96.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/dirk/Dirk - Skill 1  384x96.gif",
            "/resources/character/dirk/Dirk - Skill 2 384x96.gif",
            "/resources/character/dirk/Dirk - Skill 3 384x96.gif"
    };

    // Placeholder durations (ms). Adjust these to perfectly match the GIF lengths once tested.
    private static final int[] SKILL_DURATIONS_MS = { 1400, 1400, 1600 };

    public Dirk() {
        super(
                "Dirk",
                "Guardian of Growth and Harvest",
                5000,
                "Emerging from the mountains, Dirk protects the land with unwavering devotion."
        );

        skills.add(new Skill("Bato Dela Rosa", 230, 310, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Ding ang Bato!", 300, 400, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Linog", 500, 650, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.DAMAGE_REDUCTION,
                0.40,
                1.0));
    }

    // ── Animation Overrides ───────────────────────────────────────────────────
    @Override
    public String getIdleAnimationPath() {
        return IDLE_PATH;
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