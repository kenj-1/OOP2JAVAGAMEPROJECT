package encantadia.battle.engine;

import encantadia.battle.skill.Skill;
import encantadia.battle.status.StatusEffect;
import encantadia.battle.result.TurnResult;
import encantadia.characters.Character;

import java.util.*;

public class TurnManager {

    private final Character player;
    private final Character enemy;
    private final CooldownManager cooldownManager;

    private final List<StatusEffect> activeEffects = new ArrayList<>();
    private final List<Character> effectOwners = new ArrayList<>();

    private boolean playerTurn;

    public TurnManager(Character player, Character enemy) {
        this.player = player;
        this.enemy = enemy;
        this.playerTurn = true;

        this.cooldownManager = new CooldownManager();
        cooldownManager.registerCharacter(player);
        cooldownManager.registerCharacter(enemy);
    }

    public TurnResult executeSkill(Character attacker, Character target, int skillIndex) {

        TurnResult.Builder result = new TurnResult.Builder()
                .attackerName(attacker.getName());

        List<Skill> skills = attacker.getSkills();

        if (skillIndex < 0 || skillIndex >= skills.size()) {
            return result.log("Invalid skill index").build();
        }

        Skill skill = skills.get(skillIndex);
        result.skillUsed(skill.getName());

        if (cooldownManager.isOnCooldown(attacker, skillIndex)) {
            return result.log("Skill on cooldown!").build();
        }

        int base = skill.rollValue();
        result.baseDamageOrHeal(base);

        if (skill.getSkillType() == Skill.SkillType.HEAL) {
            processHeal(attacker, skill, skillIndex, base, result);
        } else {
            processDamage(attacker, target, skill, skillIndex, base, result);
        }

        tickEffects();
        return result.build();
    }

    private void processDamage(Character attacker, Character target,
                               Skill skill, int index, int base,
                               TurnResult.Builder result) {

        double missChance = getMissChance(attacker);
        if (Math.random() < missChance) {
            result.log("💨 Attack missed!");
            cooldownManager.setCooldown(attacker, index, skill.getCooldown());
            return;
        }

        int damage = applyReductions(attacker, target, base);

        target.takeDamage(damage);
        result.totalDamageDealt(damage);
        result.log("⚔  " + attacker.getName() + " uses " + skill.getName()
                + " on " + target.getName() + "  →  " + damage + " dmg");  // ADD THIS

        if (skill.getEffectType() != Skill.EffectType.NONE && skill.effectTriggered()) {
            applyEffect(attacker, target, skill, damage, result);
        }

        cooldownManager.setCooldown(attacker, index, skill.getCooldown());

        if (!target.isAlive()) {
            result.targetDefeated(true);
        }
    }

    private int applyReductions(Character attacker, Character target, int damage) {

        double selfReduction = getReduction(attacker, StatusEffect.EffectTarget.SELF);
        double enemyReduction = getReduction(target, StatusEffect.EffectTarget.ENEMY);

        damage *= (1 - selfReduction);
        damage *= (1 - enemyReduction);

        return Math.max(0, (int) damage);
    }

    private void processHeal(Character healer, Skill skill, int index,
                             int amount, TurnResult.Builder result) {

        healer.heal(amount);
        result.totalHealApplied(amount);
        result.log("💚  " + healer.getName() + " uses " + skill.getName()
                + "  →  +" + amount + " HP restored");  // ADD THIS

        cooldownManager.setCooldown(healer, index, skill.getCooldown());
    }

    private void applyEffect(Character attacker, Character target,
                             Skill skill, int damage, TurnResult.Builder result) {

        switch (skill.getEffectType()) {

            case EXTRA_DAMAGE -> {
                int bonus = (skill.getEffectValue() < 1)
                        ? (int) (target.getMaxHP() * skill.getEffectValue())
                        : (int) skill.getEffectValue();

                target.takeDamage(bonus);
                result.extraDamage(bonus);
                result.log("🔥  Bonus hit!  " + bonus + " extra damage");
            }

            case TURN_STEAL -> {
                result.turnStolen(true);
                result.log("⚡ Extra turn gained!");
            }

            case COOLDOWN_INCREASE -> {
                cooldownManager.increaseCooldowns(target, (int) skill.getEffectValue());
                result.log("⏳ Enemy cooldown increased!");
            }

            case MISS_CHANCE -> {
                addEffect(new StatusEffect(
                        Skill.EffectType.MISS_CHANCE,
                        skill.getEffectValue(),
                        StatusEffect.EffectTarget.ENEMY,
                        1), attacker);
                result.log("🌫 Enemy accuracy reduced!");
            }

            case DAMAGE_REDUCTION -> {
                addEffect(new StatusEffect(
                        Skill.EffectType.DAMAGE_REDUCTION,
                        skill.getEffectValue(),
                        StatusEffect.EffectTarget.ENEMY,
                        1), attacker);
                result.log("🛡 Damage reduction applied!");
            }

            case RECOIL -> {
                int recoil = (int) (damage * skill.getEffectValue());
                attacker.takeDamage(recoil);
                result.recoilDamage(recoil);
                result.log("💢  " + attacker.getName() + " suffers " + recoil + " recoil damage");

                addEffect(new StatusEffect(
                        Skill.EffectType.DAMAGE_REDUCTION,
                        0.30,
                        StatusEffect.EffectTarget.SELF,
                        2), attacker);
            }

            case HEAL -> {
                int heal = (int) skill.getEffectValue();
                attacker.heal(heal);
                result.totalHealApplied(heal);
                result.log("💚  +" + heal + " HP from skill effect");
            }
        }
    }

    private void addEffect(StatusEffect effect, Character owner) {
        activeEffects.add(effect);
        effectOwners.add(owner);
    }

    private double getReduction(Character character, StatusEffect.EffectTarget targetType) {
        double total = 0;

        for (int i = 0; i < activeEffects.size(); i++) {
            StatusEffect e = activeEffects.get(i);

            if (e.getType() == Skill.EffectType.DAMAGE_REDUCTION &&
                    e.getTarget() == targetType &&
                    (targetType == StatusEffect.EffectTarget.SELF
                            ? effectOwners.get(i) == character
                            : effectOwners.get(i) != character)) {

                total += e.getValue();
            }
        }

        return Math.min(total, 0.9);
    }

    private double getMissChance(Character attacker) {
        double total = 0;

        for (int i = 0; i < activeEffects.size(); i++) {
            StatusEffect e = activeEffects.get(i);

            if (e.getType() == Skill.EffectType.MISS_CHANCE &&
                    effectOwners.get(i) != attacker) {
                total += e.getValue();
            }
        }

        return Math.min(total, 0.9);
    }

    private void tickEffects() {

        Iterator<StatusEffect> eIt = activeEffects.iterator();
        Iterator<Character> oIt = effectOwners.iterator();

        while (eIt.hasNext()) {
            StatusEffect e = eIt.next();
            oIt.next();

            e.tick();

            if (e.isExpired()) {
                eIt.remove();
                oIt.remove();
            }
        }
    }

    public void advanceTurn() {
        playerTurn = !playerTurn;
        cooldownManager.decrementCooldowns(playerTurn ? player : enemy);
    }

    public boolean isPlayerTurn() { return playerTurn; }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}