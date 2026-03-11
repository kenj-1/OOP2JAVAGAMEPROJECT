package encantadia.battle;

import encantadia.characters.*;
import encantadia.characters.Character;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyFactory {

    public static Character getRandomEnemy(Character player){

        List<Character> roster = new ArrayList<>();

        roster.add(new Adamus());
        roster.add(new Dea());
        roster.add(new Dirk());
        roster.add(new Flamara());
        roster.add(new MakelanShere());
        roster.add(new Mary());
        roster.add(new Tera());
        roster.add(new Tyrone());

        roster.removeIf(c -> c.getName().equals(player.getName()));

        Random rand = new Random();
        return roster.get(rand.nextInt(roster.size()));
    }
}