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
import java.net.URL;

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

    // Updated path to point to the new pixel-art asset
    private static final String BTN_LEADER   = "/resources/leaderboardBUTTON.png";

    private ImagePanel       holderPanel;
    private JPanel           buttonsInsideHolder;
    private JPanel           exitRow;
    private ScaledImagePanel titlePanel;
    private TrophyPanel      trophyPanel;

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
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        ScaledImagePanel columns = new ScaledImagePanel(COLUMNS_PATH);
        lp.add(columns, JLayeredPane.PALETTE_LAYER);

        titlePanel = new ScaledImagePanel(TITLE_PATH);
        lp.add(titlePanel, JLayeredPane.MODAL_LAYER);

        holderPanel = new ImagePanel(HOLDER_PATH);
        holderPanel.setLayout(new GridBagLayout());
        lp.add(holderPanel, JLayeredPane.POPUP_LAYER);

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

        // --- CHANGE: Manual text is no longer needed ---
        // leaderboardButton.setText("Leaderboard");

        exitGameButton = createImageButton(BTN_EXIT);
        exitGameButton.setText("Exit");

        exitRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        exitRow.setOpaque(false);
        exitRow.add(leaderboardButton);
        exitRow.add(exitGameButton);
        lp.add(exitRow, JLayeredPane.DRAG_LAYER);

        trophyPanel = new TrophyPanel();
        lp.add(trophyPanel, JLayeredPane.DRAG_LAYER);

        PVPButton.addActionListener(   e -> launchMode(GameModeType.PVP));
        PVEButton.addActionListener(   e -> launchMode(GameModeType.PVE));
        arcadeButton.addActionListener(e -> launchMode(GameModeType.ARCADE));
        leaderboardButton.addActionListener(e -> { dispose(); new LeaderboardFrame(); });
        exitGameButton.addActionListener(e -> { dispose(); new WelcomeScreenPage(); });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reposition(lp, bg, columns);
            }
        });

        setVisible(true);
        ScreenManager.register(this);
        SwingUtilities.invokeLater(() -> reposition(lp, bg, columns));
    }

    @Override
    public void dispose() { ScreenManager.unregister(this); super.dispose(); }

    private void reposition(JLayeredPane pane, JPanel bg, JPanel columns) {
        int w = pane.getWidth(), h = pane.getHeight();
        if (w == 0 || h == 0) return;

        bg.setBounds(0, 0, w, h);
        columns.setBounds(0, 0, w, h);

        int holderW = Math.min(600, (int)(w * 0.55));
        int holderH = Math.min(680, (int)(h * 0.85));
        int holderX = (w - holderW) / 2;
        int holderY = (int)(h * 0.10);
        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        int titleW = Math.min(680, (int)(w * 0.65));
        int titleH = (int)(titleW * 0.30);
        int titleX = (w - titleW) / 2;
        int titleY = Math.max(0, holderY - (int)(titleH * 0.60));
        titlePanel.setBounds(titleX, titleY, titleW, titleH);

        int btnW = (int)(holderW * 0.65);
        int btnH = (int)(btnW * 0.28);
        int gap  = Math.max(10, (int)(holderH * 0.03));

        Dimension mainSize = new Dimension(btnW, btnH);
        for (JButton b : new JButton[]{PVPButton, PVEButton, arcadeButton}) {
            b.setPreferredSize(mainSize);
            b.setMinimumSize(mainSize);
            b.setMaximumSize(mainSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        // --- OPTIONAL: Adjust the size of the exit/leaderboard buttons if needed ---
        // This size might feel different now that it's a fixed image.
        int exitW = (int)(btnW * 0.45);
        int exitH = (int)(exitW * 0.45);
        Dimension exitSize = new Dimension(exitW, exitH);

        exitGameButton.setPreferredSize(exitSize);
        leaderboardButton.setPreferredSize(exitSize);
        exitGameButton.setContentAreaFilled(false);
        leaderboardButton.setContentAreaFilled(false);

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

        int rightPillarLeft = (int)(w * 0.85);
        int gapCentreX = holderX + holderW + (rightPillarLeft - holderX - holderW) / 2;
        int trophySize = Math.min(140, Math.max(70, (int)(Math.min(w, h) * 0.12)));

        int trophyX = gapCentreX - trophySize / 2;
        int trophyY = holderY + (holderH - trophySize) / 2;
        trophyPanel.setBounds(trophyX, trophyY, trophySize, trophySize);

        holderPanel.revalidate(); holderPanel.repaint();
        pane.revalidate();        pane.repaint();
    }

    private void launchMode(GameModeType mode) {
        dispose();
        switch (mode) {
            case PVE:    new PVEMode();    break;
            case PVP:    new PVPMode();    break;
            case ARCADE: new ArcadeMode(); break;
        }
    }

    private JButton createImageButton(String path) {
        Image img = loadImage(path);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                // FALLBACK PAINTING: This draws the yellow rectangle seen before
                // It will only execute if img == null, so if your asset path is correct,
                // this code is ignored.
                if (img == null) {
                    g.setColor(new Color(0xC8, 0xA0, 0x28));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.BLACK);
                    // The font settings here are what control the text in the placeholder
                    g.setFont(new Font("Serif", Font.PLAIN, 12));
                    g.drawString(getText(), 10, getHeight()/2 + 5);
                    return;
                }

                // MAIN IMAGE PAINTING (executes when the asset is found)
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

    private class TrophyPanel extends JPanel {
        private float time = 0f;
        TrophyPanel() {
            setOpaque(false);
            new Timer(16, e -> { time += 0.04f; repaint(); }).start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!ArcadeModeManager.isArcadeCompleted()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int labelH  = Math.max(16, (int)(H * 0.22));
            int gemArea = H - labelH;
            int cx = W / 2, cy = gemArea / 2;

            float pulse = (float)(0.82 + 0.18 * Math.sin(time));
            int sz = (int)(Math.min(W, gemArea) * 0.40 * pulse);

            int[] xs = { cx,      cx + sz, cx,      cx - sz };
            int[] ys = { cy - sz, cy,      cy + sz, cy      };

            for (int ring = 6; ring >= 1; ring--) {
                int alpha = Math.min(255, 18 * (7 - ring));
                g2.setColor(new Color(0xC8, 0xA0, 0x28, alpha));
                g2.setStroke(new BasicStroke(ring * 2f));
                g2.drawPolygon(xs, ys, 4);
            }

            g2.setPaint(new GradientPaint(
                    cx, cy - sz, new Color(0xFF, 0xF0, 0x60),
                    cx, cy + sz, new Color(0xFF, 0x80, 0x00)));
            g2.fillPolygon(xs, ys, 4);

            g2.setColor(new Color(255, 255, 255, 105));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawLine(cx - sz/3, cy - sz/2, cx + sz/5, cy - sz/6);

            g2.setColor(new Color(0xC8, 0xA0, 0x28));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawPolygon(xs, ys, 4);

            int fontSize = Math.max(8, (int)(labelH * 0.70));
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            String lbl = "Arcade";
            int lx = cx - fm.stringWidth(lbl) / 2;
            int ly = gemArea + fm.getAscent();

            g2.setColor(new Color(0, 0, 0, 120));
            g2.drawString(lbl, lx + 1, ly + 1);
            g2.setColor(new Color(0xC8, 0xA0, 0x28));
            g2.drawString(lbl, lx, ly);

            g2.dispose();
        }
    }
}