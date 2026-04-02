package encantadia.battle.ai;

import encantadia.battle.engine.CooldownManager;
import encantadia.characters.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyAI {

    private static final Random rand = new Random();

    public static int chooseSkill(Character enemy, CooldownManager cm) {

        List<Integer> available = new ArrayList<>();

        for (int i = 0; i < enemy.getSkills().size(); i++) {
            if (!cm.isOnCooldown(enemy, i)) {
                available.add(i);
            }
        }

        if (available.isEmpty()) return 0;

        return available.get(rand.nextInt(available.size()));
    }
}