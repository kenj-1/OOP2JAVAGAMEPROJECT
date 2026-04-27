package encantadia.characters;

import encantadia.battle.skill.Skill;

/**
 * Mary  —  in-game display name "Claire", Goddess of Tides
 *
 * Animation assets
 * ────────────────
 * Place the following GIF files in your project's resources folder before
 * compiling.  Rename the uploaded files exactly as shown:
 *
 *   Upload filename                           →  Resource path
 *   ─────────────────────────────────────     ────────────────────────────────
 *   Claire_-_Idle_96x96.gif                  →  /resources/claire_idle.gif
 *   Claire_skill_1_384x96.gif                →  /resources/claire_skill_1.gif
 *   claire_skill_2_384x96.gif                →  /resources/claire_skill_2.gif
 *   claire_skill_3_384x96.gif  (any prefix)  →  /resources/claire_skill_3.gif
 *
 * GIF specs (measured):
 *   Idle   – 96×96  px, 16 frames, ~1620 ms loop
 *   Skill1 – 384×96 px, 14 frames,  1400 ms one-shot
 *   Skill2 – 384×96 px, 14 frames,  1400 ms one-shot
 *   Skill3 – 384×96 px, 16 frames,  1600 ms one-shot
 */
public class Mary extends Character {

    // ── Animation resource paths ──────────────────────────────────────────────
    private static final String IDLE_PATH  = "/resources/character/claire/Claire - idle 96x96.gif";

    private static final String[] SKILL_PATHS = {
            "/resources/character/claire/Claire skill 1 384x96.gif",   // Luha
            "/resources/character/claire/claire skill 2 384x96.gif",   // Flood Control
            "/resources/character/claire/claire skill 3 384x96.gif"    // Tsunami
    };

    /**
     * Exact measured total durations (ms) for each skill GIF.
     * CharacterAnimator adds a small padding before reverting to idle.
     */
    private static final int[] SKILL_DURATIONS_MS = {
            1400,   // Luha          – 14 frames × 100 ms
            1400,   // Flood Control – 14 frames × 100 ms
            1600    // Tsunami       – 16 frames × 100 ms
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public Mary() {
        super(
                "Claire",
                "Goddess of Tides",
                5000,
                "Born of moon and sea, Mary governs rhythm and balance. " +
                        "She feels Joygen's absence as a broken tide—an imbalance " +
                        "that threatens to drown Encantadia itself.\n\n"
        );

        // Skill 0 – Luha  (basic water bolt)
        skills.add(new Skill(
                "Luha", 220, 300, 0,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        // Skill 1 – Flood Control  (mid-range surge)
        skills.add(new Skill(
                "Flood Control", 320, 420, 2,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.NONE, 0, 0));

        // Skill 2 – Tsunami  (ultimate: high damage + 50 % turn-steal)
        skills.add(new Skill(
                "Tsunami", 525, 650, 3,
                Skill.SkillType.DAMAGE,
                Skill.EffectType.TURN_STEAL,
                1, 0.50));
    }

    // ── Animation overrides ───────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public String getIdleAnimationPath() {
        return IDLE_PATH;
    }

    /** {@inheritDoc} */
    @Override
    public String[] getSkillAnimationPaths() {
        return SKILL_PATHS.clone();         // defensive copy so callers can't mutate
    }

    /** {@inheritDoc} */
    @Override
    public int[] getSkillAnimationDurations() {
        return SKILL_DURATIONS_MS.clone();  // defensive copy
    }
}