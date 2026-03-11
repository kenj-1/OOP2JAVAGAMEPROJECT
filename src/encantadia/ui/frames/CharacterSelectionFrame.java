package encantadia.ui.frames;
import encantadia.battle.EnemyFactory;

import encantadia.characters.*;
import encantadia.characters.Character;
import encantadia.ui.frames.battleModeFrames.PVEBattleFrame;


import javax.swing.*;
import java.awt.*;        // ← add this

public class CharacterSelectionFrame extends JFrame {

    private JPanel characterSelectionFrame;
    private JButton selectDirk;
    private JButton selectMary;
    private JButton selectMakelanShere;
    private JButton selectTyrone;
    private JButton selectAdamus;
    private JButton selectTera;
    private JButton selectFlamara;
    private JButton selectDea;
    private JLabel labelDirk;
    private JLabel labelMary;
    private JLabel labelMakelanShere;
    private JLabel labelTyrone;
    private JLabel labelDea;
    private JLabel labelFlamara;
    private JLabel labelTera;
    private JLabel labelAdamus;
    private JLabel dirkCharacter;
    private JLabel maryCharacter;
    private JLabel makelanShereCharacter;
    private JLabel tyroneCharacter;
    private JLabel deaCharacter;
    private JLabel flamaraCharacter;
    private JLabel teraCharacter;
    private JLabel adamusCharacter;


    public CharacterSelectionFrame(){
        setContentPane(characterSelectionFrame);
        setTitle("Encantadia: Echoes of the Gem - Select your Fighter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);

        ImageIcon icon = new ImageIcon("src/assets/Portrait_Placeholder.png");
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);


        dirkCharacter.setText(
                createCharacterCard(
                        "Dirk",
                        "src/assets/dirk.png",
                        "5000 HP",
                        "Reduces enemy damage by 65% next turn"
                )
        );

        maryCharacter.setText(
                createCharacterCard(
                        "Mary",
                        "src/assets/mary.png",
                        "5000 HP",
                        "50% chance to steal a turn"
                )
        );

        makelanShereCharacter.setText(
                createCharacterCard(
                        "Makelan Shere",
                        "src/assets/makelan.png",
                        "5000 HP",
                        "Increase enemy cooldown by 1 turn"
                )
        );

        tyroneCharacter.setText(
                createCharacterCard(
                        "Tyrone",
                        "src/assets/tyrone.png",
                        "5000 HP",
                        "Heavy strike dealing bonus damage"
                )
        );

        deaCharacter.setText(
                createCharacterCard(
                        "Dea",
                        "src/assets/dea.png",
                        "5000 HP",
                        "60% chance to increase opponent cooldown"
                )
        );

        flamaraCharacter.setText(
                createCharacterCard(
                        "Flamara",
                        "src/assets/flamara.png",
                        "5000 HP",
                        "Throws additional damage of 300"
                )
        );

        teraCharacter.setText(
                createCharacterCard(
                        "Tera",
                        "src/assets/tera.png",
                        "5000 HP",
                        "Heals 300 HP"
                )
        );

        adamusCharacter.setText(
                createCharacterCard(
                        "Adamus",
                        "src/assets/adamus.png",
                        "5000 HP",
                        "Massive strike ignoring defense"
                )
        );


        selectDirk.setText("Select Dirk");
        selectMary.setText("Select Mary");
        selectDea.setText("Select Dea");
        selectFlamara.setText("Select Flamara");
        selectTera.setText("Select Tera");
        selectAdamus.setText("Select Adamus");
        selectTyrone.setText("Select Tyrone");
        selectMakelanShere.setText("Select Makelan Shere");


        labelDirk.setIcon(new ImageIcon(img));
        labelMary.setIcon(new ImageIcon(img));
        labelMakelanShere.setIcon(new ImageIcon(img));
        labelTyrone.setIcon(new ImageIcon(img));
        labelDea.setIcon(new ImageIcon(img));
        labelFlamara.setIcon(new ImageIcon(img));
        labelTera.setIcon(new ImageIcon(img));
        labelAdamus.setIcon(new ImageIcon(img));

        selectDirk.addActionListener(e -> showCharacterBackstory(new Dirk()));
        selectTyrone.addActionListener(e -> showCharacterBackstory(new Tyrone()));
        selectMary.addActionListener(e -> showCharacterBackstory(new Mary()));
        selectMakelanShere.addActionListener(e -> showCharacterBackstory(new MakelanShere()));
        selectAdamus.addActionListener(e -> showCharacterBackstory(new Adamus()));
        selectTera.addActionListener(e -> showCharacterBackstory(new Tera()));
        selectFlamara.addActionListener(e -> showCharacterBackstory(new Flamara()));
        selectDea.addActionListener(e -> showCharacterBackstory(new Dea()));

        setVisible(true);
    }


    private String createCharacterCard(String name, String img, String health, String ultimate){

        return "<html><div style='text-align:center;width:170px;'>"
                + "<h3>" + name + "</h3>"
                + "<img src='file:" + img + "' width='110' height='110'><br>"
                + "<b>Health:</b> " + health + "<br>"
                + "<b>Ultimate:</b> " + ultimate
                + "</div></html>";
    }


    private void showCharacterBackstory(Character character) {

        Character enemy = EnemyFactory.getRandomEnemy(character);

        new StorylineDialogBox(character, enemy, () -> {

            new PVEBattleFrame(character, enemy);

        });

        dispose();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }


}
