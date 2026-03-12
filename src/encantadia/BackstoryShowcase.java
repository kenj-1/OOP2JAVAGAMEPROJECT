package encantadia;

import encantadia.story.*;
import encantadia.ui.frames.CharacterSelectionFrame;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import java.awt.*;

public class BackstoryShowcase extends JFrame {

    private JTextPane storyPane;
    private JButton continueButton;

    private StoryType storyType;

    public BackstoryShowcase(StoryType storyType){

        this.storyType = storyType;

        setTitle("Encantadia Lore");
        setSize(1024,768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());

        storyPane = new JTextPane();
        storyPane.setContentType("text/html");
        storyPane.setEditable(false);
        storyPane.setFont(new Font("Serif",Font.PLAIN,18));

        JScrollPane scroll = new JScrollPane(storyPane);

        continueButton = new JButton("Continue");
        continueButton.setEnabled(false);

        add(scroll, BorderLayout.CENTER);
        add(continueButton, BorderLayout.SOUTH);

        continueButton.addActionListener(e -> proceed());

        setVisible(true);

        playStory();
    }

    private void proceed(){

        dispose();

        switch (storyType){

            case GAME_LORE:
                new MainMenuFrame();
                break;

            case PVE_LORE:
            case PVP_LORE:
            case ARCADE_LORE:
                new CharacterSelectionFrame();
                break;
        }
    }

    private void playStory(){

        String story = getStory();

        new Thread(() -> {

            try{

                StringBuilder builder = new StringBuilder();

                for(char c : story.toCharArray()){

                    builder.append(c);

                    String current = builder.toString();

                    SwingUtilities.invokeLater(() ->
                            storyPane.setText(current)
                    );

                    Thread.sleep(8);
                }

                SwingUtilities.invokeLater(() ->
                        continueButton.setEnabled(true)
                );

            }catch(Exception ignored){}

        }).start();
    }

    private String getStory(){

        switch (storyType){

            case GAME_LORE:
                return GameStories.GAME_LORE;

            case PVE_LORE:
                return GameStories.PVE_LORE;

            case PVP_LORE:
                return GameStories.PVP_LORE;

            case ARCADE_LORE:
                return GameStories.ARCADE_LORE;
        }

        return "";
    }
}