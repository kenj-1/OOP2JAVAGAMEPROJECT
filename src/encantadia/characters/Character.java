package encantadia.characters;

import encantadia.battle.skill.Skill;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base for every playable and enemy character.
 *
 * Animation contract (optional override)
 * ─────────────────────────────────────
 * Subclasses that have sprite GIFs should override the three animation methods
 * below.  The default implementations return null / empty, which signals to
 * {@code CharacterAnimator.forCharacter()} that no animator should be created,
 * so the battle frames fall back gracefully to their static portrait images.
 */
public abstract class Character {

    protected String name;
    protected String title;
    protected String backstory;

    protected int maxHP;
    protected int currentHP;

    protected List<Skill> skills;

    public Character(String name, String title, int maxHP, String backstory) {
        this.name      = name;
        this.title     = title;
        this.maxHP     = maxHP;
        this.currentHP = maxHP;
        this.backstory = backstory;
        this.skills    = new ArrayList<>();
    }

    // ══════════════════════════════════════════════════════════
    //  Animation hooks  (override in subclasses with GIF assets)
    // ══════════════════════════════════════════════════════════

    /**
     * Classpath path for the character's looping idle GIF.
     * Return {@code null} (default) if no idle animation is available.
     *
     * Example: {@code return "/resources/claire_idle.gif";}
     */
    public String getIdleAnimationPath() {
        return null;
    }

    /**
     * Classpath paths for each skill animation GIF, index-aligned with
     * {@link #getSkills()}.  Return {@code null} (default) if no skill
     * animations are available.
     *
     * Example: {@code return new String[]{
     *     "/resources/Claire skill 1 384x96.gif",
     *     "/resources/claire skill 2 384x96.gif",
     *     "/resources/claire skill 3 384x96.gif"
     * };}
     */
    public String[] getSkillAnimationPaths() {
        return null;
    }

    /**
     * Playback durations in milliseconds for each skill GIF, index-aligned
     * with {@link #getSkillAnimationPaths()}.
     * These values are used by {@code CharacterAnimator} to schedule the
     * automatic revert-to-idle timer.
     * Return {@code null} (default) to use the animator's built-in fallback
     * of 1600ms.
     *
     * Example: {@code return new int[]{1400, 1400, 1600};}
     */
    public int[] getSkillAnimationDurations() {
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  Core character mechanics
    // ══════════════════════════════════════════════════════════

    public void increaseMaxHP(int amount) {
        maxHP     += amount;
        currentHP += amount;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void takeDamage(int damage) {
        damage    = Math.max(0, damage);
        currentHP -= damage;
        if (currentHP < 0) currentHP = 0;
    }

    public void heal(int amount) {
        amount    = Math.max(0, amount);
        currentHP += amount;
        if (currentHP > maxHP) currentHP = maxHP;
    }

    public boolean isAlive() {
        return currentHP > 0;
    }

    public void reset() {
        currentHP = maxHP;
    }

    @Override
    public String toString() {
        return name + " (" + currentHP + "/" + maxHP + " HP)";
    }

    // ── Getters ───────────────────────────────────────────────
    public List<Skill> getSkills()      { return new ArrayList<>(skills); }
    public String      getName()        { return name;      }
    public String      getTitle()       { return title;     }
    public String      getBackstory()   { return backstory; }
    public int         getCurrentHP()   { return currentHP; }
    public int         getMaxHP()       { return maxHP;     }
}