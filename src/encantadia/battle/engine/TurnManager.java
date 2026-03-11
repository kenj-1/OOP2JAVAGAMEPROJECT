package encantadia.battle.engine;

import encantadia.battle.skill.Skill;
import encantadia.battle.status.StatusEffect;
import encantadia.battle.result.TurnResult;
import encantadia.characters.Character;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TurnManager orchestrates the entire battle loop.
 *
 * Responsibilities:
 *   - Tracks whose turn it is (player vs enemy)
 *   - Validates skill availability via CooldownManager
 *   - Rolls damage/heal values from Skill
 *   - Applies active StatusEffects before dealing damage
 *   - Processes all EffectTypes after the hit
 *   - Sets skill cooldowns after use
 *   - Ticks and cleans up expired StatusEffects each turn
 *   - Returns a TurnResult for BattleFrame to consume
 *
 * How BattleFrame uses this:
 *   TurnResult result = turnManager.executeSkill(attacker, target, skillIndex);
 *   for (String msg : result.getLogMessages()) { battleLog.append(msg); }
 *   if (result.isTurnStolen()) { // don't switch turns }
 *   if (result.isTargetDefeated()) { // show result screen }
 */
public class TurnManager {

    private final Character player;
    private final Character enemy;
    private final CooldownManager cooldownManager;

    // Parallel lists: each effect has a corresponding owner (Character who applied it)
    private final List<StatusEffect> activeEffects = new ArrayList<>();
    private final List<Character>    effectOwners  = new ArrayList<>();

    // true = player's turn, false = enemy's turn
    private boolean playerTurn;

    public TurnManager(Character player, Character enemy) {
        this.player     = player;
        this.enemy      = enemy;
        this.playerTurn = true;

        this.cooldownManager = new CooldownManager();
        this.cooldownManager.registerCharacter(player);
        this.cooldownManager.registerCharacter(enemy);
    }

    // ── Public API ────────────────────────────────────────────

    /**d
     * Executes a skill for the attacker against the target.
     *
     * @param attacker   The character using the skill
     * @param target     The opposing character
     * @param skillIndex Index into attacker.getSkills() (0, 1, or 2)
     * @return TurnResult describing everything that happened
     */
    public TurnResult executeSkill(Character attacker, Character target, int skillIndex) {

        TurnResult.Builder result = new TurnResult.Builder()
                .attackerName(attacker.getName());

        // ── Guard: validate index ─────────────────────────────
        List<Skill> skills = attacker.getSkills();
        if (skillIndex < 0 || skillIndex >= skills.size()) {
            return result.log("Invalid skill index: " + skillIndex).build();
        }

        Skill skill = skills.get(skillIndex);
        result.skillUsed(skill.getName());

        // ── Guard: check cooldown ─────────────────────────────
        if (cooldownManager.isOnCooldown(attacker, skillIndex)) {
            int cd = cooldownManager.getRemainingCooldown(attacker, skillIndex);
            return result
                    .log(skill.getName() + " is on cooldown (" + cd + " turn(s) remaining).")
                    .build();
        }

        // ── Roll base value ───────────────────────────────────
        int baseValue = skill.rollValue();
        result.baseDamageOrHeal(baseValue);
        result.log(attacker.getName() + " uses " + skill.getName() + "!");

        // ── Branch by skill type ──────────────────────────────
        if (skill.getSkillType() == Skill.SkillType.HEAL) {
            processHeal(attacker, skill, skillIndex, baseValue, result);
        } else {
            processDamage(attacker, target, skill, skillIndex, baseValue, result);
        }

        // ── Tick all status effects at end of this action ─────
        tickAndCleanEffects(result);

        return result.build();
    }

    /**
     * Switches turns and decrements cooldowns for the next character.
     * Call this ONLY when result.isTurnStolen() is false.
     */
    public void advanceTurn() {
        playerTurn = !playerTurn;
        Character next = playerTurn ? player : enemy;
        cooldownManager.decrementCooldowns(next);
    }

    public boolean isPlayerTurn()               { return playerTurn; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public List<StatusEffect> getActiveEffects(){ return List.copyOf(activeEffects); }

    // ── Damage flow ───────────────────────────────────────────

    private void processDamage(Character attacker, Character target,
                               Skill skill, int skillIndex,
                               int baseValue, TurnResult.Builder result) {
        int damage = baseValue;

        // Self-debuff: MakelanShere's recoil reduces his own outgoing damage
        double selfReduction = getSelfDamageReduction(attacker);
        if (selfReduction > 0) {
            int cut = (int) Math.round(damage * selfReduction);
            damage -= cut;
            result.log(attacker.getName() + "'s power is weakened — damage cut by "
                    + (int)(selfReduction * 100) + "% (-" + cut + ")");
        }

        // Enemy shield: Dirk's Linog placed a reduction debuff on the attacker
        double enemyReduction = getEnemyDamageReduction(attacker);
        if (enemyReduction > 0) {
            int blocked = (int) Math.round(damage * enemyReduction);
            damage -= blocked;
            result.log(attacker.getName() + "'s damage is shielded — blocked " + blocked + "!");
        }

        // Apply damage
        target.takeDamage(damage);
        result.totalDamageDealt(damage);
        result.log(attacker.getName() + " hits for " + damage
                + " | " + target.getName() + " HP: " + target.getCurrentHP()
                + "/" + target.getMaxHP());

        // Secondary effect
        if (skill.getEffectType() != Skill.EffectType.NONE && skill.effectTriggered()) {
            applyEffect(attacker, target, skill, damage, result);
        }

        // Set cooldown
        cooldownManager.setCooldown(attacker, skillIndex, skill.getCooldown());

        // Defeat check
        if (!target.isAlive()) {
            result.targetDefeated(true);
            result.log("★ " + target.getName() + " has been defeated!");
        }
    }

    // ── Heal flow (Adamus) ────────────────────────────────────

    private void processHeal(Character healer, Skill skill,
                             int skillIndex, int baseValue,
                             TurnResult.Builder result) {
        healer.heal(baseValue);
        result.totalHealApplied(baseValue);
        result.log(healer.getName() + " restores " + baseValue + " HP"
                + " | HP: " + healer.getCurrentHP() + "/" + healer.getMaxHP());

        // Adamus's Tsunami Blast: reset cooldowns of other skills
        if (skill.getEffectType() == Skill.EffectType.COOLDOWN_REDUCTION && skill.effectTriggered()) {

            for (int i = 0; i < healer.getSkills().size(); i++) {

                if (i == skillIndex) {
                    continue;
                }

                int remaining = cooldownManager.getRemainingCooldown(healer, i);

                if (remaining > 0) {
                    cooldownManager.setCooldown(healer, i, 0);
                }
            }

            result.effectTriggered(true);
            result.effectDescription("Tidal surge resets Adamus' skills!");
            result.log("🌊 All other skill cooldowns are refreshed!");
        }

        cooldownManager.setCooldown(healer, skillIndex, skill.getCooldown());
    }

    // ── Effect dispatch ───────────────────────────────────────

    private void applyEffect(Character attacker, Character target,
                             Skill skill, int damageDealt,
                             TurnResult.Builder result) {
        result.effectTriggered(true);
        double value = skill.getEffectValue();

        switch (skill.getEffectType()) {

            case EXTRA_DAMAGE -> {
                // Tyrone: Fire Burst +250 | Flamara: Inferno Strike +300
                int bonus = (int) value;
                target.takeDamage(bonus);
                result.extraDamage(bonus);
                result.effectDescription(attacker.getName() + " unleashes extra power: +" + bonus + "!");
                result.log("Bonus hit! " + target.getName() + " takes +" + bonus
                        + " | HP: " + target.getCurrentHP() + "/" + target.getMaxHP());

                if (!target.isAlive()) {
                    result.targetDefeated(true);
                    result.log("★ " + target.getName() + " is defeated by the bonus strike!");
                }
            }

            case TURN_STEAL -> {
                // Claire's Tsunami: 50% chance to go again
                result.turnStolen(true);
                result.effectDescription(attacker.getName() + " rides the wave and strikes again!");
                result.log("⚡ TURN STEAL! " + attacker.getName() + " acts again!");
            }

            case COOLDOWN_INCREASE -> {
                // Dea's Whirlwind: 45% chance to increase enemy's active cooldowns by 1
                int amount = (int) value;
                cooldownManager.increaseCooldowns(target, amount);
                result.effectDescription(target.getName() + "'s cooldowns extended by " + amount + " turn(s)!");
                result.log("🌀 " + target.getName() + "'s skills are disrupted! Cooldowns +" + amount + ".");
            }

            case DAMAGE_REDUCTION -> {
                // Dirk's Linog: enemy damage reduced by 40% for 1 turn
                // Owner = attacker (Dirk), effect targets ENEMY = the target
                addEffect(new StatusEffect(
                        Skill.EffectType.DAMAGE_REDUCTION,
                        value,
                        StatusEffect.EffectTarget.ENEMY,
                        1
                ), attacker);
                int pct = (int)(value * 100);
                result.effectDescription(target.getName() + "'s damage reduced by " + pct + "% for 1 turn!");
                result.log("🛡 " + target.getName() + " is weakened! Outgoing damage -" + pct + "% next turn.");
            }

            case RECOIL -> {
                // MakelanShere's Gouged Petunia:
                //   Part 1 — attacker takes 15% of damage dealt as recoil
                int recoil = (int) Math.round(damageDealt * value);
                attacker.takeDamage(recoil);
                result.recoilDamage(recoil);
                result.log(attacker.getName() + " suffers " + recoil + " recoil damage"
                        + " | HP: " + attacker.getCurrentHP() + "/" + attacker.getMaxHP());

                //   Part 2 — attacker's own damage reduced by 30% for 2 turns (self-debuff)
                addEffect(new StatusEffect(
                        Skill.EffectType.DAMAGE_REDUCTION,
                        0.30,
                        StatusEffect.EffectTarget.SELF,
                        2
                ), attacker);
                result.effectDescription("Excess! " + attacker.getName()
                        + " takes " + recoil + " recoil and deals 30% less damage for 2 turns.");
                result.log(attacker.getName() + "'s power is consumed — damage -30% for 2 turns.");
            }

            case HEAL -> {
                // Tera's Nature's Wrath: heals self after dealing damage
                int healAmt = (int) value;
                attacker.heal(healAmt);
                result.totalHealApplied(healAmt);
                result.effectDescription("The earth restores " + attacker.getName() + " for " + healAmt + " HP!");
                result.log("🌿 " + attacker.getName() + " draws power from the earth: +" + healAmt + " HP"
                        + " | HP: " + attacker.getCurrentHP() + "/" + attacker.getMaxHP());
            }

            default -> { /* NONE */ }
        }
    }

    // ── StatusEffect helpers ──────────────────────────────────

    /** Registers a new effect alongside its owner for targeting lookups. */
    private void addEffect(StatusEffect effect, Character owner) {
        activeEffects.add(effect);
        effectOwners.add(owner);
    }

    /**
     * Returns total SELF DAMAGE_REDUCTION active on a character.
     * Used for MakelanShere's recoil self-debuff.
     *
     * @param character The attacker whose outgoing damage may be reduced
     */
    private double getSelfDamageReduction(Character character) {
        double total = 0;
        for (int i = 0; i < activeEffects.size(); i++) {
            StatusEffect e = activeEffects.get(i);
            if (e.getType()   == Skill.EffectType.DAMAGE_REDUCTION
                    && e.getTarget() == StatusEffect.EffectTarget.SELF
                    && effectOwners.get(i) == character) {
                total += e.getValue();
            }
        }
        return total;
    }

    /**
     * Returns total ENEMY DAMAGE_REDUCTION active on a character.
     * Used for Dirk's Linog — reduces the debuffed character's outgoing damage.
     *
     * A DAMAGE_REDUCTION with EffectTarget.ENEMY placed by owner X applies
     * to X's opponent — so we look for effects NOT owned by the attacker.
     *
     * @param attacker The character currently attacking (may be under a debuff placed by opponent)
     */
    private double getEnemyDamageReduction(Character attacker) {
        double total = 0;
        for (int i = 0; i < activeEffects.size(); i++) {
            StatusEffect e = activeEffects.get(i);
            if (e.getType()   == Skill.EffectType.DAMAGE_REDUCTION
                    && e.getTarget() == StatusEffect.EffectTarget.ENEMY
                    && effectOwners.get(i) != attacker) {     // placed by the OTHER character
                total += e.getValue();
            }
        }
        return total;
    }

    /** Ticks all active effects and removes expired ones. */
    private void tickAndCleanEffects(TurnResult.Builder result) {
        Iterator<StatusEffect> effectIt = activeEffects.iterator();
        Iterator<Character>    ownerIt  = effectOwners.iterator();

        while (effectIt.hasNext()) {
            StatusEffect e = effectIt.next();
            ownerIt.next();
            e.tick();
            if (e.isExpired()) {
                result.log("[Status expired: " + e.getType() + "]");
                effectIt.remove();
                ownerIt.remove();
            }
        }
    }
}