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

/**
 * MainMenuFrame
 *
 * Changes from original:
 *   • Fog effect REMOVED — plain background image, no animated overlays
 *   • Trophy FIXED — correct proportions, right-side gap between holder
 *     and right pillar, full opacity, no "uncanny compressed" look
 *   • Trophy drawn with explicit width/height from a single
 *     `trophySize` value so it's always a proper square diamond
 */
public class MainMenuFrame extends JFrame {

    private JButton arcadeButton, PVPButton, PVEButton, exitGameButton;

    private static final String BG_PATH      = "/resources/background.png";
    private static final String COLUMNS_PATH = "/resources/columns.png";
    private static final String TITLE_PATH   = "/resources/mainMenu (1).png";
    private static final String HOLDER_PATH  = "/resources/mainMenuHolder.png";
    private static final String BTN_ARCADE   = "/resources/ArcadeButton (1).png";
    private static final String BTN_PVE      = "/resources/PVEButton (1).png";
    private static final String BTN_PVP      = "/resources/PVPButton (1).png";
    private static final String BTN_EXIT     = "/resources/exitButton (3).png";

    private ImagePanel       holderPanel;
    private JPanel           buttonsInsideHolder;
    private JPanel           exitRow;
    private ScaledImagePanel titlePanel;
    private TrophyPanel      trophyPanel;

    public MainMenuFrame() {
        setTitle("Encantadia: Echoes of the Gem — Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setResizable(true);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        // Layer 0 — plain background (NO fog)
        BackgroundPanel bg = new BackgroundPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        // Layer 1 — columns
        ScaledImagePanel columns = new ScaledImagePanel(COLUMNS_PATH);
        lp.add(columns, JLayeredPane.PALETTE_LAYER);

        // Layer 2 — title image
        titlePanel = new ScaledImagePanel(TITLE_PATH);
        lp.add(titlePanel, JLayeredPane.MODAL_LAYER);

        // Layer 3 — stone holder with buttons
        holderPanel = new ImagePanel(HOLDER_PATH);
        holderPanel.setLayout(new GridBagLayout());
        lp.add(holderPanel, JLayeredPane.POPUP_LAYER);

        arcadeButton = createImageButton(BTN_ARCADE);
        PVEButton    = createImageButton(BTN_PVE);
        PVPButton    = createImageButton(BTN_PVP);

        buttonsInsideHolder = new JPanel();
        buttonsInsideHolder.setOpaque(false);
        buttonsInsideHolder.setLayout(new BoxLayout(buttonsInsideHolder, BoxLayout.Y_AXIS));
        for (JButton b : new JButton[]{PVPButton, PVEButton, arcadeButton})
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonsInsideHolder.add(Box.createVerticalGlue());
        buttonsInsideHolder.add(PVPButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(14));
        buttonsInsideHolder.add(PVEButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(14));
        buttonsInsideHolder.add(arcadeButton);
        buttonsInsideHolder.add(Box.createVerticalGlue());
        holderPanel.add(buttonsInsideHolder);

        // Layer 4 — exit button
        exitGameButton = createImageButton(BTN_EXIT);
        exitRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        exitRow.setOpaque(false);
        exitRow.add(exitGameButton);
        lp.add(exitRow, JLayeredPane.DRAG_LAYER);

        // Layer 5 — trophy gem (topmost, won't clip behind pillars)
        trophyPanel = new TrophyPanel();
        lp.add(trophyPanel, JLayeredPane.DRAG_LAYER);
        lp.setLayer(trophyPanel, JLayeredPane.DRAG_LAYER);

        // Actions
        PVPButton.addActionListener(   e -> launchMode(GameModeType.PVP));
        PVEButton.addActionListener(   e -> launchMode(GameModeType.PVE));
        arcadeButton.addActionListener(e -> launchMode(GameModeType.ARCADE));
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

    // ── Layout ────────────────────────────────────────────────
    private void reposition(JLayeredPane pane, JPanel bg, JPanel columns) {
        int w = pane.getWidth(), h = pane.getHeight();
        if (w == 0 || h == 0) return;

        bg.setBounds(0, 0, w, h);
        columns.setBounds(0, 0, w, h);

        // Title
        int titleW = (int)(w * 0.42);
        int titleH = (int)(titleW * 0.28);
        titlePanel.setBounds((w-titleW)/2, (int)(h*0.04), titleW, titleH);

        // Holder
        int holderW = (int)(w * 0.38);
        int holderH = (int)(h * 0.58);
        int holderX = (w - holderW) / 2;
        int holderY = (int)(h*0.04) + titleH + (int)(h*0.015);
        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        // Buttons inside holder
        int btnW      = (int)(holderW * 0.76);
        int btnH      = (int)(btnW * 0.24);
        int usableH   = (int)(holderH * 0.75);
        int gap       = Math.max(10, (usableH - btnH*3) / 4);
        for (JButton b : new JButton[]{PVPButton, PVEButton, arcadeButton}) {
            Dimension d = new Dimension(btnW, btnH);
            b.setPreferredSize(d); b.setMinimumSize(d); b.setMaximumSize(d);
        }
        buttonsInsideHolder.setBounds(0, 0, holderW, holderH);
        buttonsInsideHolder.removeAll();
        buttonsInsideHolder.add(Box.createVerticalGlue());
        buttonsInsideHolder.add(PVPButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(gap));
        buttonsInsideHolder.add(PVEButton);
        buttonsInsideHolder.add(Box.createVerticalStrut(gap));
        buttonsInsideHolder.add(arcadeButton);
        buttonsInsideHolder.add(Box.createVerticalGlue());

        // Exit
        int exitW = (int)(btnW * 0.46);
        int exitH = (int)(exitW * 0.45);
        Dimension ed = new Dimension(exitW, exitH);
        exitGameButton.setPreferredSize(ed);
        exitGameButton.setMinimumSize(ed);
        exitGameButton.setMaximumSize(ed);
        exitRow.setBounds((w-exitW)/2, holderY+holderH+(int)(h*0.008), exitW, exitH+4);

        // ── Trophy — RIGHT side gap, proper square bounding box ──
        // The right pillar occupies approximately the rightmost 13 % of the frame.
        // The holder's right edge is holderX+holderW. The gap centre is the midpoint
        // of the space between that edge and the left edge of the right pillar.
        int rightPillarLeft = (int)(w * 0.87);
        int gapCentreX      = holderX + holderW + (rightPillarLeft - holderX - holderW) / 2;

        // Trophy size: 8 % of shorter dimension, min 60 px, max 120 px
        int trophySize = Math.min(120, Math.max(60, (int)(Math.min(w, h) * 0.08)));

        // Position: centred on gap, vertically centred to holder
        int trophyX = gapCentreX - trophySize / 2;
        int trophyY = holderY + (holderH - trophySize) / 2;
        trophyPanel.setBounds(trophyX, trophyY, trophySize, trophySize);

        holderPanel.revalidate(); holderPanel.repaint();
        exitRow.revalidate();     exitRow.repaint();
    }

    private void launchMode(GameModeType mode) {
        dispose();
        switch (mode) {
            case PVE:    new PVEMode();    break;
            case PVP:    new PVPMode();    break;
            case ARCADE: new ArcadeMode(); break;
        }
    }

    // ── Image button ──────────────────────────────────────────
    private JButton createImageButton(String path) {
        Image img = loadImage(path);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                if (img == null) { super.paintComponent(g); return; }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int iw = img.getWidth(null), ih = img.getHeight(null);
                if (iw <= 0 || ih <= 0) { g2.dispose(); return; }
                double scale = Math.min((double)getWidth()/iw, (double)getHeight()/ih);
                int dw = (int)(iw*scale), dh = (int)(ih*scale);
                if (getModel().isRollover())
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.80f));
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
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    // ── Background — plain, NO fog ────────────────────────────
    private class BackgroundPanel extends JPanel {
        private final Image img;
        BackgroundPanel(String p) {
            img = loadImage(p); setOpaque(true); setBackground(Color.BLACK);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        }
    }

    private class ScaledImagePanel extends JPanel {
        private final Image img;
        ScaledImagePanel(String p) { img = loadImage(p); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        }
    }

    private class ImagePanel extends JPanel {
        private final Image img;
        ImagePanel(String p) { img = loadImage(p); setOpaque(false); }
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

    // ── Trophy — animated golden diamond gem ──────────────────
    // Drawn entirely via code so it looks correct at every size.
    // Visibility: only shown when ArcadeModeManager.isArcadeCompleted().
    // Layout: occupies a SQUARE bounding box (trophySize × trophySize)
    // so the diamond is never squashed.
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();

            // Reserve bottom 22 % for the label; gem fills the rest
            int labelH    = Math.max(12, (int)(H * 0.22));
            int gemArea   = H - labelH;
            int cx        = W / 2;
            int cy        = gemArea / 2;

            // Diamond half-size: 40 % of the smaller gem-area dimension, pulsing
            float pulse = (float)(0.82 + 0.18 * Math.sin(time));
            int sz = (int)(Math.min(W, gemArea) * 0.40 * pulse);

            int[] xs = { cx,      cx + sz, cx,      cx - sz };
            int[] ys = { cy - sz, cy,      cy + sz, cy      };

            // Outer glow rings
            for (int ring = 6; ring >= 1; ring--) {
                int alpha = Math.min(255, 18 * (7 - ring));
                g2.setColor(new Color(0xC8, 0xA0, 0x28, alpha));
                g2.setStroke(new BasicStroke(ring * 2f));
                g2.drawPolygon(xs, ys, 4);
            }

            // Fill — gradient top-to-bottom
            g2.setPaint(new GradientPaint(
                    cx, cy - sz, new Color(0xFF, 0xF0, 0x60),
                    cx, cy + sz, new Color(0xFF, 0x80, 0x00)));
            g2.fillPolygon(xs, ys, 4);

            // Inner shine streak
            g2.setColor(new Color(255, 255, 255, 105));
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawLine(cx - sz/3, cy - sz/2, cx + sz/5, cy - sz/6);

            // Crisp border
            g2.setColor(new Color(0xC8, 0xA0, 0x28));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawPolygon(xs, ys, 4);

            // Label — "Arcade" centred below gem area
            int fontSize = Math.max(8, (int)(labelH * 0.70));
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            String lbl = "Arcade";
            int lx = cx - fm.stringWidth(lbl) / 2;
            int ly = gemArea + fm.getAscent();
            // Shadow
            g2.setColor(new Color(0, 0, 0, 120));
            g2.drawString(lbl, lx + 1, ly + 1);
            // Gold text
            g2.setColor(new Color(0xC8, 0xA0, 0x28));
            g2.drawString(lbl, lx, ly);

            g2.dispose();
        }
    }

    private void createUIComponents() {}
}