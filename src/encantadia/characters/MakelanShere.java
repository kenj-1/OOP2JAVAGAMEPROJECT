package encantadia.characters;

import encantadia.battle.skill.Skill;

public class MakelanShere extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    private static final String IDLE_PATH = "/resources/character/makelanShere/Makelan - Idle 96x96.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/makelanShere/Makelan - Skill 1 384x96.gif",
            "/resources/character/makelanShere/Makelan - Skill 2 384x96.gif",
            "/resources/character/makelanShere/Makelan - Skill 3 384x96.gif"
    };

    // Placeholder durations (ms). Adjust these to perfectly match the GIF lengths once tested.
    private static final int[] SKILL_DURATIONS_MS = { 1400, 1400, 1600 };

    public MakelanShere() {
        super(
                "Makelan Shere",
                "Devourer of the Crimson Bloom",
                5000,
                "Born of hunger and divine indulgence, Makelan embodies excess and consequence. " +
                        "He views battle as a feast and pain as a sacred offering. " +
                        "Each strike feeds his power, yet every overwhelming assault demands a price from his own flesh."
        );

        skills.add(new Skill("Lead Heel", 240, 320, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Savor Thy Flesh", 340, 450, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Gouged Petunia", 550, 700, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.RECOIL,
                0.15,
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