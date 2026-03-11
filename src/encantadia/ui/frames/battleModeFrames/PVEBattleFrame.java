package encantadia.ui.frames.battleModeFrames;

import encantadia.battle.ai.EnemyAI;
import encantadia.battle.engine.CooldownManager;
import encantadia.battle.engine.TurnManager;
import encantadia.battle.result.TurnResult;
import encantadia.characters.Character;

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
    private int enemyRoundsWon = 0;
    private int currentRound = 1;


    public PVEBattleFrame(Character player, Character enemy){

        this.playerCharacter = player;
        this.enemyCharacter = enemy;

        this.turnManager = new TurnManager(playerCharacter, enemyCharacter);

        setContentPane(pveBattleFramee);
        setTitle("Encantadia: Echoes of the Gem - Battle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        initializeBattleUI();

        setVisible(true);
    }

    private void initializeBattleUI(){

        playerName.setText(playerCharacter.getName());
        enemyName.setText(enemyCharacter.getName());

        roundTitle.setText("Round " + currentRound);
        bestOf3RoundsCounter.setText("First to 2 wins");

        skill1Button.setText(playerCharacter.getSkills().get(0).getName());
        skill2Button.setText(playerCharacter.getSkills().get(1).getName());
        skill3Button.setText(playerCharacter.getSkills().get(2).getName());

        skill1Button.addActionListener(e -> useSkill(0));
        skill2Button.addActionListener(e -> useSkill(1));
        skill3Button.addActionListener(e -> useSkill(2));

        updateHealthBars();
        updateCooldownCounters();
    }

    private void useSkill(int skillIndex){

        if(!turnManager.isPlayerTurn()){
            return;
        }

        CooldownManager cd = turnManager.getCooldownManager();

        if(cd.isOnCooldown(playerCharacter, skillIndex)){
            invokingOfSkillsPlayer.setText("<html>Skill is still on cooldown!</html>");
            return;
        }

        TurnResult result = turnManager.executeSkill(
                playerCharacter,
                enemyCharacter,
                skillIndex
        );

        displayBattleLog(result, false);

        updateHealthBars();
        updateCooldownCounters();

        if(result.isTargetDefeated()){
            endRound(playerCharacter);
            return;
        }

        if(!result.isTurnStolen()){
            turnManager.advanceTurn();
            enemyTurn();
        }
    }

    private void enemyTurn(){

        if(turnManager.isPlayerTurn()){
            return;
        }

        boolean extraTurn;

        do{

            int skillIndex = EnemyAI.chooseSkill(
                    enemyCharacter,
                    turnManager.getCooldownManager()
            );

            TurnResult result = turnManager.executeSkill(
                    enemyCharacter,
                    playerCharacter,
                    skillIndex
            );

            displayBattleLog(result, true);

            updateHealthBars();
            updateCooldownCounters();

            if(result.isTargetDefeated()){
                endRound(enemyCharacter);
                return;
            }

            extraTurn = result.isTurnStolen();

        } while(extraTurn && playerCharacter.isAlive());

        turnManager.advanceTurn();
    }

    private void endRound(Character winner){

        if(winner == playerCharacter){
            playerRoundsWon++;
            updateScoreBoard();
        } else {
            enemyRoundsWon++;
            updateScoreBoard();
        }

        // If someone reached 2 wins -> battle ends
        if(playerRoundsWon == 2 || enemyRoundsWon == 2){

            String finalWinner =
                    playerRoundsWon > enemyRoundsWon ?
                            playerCharacter.getName() :
                            enemyCharacter.getName();

            JOptionPane.showMessageDialog(this,
                    finalWinner + " wins the battle!\n\nFinal Score\n"
                            + playerCharacter.getName() + " : " + playerRoundsWon + "\n"
                            + enemyCharacter.getName() + " : " + enemyRoundsWon
            );

            dispose();
            return;
        }

        // Round winner announcement
        JOptionPane.showMessageDialog(this,
                winner.getName() + " wins Round " + currentRound + "!\n\n" +
                        "Score\n" +
                        playerCharacter.getName() + " : " + playerRoundsWon + "\n" +
                        enemyCharacter.getName() + " : " + enemyRoundsWon
        );

        currentRound++;

        // Ready confirmation for next round
        JOptionPane.showMessageDialog(this,
                "Prepare for Round " + currentRound + "!\n\n" +
                        "Current Score\n" +
                        playerCharacter.getName() + " : " + playerRoundsWon + "\n" +
                        enemyCharacter.getName() + " : " + enemyRoundsWon
        );

        resetRound();
    }

    private void resetRound(){

        playerCharacter.heal(playerCharacter.getMaxHP());
        enemyCharacter.heal(enemyCharacter.getMaxHP());

        turnManager = new TurnManager(playerCharacter, enemyCharacter);

        roundTitle.setText("Round " + currentRound);

        updateHealthBars();
        updateCooldownCounters();
    }

    private void updateHealthBars(){

        playerHP.setText(
                playerCharacter.getCurrentHP() +
                        " / " +
                        playerCharacter.getMaxHP()
        );

        enemyHP.setText(
                enemyCharacter.getCurrentHP() +
                        " / " +
                        enemyCharacter.getMaxHP()
        );
    }

    private void updateCooldownCounters(){

        CooldownManager cd = turnManager.getCooldownManager();

        int cd1 = cd.getRemainingCooldown(playerCharacter,0);
        int cd2 = cd.getRemainingCooldown(playerCharacter,1);
        int cd3 = cd.getRemainingCooldown(playerCharacter,2);

        skill1CooldownCounter.setText(formatCooldown(cd1));
        skill2CooldownCounter.setText(formatCooldown(cd2));
        ultimateCooldownCounter.setText(formatCooldown(cd3));

        // Disable buttons if skill is on cooldown
        skill1Button.setEnabled(cd1 == 0);
        skill2Button.setEnabled(cd2 == 0);
        skill3Button.setEnabled(cd3 == 0);
    }

    private void displayBattleLog(TurnResult result, boolean playerAction){

        StringBuilder log = new StringBuilder();

        for(String line : result.getLogMessages()){
            log.append(line).append("<br>");
        }

        if(playerAction){
            invokingOfSkillsPlayer.setText("<html>" + log + "</html>");
        } else {
            invokingOfSkillsEnemy.setText("<html>" + log + "</html>");
        }
    }

    private String formatCooldown(int cd){
        return cd == 0 ? "READY" : String.valueOf(cd);
    }

    private void updateScoreBoard(){
        playerScore.setText(String.valueOf(playerRoundsWon));
        enemyScore.setText(String.valueOf(enemyRoundsWon));
    }
}