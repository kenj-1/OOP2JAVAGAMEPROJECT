package encantadia.ui.frames;

import encantadia.ScreenManager;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.characters.Character;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class ArcadeVictoryFrame extends JFrame {

    private static final String BG_PATH = "/resources/background (4).png";

    private final Character winner;
    private Timer           animTimer;
    private float           tick = 0;

    public ArcadeVictoryFrame(Character winner) {
        this.winner = winner;
        ArcadeModeManager.setArcadeCompleted(true);

        setTitle("Encantadia — Victory!");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        setVisible(true);
        ScreenManager.register(this);
    }

    @Override
    public void dispose() {
        if (animTimer != null) animTimer.stop();
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        VictoryBgPanel bg = new VictoryBgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        JPanel content = buildContent();
        lp.add(content, JLayeredPane.PALETTE_LAYER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                bg.setBounds(0, 0, getWidth(), getHeight());
                content.setBounds(0, 0, getWidth(), getHeight());
            }
        });
        SwingUtilities.invokeLater(() -> {
            bg.setBounds(0, 0, getWidth(), getHeight());
            content.setBounds(0, 0, getWidth(), getHeight());
        });

        animTimer = new Timer(16, e -> { tick += 0.05f; lp.repaint(); });
        animTimer.start();
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(Box.createVerticalGlue());

        // Animated golden title
        JLabel victoryLbl = new JLabel("✦  VICTORY  ✦", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight();
                int fs = Math.min(72, W / 8);
                g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, fs));
                FontMetrics fm = g2.getFontMetrics();
                String txt = getText();
                int tx = (W - fm.stringWidth(txt)) / 2;
                int ty = (H + fm.getAscent() - fm.getDescent()) / 2;

                float pulse = (float)(0.7 + 0.3 * Math.sin(tick));
                for (int sp = 12; sp >= 1; sp--) {
                    g2.setColor(new Color(0xC8, 0xA0, 0x28, Math.min(255, (int)(40f / sp))));
                    g2.drawString(txt, tx - sp/2, ty);
                    g2.drawString(txt, tx + sp/2, ty);
                }
                g2.setColor(new Color(0, 0, 0, 160));
                g2.drawString(txt, tx+2, ty+2);
                int r = 0xFF, gv = (int)(0xCC * pulse), b = 0x20;
                g2.setColor(new Color(r, Math.max(0, Math.min(255, gv)), b));
                g2.drawString(txt, tx, ty);
                g2.dispose();
            }
        };
        victoryLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        victoryLbl.setMaximumSize(new Dimension(900, 110));
        victoryLbl.setPreferredSize(new Dimension(900, 110));

        // Gem trophy
        GemPanel gem = new GemPanel();
        gem.setAlignmentX(Component.CENTER_ALIGNMENT);
        gem.setMaximumSize(new Dimension(130, 130));
        gem.setPreferredSize(new Dimension(130, 130));

        JLabel winnerLbl = new JLabel(winner.getName() + " has conquered Arcade Mode!", SwingConstants.CENTER);
        winnerLbl.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 22));
        winnerLbl.setForeground(new Color(0xFF, 0xF5, 0xDC));
        winnerLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("The gem rests in your hands. The Void has been silenced.", SwingConstants.CENTER);
        subLbl.setFont(new Font("Serif", Font.ITALIC, 15));
        subLbl.setForeground(new Color(0xA0, 0x88, 0x50));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton returnBtn = makeGoldButton("Return to Main Menu");
        returnBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnBtn.addActionListener(e -> { dispose(); new MainMenuFrame(); });

        panel.add(victoryLbl);
        panel.add(Box.createVerticalStrut(16));
        panel.add(gem);
        panel.add(Box.createVerticalStrut(22));
        panel.add(winnerLbl);
        panel.add(Box.createVerticalStrut(10));
        panel.add(subLbl);
        panel.add(Box.createVerticalStrut(36));
        panel.add(returnBtn);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private class GemPanel extends JPanel {
        GemPanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int W = getWidth(), H = getHeight();
            int cx = W / 2, cy = H / 2;
            float pulse = (float)(0.85 + 0.15 * Math.sin(tick * 1.5));
            int sz = (int)(Math.min(W, H) * 0.44 * pulse);

            int[] xs = { cx, cx + sz, cx, cx - sz };
            int[] ys = { cy - sz, cy, cy + sz, cy };

            // Outer glow
            for (int ring = 8; ring >= 1; ring--) {
                g2.setColor(new Color(0xC8, 0xA0, 0x28, Math.min(255, 18 * (9 - ring))));
                g2.setStroke(new BasicStroke(ring * 2.5f));
                g2.drawPolygon(xs, ys, 4);
            }

            // Fill
            g2.setPaint(new GradientPaint(cx - sz, cy - sz, new Color(0xFF, 0xEE, 0x44),
                    cx + sz, cy + sz, new Color(0xFF, 0x88, 0x00)));
            g2.fillPolygon(xs, ys, 4);

            // Inner highlight
            g2.setColor(new Color(255, 255, 255, 110));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(cx - sz/3, cy - sz/2, cx + sz/4, cy - sz/5);

            // Border
            g2.setColor(new Color(0xC8, 0xA0, 0x28));
            g2.setStroke(new BasicStroke(2));
            g2.drawPolygon(xs, ys, 4);

            g2.dispose();
        }
    }

    private class VictoryBgPanel extends JPanel {
        private final Image img;
        private float time = 0;

        VictoryBgPanel(String path) {
            URL url = getClass().getResource(path);
            img = url != null ? new ImageIcon(url).getImage() : null;
            setOpaque(true); setBackground(Color.BLACK);
            new Timer(16, e -> { time += 0.018f; repaint(); }).start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int W = getWidth(), H = getHeight();
            if (img != null) g2.drawImage(img, 0, 0, W, H, null);

            // Dark dramatic overlay
            g2.setColor(new Color(0, 0, 0, 130));
            g2.fillRect(0, 0, W, H);

            // Rising golden particles
            for (int i = 0; i < 28; i++) {
                float phase = time + i * 0.45f;
                float x = (float)(W * (0.05 + 0.9 * ((Math.sin(phase * 0.6 + i) + 1) / 2)));
                float y = (float)(H * (1.0 - ((time * 0.04f + i * 0.085f) % 1.0f)));
                float alpha = (float)(0.25 + 0.25 * Math.sin(phase * 1.1));
                float sz    = (float)(1.5 + 3 * Math.abs(Math.sin(phase * 0.9)));
                g2.setColor(new Color(0xC8, 0xA0, 0x28, Math.min(255, (int)(alpha * 255))));
                g2.fillOval((int)x, (int)y, (int)sz, (int)sz);
            }
            g2.dispose();
        }
    }

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
                    g2.setColor(new Color(255, 215, 120, 80));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 16, 16);
                }
                g2.setColor(new Color(220, 180, 90));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                g2.setColor(Color.BLACK); g2.drawString(getText(), tx+1, ty+1);
                g2.setColor(new Color(255, 230, 170)); g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(260, 56));
        return btn;
    }
}