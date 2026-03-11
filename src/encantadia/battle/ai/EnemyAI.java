package encantadia.battle.ai;

import encantadia.battle.engine.CooldownManager;
import encantadia.characters.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyAI {

    public static int chooseSkill(Character enemy, CooldownManager cooldownManager) {

        List<Integer> availableSkills = new ArrayList<>();

        for (int i = 0; i < enemy.getSkills().size(); i++) {

            if (!cooldownManager.isOnCooldown(enemy, i)) {
                availableSkills.add(i);
            }

        }

        if (availableSkills.isEmpty()) {
            return 0; // fallback to basic attack
        }

        Random rand = new Random();
        if(availableSkills.isEmpty()){
            return 0;
        }

        return availableSkills.get(rand.nextInt(availableSkills.size()));
    }
}