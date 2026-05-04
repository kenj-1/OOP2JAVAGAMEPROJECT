package encantadia.ui.frames;

import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.ScreenManager;
import encantadia.gamemode.ArcadeMode;
import encantadia.gamemode.GameModeType;
import encantadia.gamemode.PVEMode;
import encantadia.gamemode.PVPMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import encantadia.audio.MusicManager;
import encantadia.audio.MusicType;

public class MainMenuFrame extends JFrame {

    private JButton arcadeButton, PVPButton, PVEButton, exitGameButton, leaderboardButton;

    private static final String BG_PATH      = "/resources/background.png";
    private static final String COLUMNS_PATH = "/resources/columns.png";
    private static final String TITLE_PATH   = "/resources/mainMenu (1).png";
    private static final String HOLDER_PATH  = "/resources/mainMenuHolder.png";
    private static final String BTN_ARCADE   = "/resources/ArcadeButton (1).png";
    private static final String BTN_PVE      = "/resources/PVEButton (1).png";
    private static final String BTN_PVP      = "/resources/PVPButton (1).png";
    private static final String BTN_EXIT     = "/resources/exitButton (3).png";
    private static final String BTN_LEADER   = "/resources/leaderboardBUTTON.png";

    // 🏆 TROPHY ASSETS
    private static final String TROPHY_EARNED  = "/resources/Trophy.png";
    private static final String TROPHY_FAILED  = "/resources/TrophyFailed.png";
    private static final String TROPHY_OVERLAY = "/resources/TrophyWon.png";

    private ImagePanel       holderPanel;
    private JPanel           buttonsInsideHolder;
    private JPanel           exitRow;
    private ScaledImagePanel titlePanel;
    private TrophyPanel      trophyPanel;
    private JPanel           victoryOverlay; // The pop-up modal

    public MainMenuFrame() {
        setTitle("Encantadia: Echoes of the Gem — Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setResizable(true);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BackgroundPanel bg = new BackgroundPanel(BG_PATH);
        lp.add(bg, Integer.valueOf(0)); // DEFAULT_LAYER

        ScaledImagePanel columns = new ScaledImagePanel(COLUMNS_PATH);
        lp.add(columns, Integer.valueOf(100)); // PALETTE_LAYER

        titlePanel = new ScaledImagePanel(TITLE_PATH);
        lp.add(titlePanel, Integer.valueOf(200)); // MODAL_LAYER

        holderPanel = new ImagePanel(HOLDER_PATH);
        holderPanel.setLayout(new GridBagLayout());
        lp.add(holderPanel, Integer.valueOf(300)); // POPUP_LAYER

        arcadeButton = createImageButton(BTN_ARCADE);
        PVEButton    = createImageButton(BTN_PVE);
        PVPButton    = createImageButton(BTN_PVP);

        buttonsInsideHolder = new JPanel();
        buttonsInsideHolder.setOpaque(false);
        buttonsInsideHolder.setLayout(new BoxLayout(buttonsInsideHolder, BoxLayout.Y_AXIS));

        buttonsInsideHolder.add(Box.createVerticalGlue());
        buttonsInsideHolder.add(PVPButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(14));
        buttonsInsideHolder.add(PVEButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(14));
        buttonsInsideHolder.add(arcadeButton);
        buttonsInsideHolder.add(Box.createVerticalGlue());
        holderPanel.add(buttonsInsideHolder);

        leaderboardButton = createImageButton(BTN_LEADER);

        exitGameButton = createImageButton(BTN_EXIT);
        exitGameButton.setText("Exit");

        exitRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        exitRow.setOpaque(false);
        exitRow.add(leaderboardButton);
        exitRow.add(exitGameButton);
        lp.add(exitRow, Integer.valueOf(400)); // DRAG_LAYER

        trophyPanel = new TrophyPanel();
        lp.add(trophyPanel, Integer.valueOf(400));

        // 🌟 VICTORY OVERLAY MODAL (Hidden by default)
        victoryOverlay = new JPanel(null) {
            private final Image wonImg = loadImage(TROPHY_OVERLAY);
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Dark translucent backdrop
                g2.setColor(new Color(0, 0, 0, 210));
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (wonImg != null) {
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    int iw = wonImg.getWidth(null), ih = wonImg.getHeight(null);
                    double scale = Math.min((double)getWidth()*0.6 / iw, (double)getHeight()*0.6 / ih);
                    int dw = (int)(iw * scale), dh = (int)(ih * scale);
                    g2.drawImage(wonImg, (getWidth() - dw) / 2, (getHeight() - dh) / 2, dw, dh, null);
                }
                g2.dispose();
            }
        };
        victoryOverlay.setOpaque(false);
        victoryOverlay.setVisible(false);

        JButton closeOverlayBtn = createImageButton("/resources/exitButton (1).png");
        closeOverlayBtn.setText("Close");
        closeOverlayBtn.addActionListener(e -> victoryOverlay.setVisible(false));
        victoryOverlay.add(closeOverlayBtn);

        lp.add(victoryOverlay, Integer.valueOf(500)); // Highest Layer!

        PVPButton.addActionListener(   e -> launchMode(GameModeType.PVP));
        PVEButton.addActionListener(   e -> launchMode(GameModeType.PVE));
        arcadeButton.addActionListener(e -> launchMode(GameModeType.ARCADE));
        leaderboardButton.addActionListener(e -> { dispose(); new LeaderboardFrame(); });
        exitGameButton.addActionListener(e -> { dispose(); new WelcomeScreenPage(); });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reposition(lp, bg, columns, closeOverlayBtn);
            }
        });

        setVisible(true);

        ScreenManager.register(this);
        SwingUtilities.invokeLater(() -> reposition(lp, bg, columns, closeOverlayBtn));

        // ✅ START MENU MUSIC
        MusicManager.play(MusicType.MENU);
    }

    private void reposition(JLayeredPane pane, JPanel bg, JPanel columns, JButton closeOverlayBtn) {
        int w = pane.getWidth(), h = pane.getHeight();
        if (w == 0 || h == 0) return;

        bg.setBounds(0, 0, w, h);
        columns.setBounds(0, 0, w, h);
        victoryOverlay.setBounds(0, 0, w, h); // Overlay covers the entire screen

        int holderW = Math.min(600, (int)(w * 0.55));
        int holderH = Math.min(680, (int)(h * 0.85));
        int holderX = (w - holderW) / 2;
        int holderY = (int)(h * 0.15);
        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        int titleW = Math.min(680, (int)(w * 0.65));
        int titleH = (int)(titleW * 0.30);
        int titleX = (w - titleW) / 2;
        int titleY = Math.max(0, holderY - (int)(titleH * 0.60));
        titlePanel.setBounds(titleX, titleY, titleW, titleH);

        int btnW = (int)(holderW * 0.72);
        int btnH = (int)(btnW * 0.32);
        int gap  = Math.max(12, (int)(holderH * 0.03));

        Dimension mainSize = new Dimension(btnW, btnH);
        for (JButton b : new JButton[]{PVPButton, PVEButton, arcadeButton}) {
            b.setPreferredSize(mainSize);
            b.setMinimumSize(mainSize);
            b.setMaximumSize(mainSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        int sideBtnSize = (int)(Math.min(w, h) * 0.125);
        Dimension sideSize = new Dimension(sideBtnSize, sideBtnSize);

        exitGameButton.setPreferredSize(sideSize);
        leaderboardButton.setPreferredSize(sideSize);
        exitGameButton.setMinimumSize(sideSize);
        leaderboardButton.setMinimumSize(sideSize);
        exitGameButton.setMaximumSize(sideSize);
        leaderboardButton.setMaximumSize(sideSize);

        exitGameButton.setContentAreaFilled(false);
        leaderboardButton.setContentAreaFilled(false);

        int centerY = holderY + (holderH / 2);
        int sidePadding = (int)(w * 0.03);

        int lbX = sidePadding;
        int exitX = w - sideBtnSize - sidePadding;
        int sideY = centerY - (sideBtnSize / 2);

        leaderboardButton.setBounds(lbX, sideY, sideBtnSize, sideBtnSize);
        exitGameButton.setBounds(exitX, sideY, sideBtnSize, sideBtnSize);

        buttonsInsideHolder.setBounds(0, 0, holderW, holderH);
        buttonsInsideHolder.removeAll();
        buttonsInsideHolder.add(Box.createVerticalGlue());
        buttonsInsideHolder.add(Box.createVerticalStrut((int)(holderH * 0.15)));
        buttonsInsideHolder.add(PVPButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(gap));
        buttonsInsideHolder.add(PVEButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(gap));
        buttonsInsideHolder.add(arcadeButton);
        buttonsInsideHolder.add(Box.createVerticalGlue());
        buttonsInsideHolder.add(exitRow);
        buttonsInsideHolder.add(Box.createVerticalStrut((int)(holderH * 0.08)));

        // --- TROPHY POSITION ---
        int rightPillarLeft = (int)(w * 0.85);
        int gapCentreX = holderX + holderW + (rightPillarLeft - holderX - holderW) / 2;
        int trophySize = Math.min(180, Math.max(100, (int)(w * 0.15))); // Slightly larger
        int trophyX = gapCentreX - trophySize / 2;
        int trophyY = holderY + (holderH - trophySize) / 2;

        trophyPanel.setBounds(trophyX, trophyY, trophySize, trophySize);

        // --- OVERLAY CLOSE BUTTON POSITION ---
        int cw = (int)(w * 0.18);
        int ch = (int)(cw * 0.4);
        closeOverlayBtn.setBounds((w - cw)/2, (int)(h * 0.82), cw, ch);

        holderPanel.revalidate();
        holderPanel.repaint();
        pane.revalidate();
        pane.repaint();
    }

    private void launchMode(GameModeType mode) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            switch (mode) {
                case PVE -> new PVEMode();
                case PVP -> new PVPMode();
                case ARCADE -> new ArcadeMode();
            }
        });
    }

    private JButton createImageButton(String path) {
        Image img = loadImage(path);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                if (img == null) {
                    g.setColor(new Color(0xC8, 0xA0, 0x28));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Serif", Font.PLAIN, 12));
                    g.drawString(getText(), 10, getHeight()/2 + 5);
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int iw = img.getWidth(null), ih = img.getHeight(null);
                if (iw <= 0 || ih <= 0) { g2.dispose(); return; }

                double scale = Math.min((double)getWidth()/iw, (double)getHeight()/ih);
                int dw = (int)(iw*scale), dh = (int)(ih*scale);

                if (getModel().isRollover()) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
                }
                g2.drawImage(img, (getWidth()-dw)/2, (getHeight()-dh)/2, dw, dh, null);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(url).getImage();
    }

    protected void drawImageFill(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    protected void drawImageProportional(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.min((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    private class BackgroundPanel extends JPanel {
        private final Image img;
        BackgroundPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawImageFill(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    private class ScaledImagePanel extends JPanel {
        private final Image img;
        ScaledImagePanel(String p) { img = loadImage(p); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (img == titlePanel.img) drawImageProportional(g2, img, 0, 0, getWidth(), getHeight());
                else drawImageFill(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    private class ImagePanel extends JPanel {
        private final Image img;
        ImagePanel(String p) { img = loadImage(p); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawImageProportional(g2, img, 0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    // 🏆 NEW: State-driven Interactive Trophy Panel
    private class TrophyPanel extends JPanel {
        private float time = 0f;
        private final Image earnedImg = loadImage(TROPHY_EARNED);
        private final Image failedImg = loadImage(TROPHY_FAILED);

        TrophyPanel() {
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Kinetic Polish: Continually updates time for the sine wave
            new Timer(16, e -> { time += 0.04f; repaint(); }).start();

            // Interaction Trigger
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (ArcadeModeManager.isArcadeCompleted()) {
                        victoryOverlay.setVisible(true); // Pop open the interactive overlay
                    }
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            boolean isEarned = ArcadeModeManager.isArcadeCompleted();
            Image displayImg = isEarned ? earnedImg : failedImg;

            if (displayImg != null) {
                int iw = displayImg.getWidth(null), ih = displayImg.getHeight(null);
                double scale = Math.min((double) getWidth() / iw, (double) getHeight() / ih);
                int dw = (int) (iw * scale), dh = (int) (ih * scale);
                int dx = (getWidth() - dw) / 2;

                // 🪄 Kinetic Hover: Smoothly oscillates up and down by 8 pixels
                int dy = (getHeight() - dh) / 2 + (int)(Math.sin(time) * 8);

                g2.drawImage(displayImg, dx, dy, dw, dh, null);
            }
            g2.dispose();
        }
    }
}