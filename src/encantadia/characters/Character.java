package encantadia.characters;

import encantadia.battle.skill.Skill;

import java.util.ArrayList;
import java.util.List;

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

    public void increaseMaxHP(int amount) {
        maxHP += amount;
        currentHP += amount;
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void takeDamage(int damage) {
        damage = Math.max(0, damage);
        currentHP -= damage;
        if (currentHP < 0) currentHP = 0;
    }

    public void heal(int amount) {
        amount = Math.max(0, amount);
        currentHP += amount;
        if (currentHP > maxHP) currentHP = maxHP;
    }

    public boolean isAlive() {
        return currentHP > 0;
    }

    @Override
    public String toString() {
        return name + " (" + currentHP + "/" + maxHP + " HP)";
    }

    public void reset() {
        currentHP = maxHP;
    }
    // ── Getters ──────────────────────────────────────────────

    public List<Skill> getSkills() {
        return new ArrayList<>(skills);
    }
    public String getName()         { return name; }
    public String getTitle()        { return title; }
    public String getBackstory()    { return backstory; }
    public int getCurrentHP()       { return currentHP; }
    public int getMaxHP()           { return maxHP; }   // ← required by TurnManager

}