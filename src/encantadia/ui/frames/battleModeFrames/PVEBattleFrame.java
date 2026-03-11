package encantadia.ui.frames.battleModeFrames;

import encantadia.battle.ai.EnemyAI;
import encantadia.battle.engine.CooldownManager;
import encantadia.battle.engine.TurnManager;
import encantadia.battle.result.TurnResult;
import encantadia.characters.Character;
import encantadia.ui.frames.ResultDialogFrame;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;

public class PVEBattleFrame extends JFrame {

    private TurnManager turnManager;

    private JButton skill1Button;
    private JButton skill2Button;
    private JButton skill3Button;

    private JLabel playerName;
    private JLabel enemyName;

    private JLabel playerHP;
    private JLabel enemyHP;

    private JLabel playerIcon;
    private JLabel enemyIcon;

    private JLabel bestOf3RoundsCounter;
    private JLabel roundTitle;

    private JLabel skill1CooldownCounter;
    private JLabel skill2CooldownCounter;
    private JLabel ultimateCooldownCounter;

    private JPanel pveBattleFramee;
    private JLabel invokingOfSkillsPlayer;
    private JLabel skill1DamageRange;
    private JLabel skill2DamageRange;
    private JLabel ultimateDamageRange;
    private JLabel playerScore;
    private JLabel enemyScore;
    private JLabel invokingOfSkillsEnemy;

    private Character playerCharacter;
    private Character enemyCharacter;

    private int playerRoundsWon = 0;
    private int enemyRoundsWon  = 0;
    private int currentRound    = 1;

    public PVEBattleFrame(Character player, Character enemy) {

        this.playerCharacter = player;
        this.enemyCharacter  = enemy;
        this.turnManager     = new TurnManager(playerCharacter, enemyCharacter);

        setContentPane(pveBattleFramee);
        setTitle("Encantadia: Echoes of the Gem - Battle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        initializeBattleUI();
        setVisible(true);
    }

    // ── UI init ───────────────────────────────────────────────

    private void initializeBattleUI() {

        playerName.setText("<html><div style='text-align:center;'><h2>"
                + playerCharacter.getName() + "</h2></div></html>");

        enemyName.setText("<html><div style='text-align:center;'><h2>"
                + enemyCharacter.getName() + "</h2></div></html>");

        roundTitle.setText("<html><h2>Round " + currentRound + "</h2></html>");
        bestOf3RoundsCounter.setText("<html><b>First to 2 Wins</b></html>");

        skill1Button.setText(playerCharacter.getSkills().get(0).getName());
        skill2Button.setText(playerCharacter.getSkills().get(1).getName());
        skill3Button.setText(playerCharacter.getSkills().get(2).getName());

        skill1Button.addActionListener(e -> useSkill(0));
        skill2Button.addActionListener(e -> useSkill(1));
        skill3Button.addActionListener(e -> useSkill(2));

        updateHealthBars();
        updateCooldownCounters();
        updateScoreBoard();
    }

    // ── Player turn ───────────────────────────────────────────

    private void useSkill(int skillIndex) {

        if (!turnManager.isPlayerTurn()) return;

        CooldownManager cd = turnManager.getCooldownManager();
        if (cd.isOnCooldown(playerCharacter, skillIndex)) {
            invokingOfSkillsPlayer.setText("<html>Skill is still on cooldown!</html>");
            return;
        }

        TurnResult result = turnManager.executeSkill(playerCharacter, enemyCharacter, skillIndex);

        displayBattleLog(result, false);
        updateHealthBars();
        updateCooldownCounters();

        if (result.isTargetDefeated()) {
            endRound(playerCharacter);
            return;
        }

        if (!result.isTurnStolen()) {
            turnManager.advanceTurn();
            enemyTurn();
        }
    }

    // ── Enemy turn ────────────────────────────────────────────

    private void enemyTurn() {

        if (turnManager.isPlayerTurn()) return;

        boolean extraTurn;

        do {
            int skillIndex = EnemyAI.chooseSkill(enemyCharacter, turnManager.getCooldownManager());

            TurnResult result = turnManager.executeSkill(enemyCharacter, playerCharacter, skillIndex);

            displayBattleLog(result, true);
            updateHealthBars();
            updateCooldownCounters();

            if (result.isTargetDefeated()) {
                endRound(enemyCharacter);
                return;
            }

            extraTurn = result.isTurnStolen();

        } while (extraTurn && playerCharacter.isAlive());

        turnManager.advanceTurn();
    }

    // ── Round / match end ─────────────────────────────────────

    private void endRound(Character winner) {

        Character loser = (winner == playerCharacter) ? enemyCharacter : playerCharacter;

        if (winner == playerCharacter) {
            playerRoundsWon++;
        } else {
            enemyRoundsWon++;
        }

        updateScoreBoard();

        // ── Match over ────────────────────────────────────────
        if (playerRoundsWon == 2 || enemyRoundsWon == 2) {

            int wScore = (winner == playerCharacter) ? playerRoundsWon : enemyRoundsWon;
            int lScore = (winner == playerCharacter) ? enemyRoundsWon  : playerRoundsWon;

            ResultDialogFrame.showMatchResult(
                    this,
                    winner,
                    loser,
                    wScore,
                    lScore,
                    () -> {
                        new MainMenuFrame();
                        dispose();

                    }
            );

            return;
        }

        // ── Round over — show result, then go to next round ───
        int nextRound = currentRound + 1;

        int wScore = (winner == playerCharacter) ? playerRoundsWon : enemyRoundsWon;
        int lScore = (winner == playerCharacter) ? enemyRoundsWon  : playerRoundsWon;

        ResultDialogFrame.showRoundResult(
                this,
                winner,
                loser,
                wScore,
                lScore,
                nextRound,
                () -> {
                    currentRound = nextRound;
                    resetRound();
                }
        );
    }

    private void resetRound() {

        // Full heal both characters
        playerCharacter.heal(playerCharacter.getMaxHP());
        enemyCharacter.heal(enemyCharacter.getMaxHP());

        // Fresh turn manager (resets all cooldowns and status effects)
        turnManager = new TurnManager(playerCharacter, enemyCharacter);

        roundTitle.setText("<html><h2>Round " + currentRound + "</h2></html>");

        updateHealthBars();
        updateCooldownCounters();
        invokingOfSkillsPlayer.setText("");
        invokingOfSkillsEnemy.setText("");
    }

    // ── UI update helpers ─────────────────────────────────────

    private void updateHealthBars() {

        playerHP.setText("<html><b>HP:</b> "
                + playerCharacter.getCurrentHP() + " / "
                + playerCharacter.getMaxHP() + "</html>");

        enemyHP.setText("<html><b>HP:</b> "
                + enemyCharacter.getCurrentHP() + " / "
                + enemyCharacter.getMaxHP() + "</html>");
    }

    private void updateCooldownCounters() {

        CooldownManager cd = turnManager.getCooldownManager();

        int cd1 = cd.getRemainingCooldown(playerCharacter, 0);
        int cd2 = cd.getRemainingCooldown(playerCharacter, 1);
        int cd3 = cd.getRemainingCooldown(playerCharacter, 2);

        skill1CooldownCounter.setText(formatCooldown(cd1));
        skill2CooldownCounter.setText(formatCooldown(cd2));
        ultimateCooldownCounter.setText(formatCooldown(cd3));

        skill1Button.setEnabled(cd1 == 0);
        skill2Button.setEnabled(cd2 == 0);
        skill3Button.setEnabled(cd3 == 0);
    }

    private void displayBattleLog(TurnResult result, boolean isEnemy) {

        StringBuilder log = new StringBuilder();
        for (String line : result.getLogMessages()) {
            log.append(line).append("<br>");
        }

        if (isEnemy) {
            invokingOfSkillsEnemy.setText("<html>" + log + "</html>");
        } else {
            invokingOfSkillsPlayer.setText("<html>" + log + "</html>");
        }
    }

    private String formatCooldown(int cd) {
        if (cd == 0) return "<html><font color='green'><b>READY</b></font></html>";
        return "<html><font color='red'>" + cd + "</font></html>";
    }

    private void updateScoreBoard() {
        playerScore.setText(String.valueOf(playerRoundsWon));
        enemyScore.setText(String.valueOf(enemyRoundsWon));
    }
}