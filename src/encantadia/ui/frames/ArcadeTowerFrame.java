package encantadia.ui.frames;

import encantadia.BackstoryShowcase;
import encantadia.ScreenManager;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.characters.Character;
import encantadia.story.CharacterStories;
import encantadia.ui.frames.battleModeFrames.ArcadeModeBattleFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.List;

/**
 * Shows the arcade tower lineup before the first battle.
 * Final boss at top, player at the bottom, enemies stacked in order.
 * Reuses character frame images from CharacterSelectionFrame.
 */
public class ArcadeTowerFrame extends JFrame {

    private static final String BG_PATH = "/resources/background (4).png";

    private static final String[] FRAME_IMGS = {
            "/resources/tyroneFrame (1).png",
            "/resources/elanFrame (1).png",
            "/resources/claireFrame (1).png",
            "/resources/dirkFrame (1).png",
            "/resources/flamaraFrame (1).png",
            "/resources/deaFrame (1).png",
            "/resources/adamusFrame (1).png",
            "/resources/teraFrame (1).png"
    };
    private static final String[] ALL_NAMES = {
            "Tyrone", "Elan", "Claire", "Dirk", "Flamara", "Dea", "Adamus", "Tera"
    };

    private static final Color GOLD       = new Color(0xC8, 0xA0, 0x28);
    private static final Color CREAM      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color PLAYER_CLR = new Color(0x2E, 0x8B, 0x57);
    private static final Color ENEMY_CLR  = new Color(0xB0, 0x2A, 0x2A);
    private static final Color BOSS_CLR   = new Color(0xAA, 0x00, 0xFF);
    private static final Color MUTED      = new Color(0xA0, 0x88, 0x50);

    private final Character         player;
    private final ArcadeModeManager arcadeManager;

    public ArcadeTowerFrame(Character player) {
        this.player        = player;
        this.arcadeManager = new ArcadeModeManager(player);

        setTitle("Arcade Mode — The Tower of Trials");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        setVisible(true);
        ScreenManager.register(this);
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        // Semi-transparent dark overlay for readability
        JPanel overlay = new JPanel();
        overlay.setOpaque(true);
        overlay.setBackground(new Color(0, 0, 0, 140));
        lp.add(overlay, JLayeredPane.PALETTE_LAYER);

        JPanel content = buildContent();
        lp.add(content, JLayeredPane.MODAL_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = lp.getWidth(), h = lp.getHeight();
                bg.setBounds(0, 0, w, h);
                overlay.setBounds(0, 0, w, h);
                content.setBounds(0, 0, w, h);
            }
        });
        SwingUtilities.invokeLater(() -> {
            int w = lp.getWidth(), h = lp.getHeight();
            bg.setBounds(0, 0, w, h);
            overlay.setBounds(0, 0, w, h);
            content.setBounds(0, 0, w, h);
        });
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setOpaque(false);

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(24, 0, 12, 0));

        JLabel titleLbl = new JLabel("⚔  THE TOWER OF TRIALS  ⚔", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 34));
        titleLbl.setForeground(GOLD);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Defeat all " + arcadeManager.getTotalEnemies() + " warriors to claim victory",
                SwingConstants.CENTER);
        subLbl.setFont(new Font("Serif", Font.ITALIC, 14));
        subLbl.setForeground(MUTED);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(titleLbl);
        header.add(Box.createVerticalStrut(6));
        header.add(subLbl);
        root.add(header, BorderLayout.NORTH);

        // ── Tower (scrollable) ────────────────────────────────
        JPanel tower = buildTower();
        JScrollPane scroll = new JScrollPane(tower,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setOpaque(false);
        root.add(scroll, BorderLayout.CENTER);

        // ── Begin button ──────────────────────────────────────
        JButton beginBtn = makeGoldButton("⚔  Begin  ⚔");
        beginBtn.addActionListener(e -> onBegin());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 20, 0));
        footer.add(beginBtn);
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildTower() {
        List<Character> enemies = arcadeManager.getEnemyQueue();

        JPanel tower = new JPanel();
        tower.setOpaque(false);
        tower.setLayout(new BoxLayout(tower, BoxLayout.Y_AXIS));
        tower.setBorder(BorderFactory.createEmptyBorder(8, 60, 8, 60));

        // Add enemies top-to-bottom: boss at top, enemy 1 at bottom
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Character enemy  = enemies.get(i);
            boolean   isBoss = (i == enemies.size() - 1);
            boolean   isNext = (i == 0);
            tower.add(buildEnemyRow(enemy, i + 1, isBoss, isNext));
            tower.add(Box.createVerticalStrut(5));
        }

        // Connector line
        tower.add(buildConnector());

        // Player at the very bottom
        tower.add(buildPlayerRow());

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(tower);
        return wrapper;
    }

    private JPanel buildEnemyRow(Character enemy, int pos, boolean isBoss, boolean isNext) {
        Color rowColor = isBoss ? BOSS_CLR : ENEMY_CLR;
        Color bgColor  = isBoss
                ? new Color(0x2A, 0x08, 0x2A, 170)
                : isNext
                ? new Color(0x28, 0x08, 0x08, 150)
                : new Color(0x12, 0x0E, 0x08, 130);

        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(true);
        row.setBackground(bgColor);
        row.setMaximumSize(new Dimension(700, 68));
        row.setPreferredSize(new Dimension(660, 68));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(
                        rowColor.getRed(), rowColor.getGreen(), rowColor.getBlue(),
                        isBoss ? 200 : isNext ? 160 : 80), isBoss ? 2 : 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));

        // Position badge
        JLabel badge = new JLabel(isBoss ? "👑" : String.valueOf(pos), SwingConstants.CENTER);
        badge.setFont(new Font("Serif", Font.BOLD, isBoss ? 22 : 15));
        badge.setForeground(rowColor);
        badge.setPreferredSize(new Dimension(28, 56));
        row.add(badge, BorderLayout.WEST);

        // Portrait
        int fi = getFrameIndex(enemy.getName());
        Image img = fi >= 0 ? loadImage(FRAME_IMGS[fi]) : null;
        JPanel portrait = new JPanel() {
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
        };
        portrait.setOpaque(false);
        portrait.setPreferredSize(new Dimension(54, 54));
        row.add(portrait, BorderLayout.LINE_START);

        // Need a small panel to hold portrait next to badge
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(badge);
        left.add(portrait);
        row.remove(badge); // re-add via left panel
        row.add(left, BorderLayout.WEST);

        // Info
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(enemy.getName());
        nameLbl.setFont(new Font("Serif", Font.BOLD, isBoss ? 16 : 13));
        nameLbl.setForeground(CREAM);

        JLabel titleLbl = new JLabel(enemy.getTitle());
        titleLbl.setFont(new Font("Serif", Font.ITALIC, 10));
        titleLbl.setForeground(MUTED);

        String tagText = isBoss ? "⚡  FINAL BOSS" : isNext ? "▶  NEXT OPPONENT" : "";
        JLabel tagLbl = new JLabel(tagText);
        tagLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
        tagLbl.setForeground(rowColor);

        info.add(Box.createVerticalGlue());
        info.add(nameLbl);
        info.add(titleLbl);
        if (!tagText.isEmpty()) info.add(tagLbl);
        info.add(Box.createVerticalGlue());
        row.add(info, BorderLayout.CENTER);

        // Tier reward badge (right side)
        String rewardText = "";
        if (pos == ArcadeModeManager.HP_BOOST_AT)  rewardText = "✨ +1000 HP";
        if (pos == ArcadeModeManager.ULTIMATE_AT)   rewardText = "🔥 Ultimate";
        if (isBoss)                                  rewardText = "🏆 Victory";

        if (!rewardText.isEmpty()) {
            JLabel rewardLbl = new JLabel(rewardText, SwingConstants.CENTER);
            rewardLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
            rewardLbl.setForeground(GOLD);
            rewardLbl.setPreferredSize(new Dimension(80, 56));
            row.add(rewardLbl, BorderLayout.EAST);
        }

        return row;
    }

    private JPanel buildConnector() {
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0xC8, 0xA0, 0x28, 120));
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{6, 4}, 0));
                g2.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setMaximumSize(new Dimension(700, 18));
        c.setPreferredSize(new Dimension(660, 18));
        return c;
    }

    private JPanel buildPlayerRow() {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(true);
        row.setBackground(new Color(0x08, 0x20, 0x10, 180));
        row.setMaximumSize(new Dimension(700, 68));
        row.setPreferredSize(new Dimension(660, 68));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2E, 0x8B, 0x57, 200), 2),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));

        int fi = getFrameIndex(player.getName());
        Image img = fi >= 0 ? loadImage(FRAME_IMGS[fi]) : null;

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JLabel star = new JLabel("★", SwingConstants.CENTER);
        star.setFont(new Font("Serif", Font.BOLD, 22));
        star.setForeground(PLAYER_CLR);
        star.setPreferredSize(new Dimension(28, 56));

        JPanel portrait = new JPanel() {
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
        };
        portrait.setOpaque(false);
        portrait.setPreferredSize(new Dimension(54, 54));

        left.add(star);
        left.add(portrait);
        row.add(left, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(player.getName());
        nameLbl.setFont(new Font("Serif", Font.BOLD, 15));
        nameLbl.setForeground(CREAM);

        JLabel tagLbl = new JLabel("▶  YOU");
        tagLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
        tagLbl.setForeground(PLAYER_CLR);

        info.add(Box.createVerticalGlue());
        info.add(nameLbl);
        info.add(tagLbl);
        info.add(Box.createVerticalGlue());
        row.add(info, BorderLayout.CENTER);

        return row;
    }

    private void onBegin() {
        Character firstEnemy = arcadeManager.getCurrentEnemy();
        if (firstEnemy == null) return;

        Runnable launchBattle = () -> new ArcadeModeBattleFrame(player, arcadeManager);
        dispose();
        new BackstoryShowcase(
                CharacterStories.getEnemyStory(firstEnemy),
                CharacterStories.getEnemyTitle(firstEnemy),
                launchBattle);
    }

    private int getFrameIndex(String name) {
        for (int i = 0; i < ALL_NAMES.length; i++)
            if (ALL_NAMES[i].equals(name)) return i;
        return -1;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        return url != null ? new ImageIcon(url).getImage() : null;
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
                g2.setFont(new Font("Serif", Font.BOLD, 15));
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
        btn.setPreferredSize(new Dimension(220, 52));
        return btn;
    }

    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
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
}