package encantadia.ui.frames;

import encantadia.ScreenManager;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.characters.Character;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * ArcadeVictoryFrame — shown when the player clears the Arcade Tower.
 * FIXED: "Alpha out of range" crash fixed by strictly clamping particle opacity between 0 and 255.
 */
public class ArcadeVictoryFrame extends JFrame {

    private static final String BG_PATH = "/resources/background (4).png";

    // ── Lore lines revealed by typewriter ─────────────────────
    private static final String[] LORE_LINES = {
            "The Tower of Trials has fallen silent.",
            "Jelian and Joygen have been found — and freed.",
            "Encantadia breathes again."
    };

    private final Character winner;
    private Timer           animTimer;
    private float           tick = 0f;

    // Typewriter state
    private int    loreLineIdx  = 0;
    private int    loreCharIdx  = 0;
    private Timer  typeTimer;
    private Timer  pauseTimer;

    // UI refs
    private JLabel loreLabel;
    private JLabel dotLabel;

    public ArcadeVictoryFrame(Character winner) {
        this.winner = winner;
        ArcadeModeManager.setArcadeCompleted(true);

        setTitle("Encantadia — Arcade Clear!");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        buildUI();
        setVisible(true);
        ScreenManager.register(this);

        // Start typewriter after a short dramatic pause
        pauseTimer = new Timer(700, e -> {
            ((Timer)e.getSource()).stop();
            startTypewriter();
        });
        pauseTimer.setRepeats(false);
        pauseTimer.start();
    }

    @Override
    public void dispose() {
        if (animTimer  != null) { animTimer.stop();  animTimer = null; }
        if (typeTimer  != null) { typeTimer.stop();  typeTimer = null; }
        if (pauseTimer != null) { pauseTimer.stop(); pauseTimer = null; }
        ScreenManager.unregister(this);
        super.dispose();
    }

    // ══════════════════════════════════════════════════════════
    //  UI
    // ══════════════════════════════════════════════════════════
    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        VictoryBgPanel bg = new VictoryBgPanel();
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        JPanel content = buildContent();
        content.setOpaque(false);
        lp.add(content, JLayeredPane.PALETTE_LAYER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                bg.setBounds(0, 0, getWidth(), getHeight());
                content.setBounds(0, 0, getWidth(), getHeight());
                content.revalidate();
                content.repaint();
            }
        });
        SwingUtilities.invokeLater(() -> {
            bg.setBounds(0, 0, getWidth(), getHeight());
            content.setBounds(0, 0, getWidth(), getHeight());
        });

        animTimer = new Timer(16, e -> { tick += 0.045f; lp.repaint(); });
        animTimer.start();
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());

        // ── Title ──────────────────────────────────────────────
        JLabel titleLbl = new JLabel("✦  ARCADE CLEAR  ✦", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int W = getWidth(), H = getHeight();
                int fs = Math.max(32, Math.min(68, W / 10));
                g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, fs));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                int tx = (W - fm.stringWidth(txt)) / 2, ty = (H + fm.getAscent() - fm.getDescent()) / 2;

                // Outer glow
                for (int sp = 12; sp >= 1; sp--) {
                    g2.setColor(new Color(0xC8, 0xA0, 0x28, Math.max(5, 35 / sp)));
                    g2.drawString(txt, tx - sp/2, ty);
                    g2.drawString(txt, tx + sp/2, ty);
                }
                g2.setColor(new Color(0, 0, 0, 180)); g2.drawString(txt, tx+2, ty+2);
                float pulse = (float)(0.72 + 0.28 * Math.sin(tick));
                int gv = (int)(0xCC * pulse);
                g2.setColor(new Color(0xFF, Math.max(0, Math.min(255, gv)), 0x20));
                g2.drawString(txt, tx, ty);
                g2.dispose();
            }
        };
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLbl.setPreferredSize(new Dimension(900, 100));
        titleLbl.setMaximumSize(new Dimension(900, 100));

        // ── Gem ────────────────────────────────────────────────
        GemPanel gem = new GemPanel();
        gem.setAlignmentX(Component.CENTER_ALIGNMENT);
        gem.setPreferredSize(new Dimension(140, 140));
        gem.setMaximumSize(new Dimension(140, 140));

        // ── Winner line ────────────────────────────────────────
        JLabel winnerLbl = new JLabel(winner.getName() + " conquered the Tower of Trials!", SwingConstants.CENTER);
        winnerLbl.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 21));
        winnerLbl.setForeground(new Color(0xFF, 0xF5, 0xDC));
        winnerLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Typewriter lore ────────────────────────────────────
        loreLabel = new JLabel("", SwingConstants.CENTER);
        loreLabel.setFont(new Font("Serif", Font.ITALIC, 16));
        loreLabel.setForeground(new Color(0xC8, 0xA0, 0x28));
        loreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        dotLabel = new JLabel("", SwingConstants.CENTER);
        dotLabel.setFont(new Font("Serif", Font.BOLD, 18));
        dotLabel.setForeground(new Color(0xC8, 0xA0, 0x28, 180));
        dotLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Trophy note ────────────────────────────────────────
        JLabel trophyNote = new JLabel("✦  A trophy now rests on the Main Menu  ✦", SwingConstants.CENTER);
        trophyNote.setFont(new Font("Serif", Font.ITALIC, 12));
        trophyNote.setForeground(new Color(0xA0, 0x88, 0x50));
        trophyNote.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Return button ──────────────────────────────────────
        JButton returnBtn = makeGoldButton("Return to Main Menu");
        returnBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnBtn.addActionListener(e -> { dispose(); new MainMenuFrame(); });

        panel.add(titleLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(gem);
        panel.add(Box.createVerticalStrut(24));
        panel.add(winnerLbl);
        panel.add(Box.createVerticalStrut(12));
        panel.add(loreLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(dotLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(trophyNote);
        panel.add(Box.createVerticalStrut(32));
        panel.add(returnBtn);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // ── Typewriter ────────────────────────────────────────────
    private void startTypewriter() {
        loreLineIdx  = 0;
        loreCharIdx  = 0;
        typeOneLine();
    }

    private void typeOneLine() {
        if (loreLineIdx >= LORE_LINES.length) {
            dotLabel.setText("");
            return;
        }
        String line = LORE_LINES[loreLineIdx];
        loreCharIdx = 0;
        dotLabel.setText("▌"); // blinking caret

        typeTimer = new Timer(38, ev -> {
            if (loreCharIdx <= line.length()) {
                loreLabel.setText(line.substring(0, loreCharIdx));
                loreCharIdx++;
                dotLabel.setText(loreCharIdx % 10 < 5 ? "▌" : "");
            } else {
                typeTimer.stop();
                dotLabel.setText("");
                loreLineIdx++;

                pauseTimer = new Timer(1100, e2 -> {
                    ((Timer)e2.getSource()).stop();
                    typeOneLine();
                });
                pauseTimer.setRepeats(false);
                pauseTimer.start();
            }
        });
        typeTimer.start();
    }

    // ── Animated gem ─────────────────────────────────────────
    private class GemPanel extends JPanel {
        GemPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int cx = W/2, cy = H/2;
            float pulse = (float)(0.84 + 0.16 * Math.sin(tick * 1.7));
            int sz = (int)(Math.min(W, H) * 0.38 * pulse);
            // Slow spin
            double angle = tick * 0.25;
            double cos = Math.cos(angle), sin = Math.sin(angle);

            int[] xs = {
                    cx + (int)(0*cos - (-sz)*sin), cx + (int)(sz*cos - 0*sin),
                    cx + (int)(0*cos - sz*sin),    cx + (int)((-sz)*cos - 0*sin)
            };
            int[] ys = {
                    cy + (int)(0*sin + (-sz)*cos), cy + (int)(sz*sin + 0*cos),
                    cy + (int)(0*sin + sz*cos),    cy + (int)((-sz)*sin + 0*cos)
            };

            // Glow rings
            for (int ring = 9; ring >= 1; ring--) {
                g2.setColor(new Color(0xC8, 0xA0, 0x28, Math.min(255, 16*(10-ring))));
                g2.setStroke(new BasicStroke(ring * 2.2f));
                g2.drawPolygon(xs, ys, 4);
            }
            // Fill
            g2.setPaint(new GradientPaint(cx-sz, cy-sz, new Color(0xFF, 0xEE, 0x44), cx+sz, cy+sz, new Color(0xFF, 0x88, 0x00)));
            g2.fillPolygon(xs, ys, 4);
            // Shine
            g2.setColor(new Color(255, 255, 255, 110));
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(cx - sz/3, cy - sz/2, cx + sz/4, cy - sz/5);
            // Border
            g2.setColor(new Color(0xC8, 0xA0, 0x28));
            g2.setStroke(new BasicStroke(2f));
            g2.drawPolygon(xs, ys, 4);
            g2.dispose();
        }
    }

    // ── Animated background ───────────────────────────────────
    private class VictoryBgPanel extends JPanel {
        private final Image bg;
        private float time = 0f;

        VictoryBgPanel() {
            URL url = getClass().getResource(BG_PATH);
            bg = (url != null) ? new ImageIcon(url).getImage() : null;
            setOpaque(true); setBackground(Color.BLACK);
            new Timer(16, e -> { time += 0.018f; repaint(); }).start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int W = getWidth(), H = getHeight();

            if (bg != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                double scale = Math.max((double) W / bg.getWidth(null), (double) H / bg.getHeight(null));
                int dw = (int) (bg.getWidth(null) * scale);
                int dh = (int) (bg.getHeight(null) * scale);
                int dx = (W - dw) / 2;
                int dy = (H - dh) / 2;
                g2.drawImage(bg, dx, dy, dw, dh, null);
            }

            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, W, H);

            paintParticles(g2, W, H, 28, time, 0.04f, 2.5f, 200);
            paintParticles(g2, W, H, 14, time * 0.6f, 0.025f, 4f, 140);

            g2.setPaint(new RadialGradientPaint(
                    new Point(W/2, H/2), W * 0.72f,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0,0,0,0), new Color(0,0,0,110)}));
            g2.fillRect(0, 0, W, H);

            g2.dispose();
        }

        private void paintParticles(Graphics2D g2, int W, int H,
                                    int count, float t, float speed, float maxSz, int maxAlpha) {
            for (int i = 0; i < count; i++) {
                float phase = t + i * 0.45f;
                float x = (float)(W * (0.04 + 0.92 * ((Math.sin(phase * 0.58 + i) + 1) / 2)));
                float y = (float)(H * (1.0 - ((t * speed + i * 0.083f) % 1.0f)));
                float alpha = (float)(0.20 + 0.25 * Math.sin(phase * 1.2));
                float sz    = (float)(1.2 + maxSz * Math.abs(Math.sin(phase * 0.9)));

                // CRITICAL FIX: Math.max clamps the lowest possible alpha value to 0
                int finalAlpha = Math.max(0, Math.min(255, (int)(alpha * maxAlpha)));

                g2.setColor(new Color(0xC8, 0xA0, 0x28, finalAlpha));
                g2.fillOval((int)x, (int)y, (int)sz, (int)sz);
            }
        }
    }

    // ── Gold button ───────────────────────────────────────────
    private JButton makeGoldButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h = getModel().isRollover();
                g2.setPaint(new GradientPaint(0, 0,
                        h ? new Color(170, 110, 40) : new Color(120, 70, 20),
                        0, getHeight(),
                        h ? new Color(120, 80, 30) : new Color(80, 40, 10)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                if (h) {
                    g2.setColor(new Color(255, 215, 120, 85));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 16, 16);
                }
                g2.setColor(new Color(220, 180, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()-fm.stringWidth(getText()))/2;
                int ty = (getHeight()+fm.getAscent())/2 - 2;
                g2.setColor(Color.BLACK); g2.drawString(getText(), tx+1, ty+1);
                g2.setColor(new Color(255, 230, 170)); g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(270, 54));
        return btn;
    }
}