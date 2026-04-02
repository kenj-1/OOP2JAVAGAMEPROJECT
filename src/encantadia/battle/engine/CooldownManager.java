package encantadia.battle.engine;

import encantadia.characters.Character;

import java.util.HashMap;
import java.util.Map;

public class CooldownManager {

    private final Map<Character, int[]> cooldownMap = new HashMap<>();

    public void registerCharacter(Character character) {
        cooldownMap.put(character, new int[character.getSkills().size()]);
    }

    public boolean isOnCooldown(Character c, int i) {
        return get(c)[i] > 0;
    }

    public int getRemainingCooldown(Character c, int i) {
        return get(c)[i];
    }

    public void setCooldown(Character c, int i, int turns) {
        get(c)[i] = Math.max(0, turns);
    }

    public void decrementCooldowns(Character c) {
        int[] cd = get(c);
        for (int i = 0; i < cd.length; i++) {
            if (cd[i] > 0) cd[i]--;
        }
    }

    public void increaseCooldowns(Character c, int amount) {
        int[] cd = get(c);
        for (int i = 0; i < cd.length; i++) {
            if (cd[i] > 0) cd[i] += amount;
        }
    }

    private int[] get(Character c) {
        if (!cooldownMap.containsKey(c)) {
            throw new IllegalStateException("Character not registered");
        }
        return cooldownMap.get(c);
    }
}