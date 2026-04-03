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
 * ArcadeTowerFrame — shown between arcade fights.
 *
 * Responsibilities:
 *   1. Display full tower roster top-to-bottom (boss at top, player at bottom).
 *   2. Show defeated enemies (faded + ✓), next opponent (highlighted + ▶),
 *      upcoming opponents (dim).
 *   3. "Face  [name]" button → BackstoryShowcase → NEW ArcadeModeBattleFrame.
 *
 * The "new ArcadeModeBattleFrame" part is the fix:
 * we never return to the old battle frame — we always spawn a fresh one.
 */
public class ArcadeTowerFrame extends JFrame {

    // ── Resources ─────────────────────────────────────────────
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

    // ── Colours ───────────────────────────────────────────────
    private static final Color GOLD       = new Color(0xC8, 0xA0, 0x28);
    private static final Color CREAM      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color PLAYER_CLR = new Color(0x2E, 0x8B, 0x57);
    private static final Color ENEMY_CLR  = new Color(0xB0, 0x2A, 0x2A);
    private static final Color BOSS_CLR   = new Color(0xAA, 0x00, 0xFF);
    private static final Color MUTED      = new Color(0xA0, 0x88, 0x50);
    private static final Color DEFEATED   = new Color(0x55, 0x55, 0x55);

    private final Character         player;
    private final ArcadeModeManager arcadeManager;

    // ══════════════════════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════════════════════
    public ArcadeTowerFrame(Character player, ArcadeModeManager manager) {
        this.player        = player;
        this.arcadeManager = manager;

        int defeated = arcadeManager.getCurrentIndex();
        int total    = arcadeManager.getTotalEnemies();

        setTitle("Arcade Mode — Tower Progress  (" + defeated + "/" + total + " defeated)");
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

    // ══════════════════════════════════════════════════════════
    //  UI construction
    // ══════════════════════════════════════════════════════════

    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        // Translucent dark veil for readability
        JPanel veil = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 155));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        veil.setOpaque(false);
        lp.add(veil, JLayeredPane.PALETTE_LAYER);

        JPanel content = buildContent();
        content.setOpaque(false);
        lp.add(content, JLayeredPane.MODAL_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                resize(lp, bg, veil, content);
            }
        });
        SwingUtilities.invokeLater(() -> resize(lp, bg, veil, content));
    }

    private void resize(JLayeredPane lp, JPanel bg, JPanel veil, JPanel content) {
        int w = lp.getWidth(), h = lp.getHeight();
        if (w == 0 || h == 0) return;
        bg.setBounds(0, 0, w, h);
        veil.setBounds(0, 0, w, h);
        content.setBounds(0, 0, w, h);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setOpaque(false);

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(26, 0, 10, 0));

        JLabel title = new JLabel("⚔  TOWER OF TRIALS  ⚔", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 34));
        title.setForeground(GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        int defeated = arcadeManager.getCurrentIndex();
        int total    = arcadeManager.getTotalEnemies();
        JLabel sub = new JLabel(
                defeated + " of " + total + " warriors defeated",
                SwingConstants.CENTER);
        sub.setFont(new Font("Serif", Font.ITALIC, 15));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(8));
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        // ── Tower list ────────────────────────────────────────
        JPanel towerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        towerWrap.setOpaque(false);
        towerWrap.add(buildTowerList());

        JScrollPane scroll = new JScrollPane(towerWrap,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        root.add(scroll, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 22, 0));

        Character nextEnemy = arcadeManager.getCurrentEnemy();
        String btnLabel = nextEnemy != null
                ? "⚔  Face  " + nextEnemy.getName() + "  →"
                : "⚔  All Enemies Defeated";
        JButton nextBtn = makeGoldButton(btnLabel);
        nextBtn.setEnabled(nextEnemy != null);
        if (nextEnemy != null) nextBtn.addActionListener(e -> onNextChallenge());
        footer.add(nextBtn);
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildTowerList() {
        List<Character> enemies   = arcadeManager.getEnemyQueue();
        int             nextIndex = arcadeManager.getCurrentIndex();

        JPanel tower = new JPanel();
        tower.setOpaque(false);
        tower.setLayout(new BoxLayout(tower, BoxLayout.Y_AXIS));
        tower.setBorder(BorderFactory.createEmptyBorder(8, 60, 8, 60));

        // Boss at top → enemy 1 at bottom (reversed display order)
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Character enemy      = enemies.get(i);
            boolean   isDefeated = (i < nextIndex);
            boolean   isNext     = (i == nextIndex);
            boolean   isBoss     = (i == enemies.size() - 1);

            tower.add(buildEnemyRow(enemy, i + 1, isDefeated, isNext, isBoss));
            if (i > 0) tower.add(buildConnector(isDefeated));
        }

        tower.add(buildConnector(false)); // connector to player row
        tower.add(buildPlayerRow());
        return tower;
    }

    // ── Enemy row ─────────────────────────────────────────────
    private JPanel buildEnemyRow(Character enemy, int pos,
                                 boolean isDefeated, boolean isNext, boolean isBoss) {
        Color accent = isBoss   ? BOSS_CLR
                : isDefeated ? DEFEATED
                : isNext   ? ENEMY_CLR
                : new Color(0x88, 0x55, 0x33);

        // Background: defeated = very dark, next = highlighted, upcoming = dim
        Color bg = isDefeated ? new Color(0x10, 0x10, 0x10, 150)
                : isNext     ? new Color(0x30, 0x08, 0x08, 200)
                : isBoss     ? new Color(0x28, 0x00, 0x30, 180)
                :              new Color(0x18, 0x10, 0x08, 160);

        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(700, 66));
        row.setPreferredSize(new Dimension(660, 66));

        // Border: defeated=dim, next=bright, boss=purple
        int borderAlpha = isDefeated ? 60 : isNext ? 220 : isBoss ? 190 : 90;
        row.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(
                        new Color(accent.getRed(), accent.getGreen(),
                                accent.getBlue(), borderAlpha),
                        isBoss ? 2 : 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        // Left: badge + portrait
        JLabel badge = new JLabel(
                isDefeated ? "✓" : isBoss ? "👑" : String.valueOf(pos),
                SwingConstants.CENTER);
        badge.setFont(new Font("Serif", Font.BOLD, isBoss ? 22 : isDefeated ? 14 : 14));
        badge.setForeground(accent);
        badge.setPreferredSize(new Dimension(28, 54));

        int fi = getFrameIndex(enemy.getName());
        Image img = (fi >= 0) ? loadImage(FRAME_IMGS[fi]) : null;
        JPanel portrait = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img == null) return;
                Graphics2D g2 = (Graphics2D) g.create();
                if (isDefeated)
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.30f));
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        };
        portrait.setOpaque(false);
        portrait.setPreferredSize(new Dimension(48, 50));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(badge);
        left.add(portrait);
        row.add(left, BorderLayout.WEST);

        // Centre: name + title + tag
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel nameLbl = new JLabel(enemy.getName());
        nameLbl.setFont(new Font("Serif", Font.BOLD, isBoss ? 15 : 13));
        nameLbl.setForeground(isDefeated ? DEFEATED : CREAM);

        JLabel titLbl = new JLabel(enemy.getTitle());
        titLbl.setFont(new Font("Serif", Font.ITALIC, 10));
        titLbl.setForeground(isDefeated ? DEFEATED : MUTED);

        String tag = isDefeated ? "Fallen"
                : isNext     ? "▶  Next Opponent"
                : isBoss     ? "⚡  Final Boss"
                :              "";
        JLabel tagLbl = new JLabel(tag);
        tagLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
        tagLbl.setForeground(isDefeated ? DEFEATED : accent);

        info.add(Box.createVerticalGlue());
        info.add(nameLbl);
        info.add(titLbl);
        if (!tag.isEmpty()) info.add(tagLbl);
        info.add(Box.createVerticalGlue());
        row.add(info, BorderLayout.CENTER);

        // Right: tier reward badge
        String reward = "";
        if (pos == ArcadeModeManager.HP_BOOST_AT) reward = "✨ +1000 HP";
        if (pos == ArcadeModeManager.ULTIMATE_AT)  reward = "🔥 Ultimate";
        if (isBoss)                                 reward = "🏆 Victory";

        if (!reward.isEmpty()) {
            JLabel rLbl = new JLabel(reward, SwingConstants.CENTER);
            rLbl.setFont(new Font("SansSerif", Font.BOLD, 9));
            rLbl.setForeground(isDefeated ? DEFEATED : GOLD);
            rLbl.setPreferredSize(new Dimension(82, 54));
            row.add(rLbl, BorderLayout.EAST);
        }

        return row;
    }

    private JPanel buildConnector(boolean isDefeated) {
        JPanel c = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(isDefeated
                        ? new Color(0x2E, 0x8B, 0x57, 140)
                        : new Color(0xC8, 0xA0, 0x28,  70));
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 0, new float[]{5, 4}, 0));
                g2.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setMaximumSize(new Dimension(700, 16));
        c.setPreferredSize(new Dimension(660, 16));
        return c;
    }

    private JPanel buildPlayerRow() {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x08, 0x20, 0x10, 200));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(700, 66));
        row.setPreferredSize(new Dimension(660, 66));
        row.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0x2E, 0x8B, 0x57, 210), 2),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));

        JLabel star = new JLabel("★", SwingConstants.CENTER);
        star.setFont(new Font("Serif", Font.BOLD, 22));
        star.setForeground(PLAYER_CLR);
        star.setPreferredSize(new Dimension(28, 54));

        int fi = getFrameIndex(player.getName());
        Image img = (fi >= 0) ? loadImage(FRAME_IMGS[fi]) : null;
        JPanel portrait = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img == null) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        };
        portrait.setOpaque(false);
        portrait.setPreferredSize(new Dimension(48, 50));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(star); left.add(portrait);
        row.add(left, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(player.getName());
        name.setFont(new Font("Serif", Font.BOLD, 14));
        name.setForeground(CREAM);

        JLabel tag = new JLabel("▶  YOU");
        tag.setFont(new Font("SansSerif", Font.BOLD, 9));
        tag.setForeground(PLAYER_CLR);

        JLabel hp = new JLabel("HP: " + player.getCurrentHP() + " / " + player.getMaxHP());
        hp.setFont(new Font("SansSerif", Font.PLAIN, 10));
        hp.setForeground(PLAYER_CLR);

        info.add(Box.createVerticalGlue());
        info.add(name); info.add(tag); info.add(hp);
        info.add(Box.createVerticalGlue());
        row.add(info, BorderLayout.CENTER);

        return row;
    }

    // ══════════════════════════════════════════════════════════
    //  Core transition — the heart of the fix
    // ══════════════════════════════════════════════════════════

    /**
     * Player clicked "Face [enemy]".
     * dispose() this frame → BackstoryShowcase (enemy lore) → FRESH ArcadeModeBattleFrame.
     *
     * Because we always construct a new ArcadeModeBattleFrame, its
     * processingTurn field is initialised to false and its TurnManager
     * starts with zero cooldowns. The reuse bug cannot recur.
     */
    private void onNextChallenge() {
        Character nextEnemy = arcadeManager.getCurrentEnemy();
        if (nextEnemy == null) return;
        dispose();
        new BackstoryShowcase(
                CharacterStories.getEnemyStory(nextEnemy),
                CharacterStories.getEnemyTitle(nextEnemy),
                () -> SwingUtilities.invokeLater(
                        () -> new ArcadeModeBattleFrame(player, arcadeManager)));
    }

    // ══════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════

    private int getFrameIndex(String name) {
        for (int i = 0; i < ALL_NAMES.length; i++)
            if (ALL_NAMES[i].equals(name)) return i;
        return -1;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    private JButton makeGoldButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
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
        btn.setPreferredSize(new Dimension(300, 54));
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