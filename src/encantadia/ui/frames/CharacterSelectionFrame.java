package encantadia.ui.frames;

import encantadia.BackstoryShowcase;
import encantadia.ScreenManager;
import encantadia.battle.EnemyFactory;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.characters.*;
import encantadia.characters.Character;
import encantadia.gamemode.GameModeType;
import encantadia.story.CharacterStories;
import encantadia.ui.frames.battleModeFrames.ArcadeModeBattleFrame;
import encantadia.ui.frames.battleModeFrames.PVEBattleFrame;
import encantadia.ui.frames.battleModeFrames.PVPBattleFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class CharacterSelectionFrame extends JFrame {

    // ── Selection countdown ────────────────────────────────────
    private static final int SELECTION_SECONDS = 10; // easy to tune
    private Timer               selectionCountdownTimer;
    private Timer               selPulseAnimTimer;
    private int                 selectionTimeLeft = SELECTION_SECONDS;
    private float               selPulse          = 0f;
    private SelectionTimerBadge timerBadge;

    public static int FRAME_SIZE = 800;
    private final GameModeType gameModeType;

    // ── Resource paths ────────────────────────────────────────
    private static final String BG_PATH    = "/resources/background (4).png";
    private static final String TITLE_PATH = "/resources/chooseSangreTitle.png";

    private static final String[] FRAME_IMGS = {
            "/resources/TyroneFrameName.png",
            "/resources/ElanFrameName.png",
            "/resources/ClaireFrameName.png",
            "/resources/DirkFrameName.png",
            "/resources/FlamaraFrameName.png",
            "/resources/DeaFrameName.png",
            "/resources/AdamusFrameName.png",
            "/resources/TeraFrameName.png"
    };
    private static final String[] CHAR_NAMES = {
            "Tyrone", "Elan", "Claire", "Dirk",
            "Flamara", "Dea", "Adamus", "Tera"
    };
    private static final Color[] GLOW_COLORS = {
            new Color(0xFF, 0x60, 0x20),
            new Color(0x40, 0xA0, 0xFF),
            new Color(0x40, 0xE0, 0x60),
            new Color(0xFF, 0xCC, 0x30),
            new Color(0xFF, 0x40, 0x20),
            new Color(0x60, 0xA0, 0xFF),
            new Color(0x30, 0xDD, 0x88),
            new Color(0xFF, 0xCC, 0x00),
    };

    // ── UI refs ───────────────────────────────────────────────
    private JButton        exitButton;
    private BgPanel        bgPanel;
    private ScaledImgPanel titlePanel;
    private CharFramePanel[] framePanels = new CharFramePanel[8];

    // ── Bob animation ─────────────────────────────────────────
    private final int[] baseY    = new int[8];
    private double      animTick = 0;
    private Timer       animTimer;

    // ══════════════════════════════════════════════════════════
    //  Constructors
    // ══════════════════════════════════════════════════════════
    public CharacterSelectionFrame(GameModeType gameModeType) {
        this.gameModeType = gameModeType;
        init();
    }

    public CharacterSelectionFrame() { this(GameModeType.PVE); }

    // ══════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════
    private void init() {
        setTitle("Choose Your Sangre  [" + gameModeType.name() + "]");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setResizable(true);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        // Layer 0: background
        bgPanel = new BgPanel(BG_PATH);
        lp.add(bgPanel, JLayeredPane.DEFAULT_LAYER);

        // Layer 1: title
        titlePanel = new ScaledImgPanel(TITLE_PATH);
        lp.add(titlePanel, JLayeredPane.PALETTE_LAYER);

        // Layer 2: character cards
        Character[] roster = buildRoster();
        for (int i = 0; i < 8; i++) {
            final int       idx = i;
            final Character ch  = roster[i];
            framePanels[i] = new CharFramePanel(
                    FRAME_IMGS[i], CHAR_NAMES[i], GLOW_COLORS[i], i);
            framePanels[i].addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { onCharacterSelected(ch); }
                @Override public void mouseEntered(MouseEvent e) { framePanels[idx].setHovered(true); }
                @Override public void mouseExited(MouseEvent e)  { framePanels[idx].setHovered(false); }
            });
            framePanels[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lp.add(framePanels[i], JLayeredPane.MODAL_LAYER);
        }

        // Layer 3: timer badge + exit button
        timerBadge = new SelectionTimerBadge();
        lp.add(timerBadge, JLayeredPane.POPUP_LAYER);

        exitButton = new JButton();
        Image exitImg = loadImage("/resources/exitButton (1).png");
        if (exitImg != null) exitButton.setIcon(new ImageIcon(exitImg));
        exitButton.setContentAreaFilled(false);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setOpaque(false);
        exitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exitButton.addActionListener(e -> { stopSelectionTimer(); dispose(); new MainMenuFrame(); });
        lp.add(exitButton, JLayeredPane.POPUP_LAYER);

        // Bob animation — 60 fps
        animTimer = new Timer(16, e -> {
            animTick += 0.06;
            for (int i = 0; i < 8; i++) {
                double phase = animTick + i * (Math.PI / 4);
                int    bob   = (int)(Math.sin(phase) * 5);
                if (baseY[i] > 0)
                    framePanels[i].setLocation(framePanels[i].getX(), baseY[i] + bob);
                framePanels[i].repaint();
            }
        });
        animTimer.start();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { reposition(lp); }
        });

        setVisible(true);
        ScreenManager.register(this);
        SwingUtilities.invokeLater(() -> {
            reposition(lp);
            startSelectionTimer(); // start AFTER layout so badge is positioned
        });
    }

    @Override
    public void dispose() {
        stopSelectionTimer();          // always cancel before leaving
        if (animTimer != null) animTimer.stop();
        ScreenManager.unregister(this);
        super.dispose();
    }

    // ══════════════════════════════════════════════════════════
    //  Layout
    // ══════════════════════════════════════════════════════════
    private void reposition(JLayeredPane pane) {
        int W = pane.getWidth(), H = pane.getHeight();
        if (W == 0 || H == 0) return;

        bgPanel.setBounds(0, 0, W, H);

        double scale = Math.min(Math.min(W / 1024.0, H / 768.0), 1.5);

        // Title
        int titleW = (int)(560 * scale);
        int titleH = (int)(titleW / 3.5);
        int titleX = (W - titleW) / 2;
        int titleY = (int)(30 * scale);
        titlePanel.setBounds(titleX, titleY, titleW, titleH);

        // Timer badge — centered, immediately below the title
        int badgeW = (int)(220 * scale);
        int badgeH = (int)(58  * scale);
        int badgeX = (W - badgeW) / 2;
        int badgeY = titleY + titleH + (int)(6 * scale);
        if (timerBadge != null) timerBadge.setBounds(badgeX, badgeY, badgeW, badgeH);

        // Character grid — pushed below the badge
        int cellW = (int)(150 * scale);
        int cellH = (int)(150 * scale);
        int gapX  = (int)(36  * scale);
        int gapY  = (int)(32  * scale);
        int cols  = 4;
        int gridW = cols * cellW + (cols - 1) * gapX;
        int gridX = (W - gridW) / 2;
        int gridY = badgeY + badgeH + (int)(10 * scale);

        for (int i = 0; i < 8; i++) {
            int col = i % cols, row = i / cols;
            int fx  = gridX + col * (cellW + gapX);
            int fy  = gridY + row * (cellH + gapY);
            framePanels[i].setBounds(fx, fy, cellW, cellH);
            baseY[i] = fy;
        }

        // Exit button — below the grid
        int exitW = (int)(140 * scale);
        int exitH = (int)(50  * scale);
        int exitX = (W - exitW) / 2;
        int exitY = gridY + 2 * cellH + gapY + (int)(20 * scale);
        exitButton.setBounds(exitX, exitY, exitW, exitH);
        Image exitImg = loadImage("/resources/exitButton (1).png");
        if (exitImg != null)
            exitButton.setIcon(new ImageIcon(
                    exitImg.getScaledInstance(exitW, exitH, Image.SCALE_SMOOTH)));

        pane.revalidate();
        pane.repaint();
    }

    // ══════════════════════════════════════════════════════════
    //  Selection timer
    // ══════════════════════════════════════════════════════════

    /**
     * Starts the SELECTION_SECONDS auto-pick countdown.
     * Two timers are used:
     *   • 1-second logic timer  — decrements the counter and triggers auto-pick
     *   • 16 ms animation timer — drives the pulse/glow repaint cheaply
     */
    private void startSelectionTimer() {
        stopSelectionTimer();                    // safety: never double-start
        selectionTimeLeft = SELECTION_SECONDS;
        selPulse          = 0f;

        // Animation pulse — 60 fps, pure repaint, no game-state changes
        selPulseAnimTimer = new Timer(16, e -> {
            selPulse += 0.10f;
            if (timerBadge != null) timerBadge.repaint();
        });
        selPulseAnimTimer.start();

        // Logic tick — 1 per second
        selectionCountdownTimer = new Timer(1000, e -> {
            selectionTimeLeft--;
            if (timerBadge != null) timerBadge.repaint();
            if (selectionTimeLeft <= 0) {
                stopSelectionTimer();
                SwingUtilities.invokeLater(this::autoPickRandom);
            }
        });
        selectionCountdownTimer.start();
    }

    /** Cancels both timers and resets the counter. Safe to call multiple times. */
    private void stopSelectionTimer() {
        if (selectionCountdownTimer != null) {
            selectionCountdownTimer.stop();
            selectionCountdownTimer = null;
        }
        if (selPulseAnimTimer != null) {
            selPulseAnimTimer.stop();
            selPulseAnimTimer = null;
        }
        selectionTimeLeft = SELECTION_SECONDS;
    }

    /** Picks a random character and proceeds exactly as a manual click would. */
    private void autoPickRandom() {
        Character[] roster = buildRoster();
        Character   picked = roster[(int)(Math.random() * roster.length)];
        onCharacterSelected(picked);
    }

    // ══════════════════════════════════════════════════════════
    //  Game-flow logic
    // ══════════════════════════════════════════════════════════
    private void onCharacterSelected(Character character) {
        stopSelectionTimer();   // cancel countdown whether manual or auto
        dispose();
        switch (gameModeType) {
            case PVE:    startPVEFlow(character);    break;
            case PVP:    startPVPFlow(character);    break;
            case ARCADE: startArcadeFlow(character); break;
        }
    }

    private void startPVEFlow(Character character) {
        Character enemy = EnemyFactory.getRandomEnemy(character);
        Runnable launchBattle   = () -> new PVEBattleFrame(character, enemy);
        Runnable showEnemyStory = () -> new BackstoryShowcase(
                CharacterStories.getEnemyStory(enemy),
                CharacterStories.getEnemyTitle(enemy),
                launchBattle,
                () -> new CharacterSelectionFrame(GameModeType.PVE));
        new BackstoryShowcase(
                CharacterStories.getCharacterStory(character),
                CharacterStories.getCharacterTitle(character),
                showEnemyStory,
                () -> new CharacterSelectionFrame(GameModeType.PVE));
    }

    private void startPVPFlow(Character character) {
        Runnable goToP2Selection = () -> new PVPBattleFrame(character);
        new BackstoryShowcase(
                CharacterStories.getCharacterStory(character),
                CharacterStories.getCharacterTitle(character),
                goToP2Selection,
                () -> new CharacterSelectionFrame(GameModeType.PVP));
    }

    private void startArcadeFlow(Character character) {
        Runnable launchTower = () ->
                new ArcadeTowerFrame(character, new ArcadeModeManager(character));
        new BackstoryShowcase(
                CharacterStories.getCharacterStory(character),
                CharacterStories.getCharacterTitle(character),
                launchTower,
                () -> new CharacterSelectionFrame(GameModeType.ARCADE));
    }

    private Character[] buildRoster() {
        return new Character[]{
                new Tyrone(), new MakelanShere(), new Mary(), new Dirk(),
                new Flamara(), new Dea(), new Adamus(), new Tera()
        };
    }

    // ══════════════════════════════════════════════════════════
    //  SelectionTimerBadge
    //
    //  Self-contained widget — reads only selectionTimeLeft and
    //  selPulse from the outer class. No battle-frame fields.
    //  Colors: gold (>6s) → orange (4-6s) → red (≤3s, pulsing).
    // ══════════════════════════════════════════════════════════
    private class SelectionTimerBadge extends JPanel {

        SelectionTimerBadge() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();

            // Urgency color
            Color base = selectionTimeLeft > 6
                    ? new Color(0xC8, 0xA0, 0x28)   // gold
                    : selectionTimeLeft > 3
                    ? new Color(0xCC, 0x66, 0x11)   // orange
                    : new Color(0xCC, 0x22, 0x22);  // red

            // Pulse scale on final 3 seconds
            float pulse = (selectionTimeLeft <= 3)
                    ? 1f + 0.07f * (float) Math.abs(Math.sin(selPulse))
                    : 1f;

            // Card background
            int bw = (int)(W * 0.88), bh = H - 4;
            int bx = (W - bw) / 2,   by = 2;

            g2.setColor(new Color(0x0A, 0x07, 0x03, 235));
            g2.fillRoundRect(bx, by, bw, bh, 14, 14);

            // Outer border
            g2.setStroke(new BasicStroke(2.5f));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 210));
            g2.drawRoundRect(bx, by, bw, bh, 14, 14);

            // Inner inset (runic double-line)
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 70));
            g2.drawRoundRect(bx + 4, by + 3, bw - 8, bh - 6, 8, 8);

            // "AUTO-PICK IN" micro-label
            int lblSize = Math.max(9, (int)(H * 0.22));
            g2.setFont(new Font("Serif", Font.ITALIC, lblSize));
            FontMetrics lfm = g2.getFontMetrics();
            String lbl = "AUTO-PICK IN";
            int lx = (W - lfm.stringWidth(lbl)) / 2;
            int ly = by + lfm.getAscent() + 3;
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 155));
            g2.drawString(lbl, lx, ly);

            // Big countdown ◆  N  ◆
            int numSize = (int)(Math.max(22, H * 0.55) * pulse);
            g2.setFont(new Font("Serif", Font.BOLD, numSize));
            FontMetrics nfm = g2.getFontMetrics();
            String full = "\u25C6  " + selectionTimeLeft + "  \u25C6";
            int nx = (W - nfm.stringWidth(full)) / 2;
            int ny = ly + nfm.getAscent() + 2;

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(full, nx + 2, ny + 2);

            // Glow rings on critical
            if (selectionTimeLeft <= 3) {
                for (int ring = 3; ring >= 1; ring--) {
                    g2.setColor(new Color(base.getRed(), base.getGreen(),
                            base.getBlue(), 18 * (4 - ring)));
                    g2.drawString(full, nx - ring, ny);
                    g2.drawString(full, nx + ring, ny);
                }
            }

            // Main text
            g2.setColor(base);
            g2.drawString(full, nx, ny);

            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  CharFramePanel
    // ══════════════════════════════════════════════════════════
    private class CharFramePanel extends JPanel {
        private final Image  frameImg;
        private final String charName;
        private final Color  glowColor;
        private final int    slotIndex;
        private boolean      hovered   = false;
        private float        glowAlpha = 0f;
        private final Timer  glowTimer;

        CharFramePanel(String imgPath, String name, Color glow, int idx) {
            this.frameImg  = loadImage(imgPath);
            this.charName  = name;
            this.glowColor = glow;
            this.slotIndex = idx;
            setOpaque(false);
            setLayout(null);

            glowTimer = new Timer(20, e -> {
                if (hovered && glowAlpha < 1f) {
                    glowAlpha = Math.min(1f, glowAlpha + 0.08f); repaint();
                } else if (!hovered && glowAlpha > 0f) {
                    glowAlpha = Math.max(0f, glowAlpha - 0.06f); repaint();
                }
            });
            glowTimer.start();
        }

        void setHovered(boolean h) { this.hovered = h; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);

            int W = getWidth(), H = getHeight();

            // Glow rings
            if (glowAlpha > 0f) {
                for (int ring = 4; ring >= 1; ring--) {
                    int   spread = ring * 6;
                    float alpha  = (glowAlpha * 0.25f) / ring;
                    g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(),
                            glowColor.getBlue(), Math.min(255, (int)(alpha * 255))));
                    g2.setStroke(new BasicStroke(spread));
                    g2.drawRoundRect(spread/2, spread/2, W-spread, H-spread, 16, 16);
                }
            }

            float sc = hovered ? 1.08f : 1.0f;

            if (frameImg != null) {
                int iw = frameImg.getWidth(null), ih = frameImg.getHeight(null);
                double scale = ((double) FRAME_SIZE / iw) * sc;
                int drawW = (int)(iw * scale), drawH = (int)(ih * scale);
                g2.drawImage(frameImg, (W - drawW)/2, (H - drawH)/2, drawW, drawH, null);
            } else {
                g2.setColor(new Color(60, 50, 30));
                g2.fillRoundRect((W-200)/2, (H-200)/2, 200, 200, 12, 12);
            }

            // Hover name tooltip
            if (glowAlpha > 0.05f) {
                int fontSize = Math.max(10, (int)(H * 0.13));
                g2.setFont(new Font("SansSerif", Font.BOLD, fontSize));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (W - fm.stringWidth(charName)) / 2;
                int ty = H - (int)(H * 0.06);
                int pillPad = 6;
                int pillW = fm.stringWidth(charName) + pillPad * 2;
                int pillH = fm.getHeight() + 2;
                int pillX = (W - pillW) / 2;
                int pillY = ty - fm.getAscent() - 2;

                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, glowAlpha * 0.85f));
                g2.setColor(glowColor.darker());
                g2.fillRoundRect(pillX, pillY, pillW, pillH, pillH, pillH);

                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, glowAlpha));
                g2.setColor(new Color(0, 0, 0, 160));
                g2.drawString(charName, tx + 1, ty + 1);
                g2.setColor(Color.WHITE);
                g2.drawString(charName, tx, ty);
            }

            g2.dispose();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Image helpers
    // ══════════════════════════════════════════════════════════
    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String path) { img = loadImage(path); setOpaque(true); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            } else { setBackground(Color.BLACK); }
        }
    }

    private class ScaledImgPanel extends JPanel {
        private final Image img;
        ScaledImgPanel(String path) { img = loadImage(path); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing resource: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    private void createUIComponents() {}
}