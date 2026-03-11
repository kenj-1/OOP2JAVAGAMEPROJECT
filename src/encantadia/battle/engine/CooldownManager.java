package encantadia.battle.engine;

import encantadia.characters.Character;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages skill cooldowns for all registered characters.
 *
 * Each character has an int[] of cooldowns, one per skill slot.
 * A value of 0 means the skill is ready to use.
 *
 * Usage in TurnManager:
 *   cooldownManager.registerCharacter(player);
 *   cooldownManager.registerCharacter(enemy);
 *   ...
 *   cooldownManager.setCooldown(attacker, skillIndex, skill.getCooldown());
 *   cooldownManager.decrementCooldowns(attacker);
 */
public class CooldownManager {

    // Maps each character to an array of remaining cooldowns (one per skill slot)
    private final Map<Character, int[]> cooldownMap = new HashMap<>();

    /**
     * Registers a character and initializes all their skill cooldowns to 0 (ready).
     * Must be called before any other method for a given character.
     */
    public void registerCharacter(Character character) {
        int skillCount = character.getSkills().size();
        cooldownMap.put(character, new int[skillCount]);
    }

    /**
     * Returns true if the skill at the given index is currently on cooldown.
     */
    public boolean isOnCooldown(Character character, int skillIndex) {
        return getCooldowns(character)[skillIndex] > 0;
    }

    /**
     * Returns the number of turns remaining on a skill's cooldown (0 = ready).
     */
    public int getRemainingCooldown(Character character, int skillIndex) {
        return getCooldowns(character)[skillIndex];
    }

    /**
     * Sets the cooldown for a specific skill after it has been used.
     */
    public void setCooldown(Character character, int skillIndex, int turns) {
        getCooldowns(character)[skillIndex] = turns;
    }

    /**
     * Decrements all cooldowns for a character by 1 at the end of their turn.
     * Cooldowns never go below 0.
     */
    public void decrementCooldowns(Character character) {
        int[] cooldowns = getCooldowns(character);
        for (int i = 0; i < cooldowns.length; i++) {
            if (cooldowns[i] > 0) {
                cooldowns[i]--;
            }
        }
    }

    /**
     * Increases all active cooldowns for a character by the given amount.
     * Used by Dea's Whirlwind (COOLDOWN_INCREASE).
     * Skips skill at index 0 (basic attacks should never be locked out).
     */
    public void increaseCooldowns(Character character, int amount) {
        int[] cooldowns = getCooldowns(character);
        for (int i = 1; i < cooldowns.length; i++) {   // skip index 0
            if (cooldowns[i] > 0) {
                cooldowns[i] += amount;
            }
        }
    }

    /**
     * Decreases all cooldowns for a character by the given amount (minimum 0).
     * Used by Adamus's Tsunami Blast (COOLDOWN_REDUCTION).
     * Skips skill at index 0 per the character spec.
     */
    public void decreaseCooldowns(Character character, int amount) {
        int[] cooldowns = getCooldowns(character);
        for (int i = 1; i < cooldowns.length; i++) {   // skip index 0
            if (cooldowns[i] > 0) {
                cooldowns[i] = Math.max(0, cooldowns[i] - amount);
            }
        }
    }

    /**
     * Resets all cooldowns to 0 for a character (e.g. on battle start or restart).
     */
    public void resetCooldowns(Character character) {
        int[] cooldowns = getCooldowns(character);
        for (int i = 0; i < cooldowns.length; i++) {
            cooldowns[i] = 0;
        }
    }

    // ── Private helpers ───────────────────────────────────────

    private int[] getCooldowns(Character character) {
        int[] cooldowns = cooldownMap.get(character);
        if (cooldowns == null) {
            throw new IllegalStateException(
                    "Character '" + character.getName() + "' has not been registered with CooldownManager.");
        }
        return cooldowns;
    }
}