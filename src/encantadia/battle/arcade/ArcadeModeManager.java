package encantadia.battle.arcade;

import encantadia.characters.*;
import encantadia.characters.Character;
import encantadia.battle.skill.Skill;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcadeModeManager {

    private static boolean arcadeCompleted = false;

    public static final int HP_BOOST_AT  = 3;
    public static final int ULTIMATE_AT  = 6;
    public static final int HP_BOOST_AMT = 2000;

    private final Character       player;
    private final List<Character> enemyQueue;
    private final List<Character> defeatedEnemies = new ArrayList<>();
    private int currentIndex = 0;

    // 📈 REAL-TIME METRICS TRACKING
    private final long startTimeMillis;
    private long       endTimeMillis = 0;
    private int        totalDamageDealt = 0;
    private int        totalDamageReceived = 0;

    public ArcadeModeManager(Character player) {
        this.player          = player;
        this.enemyQueue      = buildQueue(player);
        this.startTimeMillis = System.currentTimeMillis(); // Start the timer!
    }

    private List<Character> buildQueue(Character player) {
        Character[] full = {
                new Tyrone(), new MakelanShere(), new Mary(), new Dirk(),
                new Flamara(), new Dea(), new Adamus(), new Tera()
        };

        List<Character> q = new ArrayList<>();
        for (Character c : full) {
            if (!c.getName().equals(player.getName())) {
                q.add(c);
            }
        }

        Collections.shuffle(q);

        if (!q.isEmpty()) {
            Character boss = q.get(q.size() - 1);
            boss.increaseMaxHP(3000);
        }
        return q;
    }

    // ── Metric Updaters ────────────────────────────────────────
    public void addDamageDealt(int amount)    { totalDamageDealt += amount; }
    public void addDamageReceived(int amount) { totalDamageReceived += amount; }

    public void markEndTime() {
        if (endTimeMillis == 0) endTimeMillis = System.currentTimeMillis();
    }

    public int getTotalTimeSeconds() {
        long end = (endTimeMillis > 0) ? endTimeMillis : System.currentTimeMillis();
        return (int) ((end - startTimeMillis) / 1000);
    }

    public int getTotalDamageDealt()    { return totalDamageDealt; }
    public int getTotalDamageReceived() { return totalDamageReceived; }

    // ── Accessors ─────────────────────────────────────────────
    public Character       getCurrentEnemy()    { return currentIndex < enemyQueue.size() ? enemyQueue.get(currentIndex) : null; }
    public int             getCurrentIndex()    { return currentIndex; }
    public int             getTotalEnemies()    { return enemyQueue.size(); }
    public List<Character> getEnemyQueue()      { return new ArrayList<>(enemyQueue); }
    public List<Character> getDefeatedEnemies() { return new ArrayList<>(defeatedEnemies); }
    public boolean         isFinished()         { return currentIndex >= enemyQueue.size(); }
    public boolean         isFinalBoss()        { return !enemyQueue.isEmpty() && currentIndex == enemyQueue.size() - 1; }

    public void recordVictory() {
        if (currentIndex < enemyQueue.size()) {
            defeatedEnemies.add(enemyQueue.get(currentIndex));
            currentIndex++;
        }
    }

    public void nextEnemy() { recordVictory(); }

    public boolean shouldGiveHPBoost()  { return currentIndex == HP_BOOST_AT; }
    public boolean shouldGiveUltimate() { return currentIndex == ULTIMATE_AT; }

    public List<Skill> getUltimateChoices() {
        List<Skill> choices = new ArrayList<>();
        for (Character d : defeatedEnemies) choices.addAll(d.getSkills());
        return choices;
    }

    public static void setArcadeCompleted(boolean v) { arcadeCompleted = v; }
    public static boolean isArcadeCompleted()        { return arcadeCompleted; }
}