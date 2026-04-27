package encantadia.characters;

import encantadia.battle.skill.Skill;

public class Tera extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    // TODO: Paste your exact GIF paths inside the quotes
    private static final String IDLE_PATH = "/resources/character/tera/Tera_Idle.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/tera/Tera_Skill1_RockSmash.gif", // Skill 1: Rock Smash
            "/resources/character/tera/Tera_Skill2_Earthquake.gif", // Skill 2: Earthquake
            "/resources/character/tera/Tera_Skill3_NaturesWrath.gif"  // Skill 3: Nature's Wrath
    };

    // TODO: Adjust these millisecond values to match the exact length of your GIFs
    private static final int[] SKILL_DURATIONS_MS = { 1400, 1400, 1600 };

    public Tera() {

        super(
                "Tera",
                "Daughter of the First Mountain Stone",
                5000,
                "Tera was born from the first mountain stone kissed by dawn, embodying endurance and the unyielding " +
                        "spirit of the land. With Jelian missing, the earth cries out in instability, yet Tera's steadfast " +
                        "nature ensures she remains the shield that guards against total collapse."
        );

        skills.add(new Skill("Rock Smash", 230, 310, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        skills.add(new Skill("Earthquake", 300, 380, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        // FIX: heal properly
        skills.add(new Skill("Nature's Wrath", 500, 650, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.HEAL,
                450,
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