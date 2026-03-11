package encantadia.ui.frames;

import encantadia.characters.Character;

import javax.swing.*;
import java.awt.*;

public class StorylineDialogBox extends JFrame {

    private JLabel playerPortrait;
    private JLabel enemyPortrait;

    private JTextArea storyBox;

    private JButton skipButton;
    private JButton battleButton;

    private Character player;
    private Character enemy;

    private Runnable onFinish;

    private boolean enemyStoryStarted = false;

    private volatile boolean skipAnimation = false;

    public StorylineDialogBox(Character player, Character enemy, Runnable onFinish){

        this.player = player;
        this.enemy = enemy;
        this.onFinish = onFinish;

        setTitle("Storyline");
        setSize(1024,768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel background = new JLabel();
        background.setBounds(0,0,1024,768);
        background.setIcon(new ImageIcon("src/assets/story_background.png"));

        playerPortrait = new JLabel(loadCharacterImage(player));
        playerPortrait.setBounds(120,220,250,250);

        enemyPortrait = new JLabel(loadCharacterImage(enemy));
        enemyPortrait.setBounds(650,220,250,250);

        storyBox = new JTextArea();
        storyBox.setBounds(200,520,620,140);
        storyBox.setFont(new Font("Serif",Font.BOLD,18));
        storyBox.setLineWrap(true);
        storyBox.setWrapStyleWord(true);
        storyBox.setEditable(false);

        skipButton = new JButton("Skip");
        skipButton.setBounds(320,680,120,35);

        battleButton = new JButton("Reveal Enemy");
        battleButton.setBounds(480,680,160,35);
        battleButton.setEnabled(false);
        skipButton.addActionListener(e -> skipAnimation = true);

        battleButton.addActionListener(e -> {

            if(!enemyStoryStarted){

                enemyStoryStarted = true;

                battleButton.setEnabled(false);
                storyBox.setText("");

                startEnemyStory();

            }else{

                dispose();

                if(onFinish != null){
                    onFinish.run();
                }
            }
        });

        add(playerPortrait);
        add(enemyPortrait);
        add(storyBox);
        add(skipButton);
        add(battleButton);
        add(background);

        setVisible(true);

        startStory();
    }

    private ImageIcon loadCharacterImage(Character c){

        ImageIcon icon = new ImageIcon(
                "src/assets/" + c.getName().toLowerCase() + ".png"
        );

        Image img = icon.getImage().getScaledInstance(220,220,Image.SCALE_SMOOTH);

        return new ImageIcon(img);
    }

    private void typeText(String text) throws InterruptedException {

        if(skipAnimation){
            SwingUtilities.invokeLater(() ->
                    storyBox.append(text)
            );
            return;
        }

        StringBuilder builder = new StringBuilder(storyBox.getText());

        for(char c : text.toCharArray()){

            if(skipAnimation){
                String finalText = builder.toString() + text.substring(builder.length());
                SwingUtilities.invokeLater(() ->
                        storyBox.setText(finalText)
                );
                return;
            }

            builder.append(c);

            String current = builder.toString();

            SwingUtilities.invokeLater(() ->
                    storyBox.setText(current)
            );

            Thread.sleep(25);
        }
    }

    private void startStory(){

        storyBox.setText("");

        new Thread(() -> {

            try{

                String playerStory =
                        player.getName() + " - " + player.getTitle() + "\n\n" +
                                player.getBackstory();

                typeText(playerStory);

                SwingUtilities.invokeLater(() ->
                        battleButton.setEnabled(true)
                );

            }catch(Exception ignored){}

        }).start();
    }

    private void startEnemyStory(){

        new Thread(() -> {

            try{

                String enemyStory =
                        "Enemy Appears!\n\n" +
                                enemy.getName() + " - " + enemy.getTitle() + "\n\n" +
                                enemy.getBackstory();

                typeText(enemyStory);

                SwingUtilities.invokeLater(() -> {

                    battleButton.setText("Begin Battle");
                    battleButton.setEnabled(true);

                });

            }catch(Exception ignored){}

        }).start();
    }
}