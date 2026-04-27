package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Dea extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    // TODO: Paste your exact GIF paths inside the quotes
    private static final String IDLE_PATH = "/resources/character/dea/Dea_Idle.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/dea/Dea_Skill1_WindSlash.gif", // Skill 1: Wind Slash
            "/resources/character/dea/Dea_Skill2_StormFury.gif", // Skill 2: Storm Fury
            "/resources/character/dea/Dea_SKill3_WhirlWind.gif"  // Skill 3: Whirlwind
    };

    // TODO: Adjust these millisecond values to match the exact length of your GIFs
    private static final int[] SKILL_DURATIONS_MS = { 1400, 1400, 1600 };

    public Dea() {

        super(
                "Dea",
                "Daughter of the Northern Winds",
                5000,
                "Born of the northern winds and nurtured by the whispers of the sky, Dea commands the currents " +
                        "with precision and foresight. Calm and observant, she senses disturbances across the realms long " +
                        "before they manifest. The disappearance of Jelian weighs heavily on her, as the winds now carry " +
                        "only silence where once they carried guidance."
        );

        skills.add(new Skill("Wind Slash", 220, 300, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Storm Fury", 320, 400, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Whirlwind", 500, 620, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.COOLDOWN_INCREASE,
                1,
                0.45));
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