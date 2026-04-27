package encantadia.ui.frames;

import encantadia.BackstoryShowcase;
import encantadia.ScreenManager;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.characters.Character;
import encantadia.characters.animation.CharacterAnimator;
import encantadia.story.CharacterStories;
import encantadia.ui.frames.battleModeFrames.ArcadeModeBattleFrame;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.List;

/**
 * ArcadeTowerFrame — shown between arcade fights.
 *
 * MK1 REDESIGN: Player stands outside the tower on the left.
 * The tower sits on the right, filled with the live idle animations
 * of the entire enemy roster staring back at the player.
 */
public class ArcadeTowerFrame extends JFrame {

    private static final String BG_PATH = "/resources/background (4).png";

    private static final String[] FRAME_IMGS = {
            "/resources/tyroneFrame (1).png", "/resources/elanFrame (1).png",
            "/resources/claireFrame (1).png", "/resources/dirkFrame (1).png",
            "/resources/flamaraFrame (1).png", "/resources/deaFrame (1).png",
            "/resources/adamusFrame (1).png", "/resources/teraFrame (1).png"
    };
    private static final String[] ALL_NAMES = {
            "Tyrone", "Makelan Shere", "Claire", "Dirk", "Flamara", "Dea", "Adamus", "Tera"
    };

    private static final Color GOLD       = new Color(0xC8, 0xA0, 0x28);
    private static final Color CREAM      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color PLAYER_CLR = new Color(0x2E, 0x8B, 0x57);
    private static final Color ENEMY_CLR  = new Color(0xB0, 0x2A, 0x2A);
    private static final Color BOSS_CLR   = new Color(0xAA, 0x00, 0xFF);
    private static final Color MUTED      = new Color(0xA0, 0x88, 0x50);
    private static final Color DEFEATED   = new Color(0x55, 0x55, 0x55);

    private final Character         player;
    private final Character         currentEnemy;
    private final ArcadeModeManager arcadeManager;

    // Animation State
    private CharacterAnimator   playerAnimator;
    private CharacterAnimator[] towerAnimators;
    private Timer               animTimer;
    private float               glowTick = 0f;
    private float               bgYOffset = -250f;

    // UI Layout Refs
    private JPanel      playerPanel;
    private JPanel      vsPanel;
    private JScrollPane towerScroll;
    private JPanel      currentTargetRowRef;
    private JPanel      footer;

    public ArcadeTowerFrame(Character player, ArcadeModeManager manager) {
        this.player        = player;
        this.arcadeManager = manager;
        this.currentEnemy  = manager.getCurrentEnemy();

        int defeated = arcadeManager.getCurrentIndex();
        int total    = arcadeManager.getTotalEnemies();

        setTitle("Arcade Mode — Tower Progress (" + defeated + "/" + total + " defeated)");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initAnimators();
        buildUI(defeated, total);

        setVisible(true);
        ScreenManager.register(this);
    }

    /**
     * Safely load the animated GIFs for the Player and EVERY enemy in the tower.
     */
    private void initAnimators() {
        this.playerAnimator = CharacterAnimator.forCharacter(player);

        List<Character> enemies = arcadeManager.getEnemyQueue();
        towerAnimators = new CharacterAnimator[enemies.size()];
        for (int i = 0; i < enemies.size(); i++) {
            towerAnimators[i] = CharacterAnimator.forCharacter(enemies.get(i));
        }
    }

    @Override
    public void dispose() {
        if (animTimer != null) animTimer.stop();
        if (playerAnimator != null) playerAnimator.dispose();
        if (towerAnimators != null) {
            for (CharacterAnimator a : towerAnimators) {
                if (a != null) a.dispose();
            }
        }
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void buildUI(int defeated, int total) {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        // 1. Background
        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        JPanel veil = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        veil.setOpaque(false);
        lp.add(veil, JLayeredPane.PALETTE_LAYER);

        // 2. Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("⚔  TOWER OF TRIALS  ⚔", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 34));
        title.setForeground(GOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel sub = new JLabel(defeated + " of " + total + " warriors defeated", SwingConstants.CENTER);
        sub.setFont(new Font("Serif", Font.ITALIC, 15));
        sub.setForeground(MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title); header.add(Box.createVerticalStrut(6)); header.add(sub);
        lp.add(header, JLayeredPane.MODAL_LAYER);

        // 3. Player Side (Left)
        playerPanel = new PlayerSidePanel();
        lp.add(playerPanel, JLayeredPane.MODAL_LAYER);

        // 4. VS Badge (Center)
        vsPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, Math.min(50, w)));
                FontMetrics fm = g2.getFontMetrics();
                String vs = "VS";
                int tx = (w - fm.stringWidth(vs)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;

                float pulse = (float)(0.6 + 0.4 * Math.abs(Math.sin(glowTick * 2.0)));
                g2.setColor(new Color(255, 200, 50, (int)(150 * pulse)));
                g2.fillOval(tx - 20, ty - fm.getAscent() - 10, fm.stringWidth(vs) + 40, fm.getHeight() + 20);

                g2.setColor(new Color(0,0,0,200)); g2.drawString(vs, tx+3, ty+3);
                g2.setColor(CREAM); g2.drawString(vs, tx, ty);
                g2.dispose();
            }
        };
        vsPanel.setOpaque(false);
        lp.add(vsPanel, JLayeredPane.POPUP_LAYER);

        // 5. Tower Side (Right)
        JPanel towerWrap = new JPanel(new GridBagLayout());
        towerWrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        towerWrap.add(buildTowerList(), gbc);

        towerScroll = new JScrollPane(towerWrap, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        towerScroll.setOpaque(false); towerScroll.getViewport().setOpaque(false); towerScroll.setBorder(null);
        towerScroll.getVerticalScrollBar().setOpaque(false);
        towerScroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        towerScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { this.thumbColor = new Color(0xC8, 0xA0, 0x28, 140); this.trackColor = new Color(0, 0, 0, 0); }
            @Override protected JButton createDecreaseButton(int o) { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected JButton createIncreaseButton(int o) { JButton b=new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {}
            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty() || !scrollbar.isEnabled()) return;
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor); g2.fillRoundRect(r.x, r.y, r.width, r.height, 6, 6); g2.dispose();
            }
        });
        lp.add(towerScroll, JLayeredPane.MODAL_LAYER);

        // 6. Footer (Button)
        footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footer.setOpaque(false);
        String btnLabel = currentEnemy != null ? "⚔  Face  " + currentEnemy.getName() + "  →" : "⚔  Tower Conquered";
        JButton nextBtn = makeGoldButton(btnLabel);
        nextBtn.setEnabled(currentEnemy != null);
        if (currentEnemy != null) nextBtn.addActionListener(e -> onNextChallenge());
        footer.add(nextBtn);
        lp.add(footer, JLayeredPane.POPUP_LAYER);

        // ── Responsive Absolute Positioning ──
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int w = lp.getWidth(), h = lp.getHeight();
                if (w == 0 || h == 0) return;
                bg.setBounds(0, 0, w, h);
                veil.setBounds(0, 0, w, h);

                header.setBounds(0, 0, w, 100);
                footer.setBounds(0, h - 80, w, 80);

                // Left: 5% to 40%
                int pX = (int)(w * 0.05), pY = 100, pW = (int)(w * 0.35), pH = h - 180;
                playerPanel.setBounds(pX, pY, pW, pH);

                // Right: 55% to 95%
                int tX = (int)(w * 0.50), tY = 100, tW = (int)(w * 0.45), tH = h - 180;
                towerScroll.setBounds(tX, tY, tW, tH);

                // VS Center
                vsPanel.setBounds((int)(w * 0.40), (h - 100) / 2, (int)(w * 0.10), 100);

                lp.revalidate(); lp.repaint();
            }
        });

        SwingUtilities.invokeLater(() -> {
            int w = lp.getWidth(), h = lp.getHeight();
            bg.setBounds(0, 0, w, h); veil.setBounds(0, 0, w, h);
            header.setBounds(0, 0, w, 100); footer.setBounds(0, h - 80, w, 80);
            playerPanel.setBounds((int)(w * 0.05), 100, (int)(w * 0.35), h - 180);
            towerScroll.setBounds((int)(w * 0.50), 100, (int)(w * 0.45), h - 180);
            vsPanel.setBounds((int)(w * 0.40), (h - 100) / 2, (int)(w * 0.10), 100);

            // Auto-scroll to Target
            Timer scrollDelay = new Timer(100, e -> {
                if (currentTargetRowRef != null && towerScroll != null) {
                    int y = currentTargetRowRef.getY();
                    Container parent = currentTargetRowRef.getParent();
                    if (parent != null) y += parent.getY();
                    int viewHeight = towerScroll.getViewport().getHeight();
                    int scrollPos = y - (viewHeight / 2) + (currentTargetRowRef.getHeight() / 2);
                    towerScroll.getVerticalScrollBar().setValue(Math.max(0, scrollPos));
                }
            });
            scrollDelay.setRepeats(false); scrollDelay.start();
        });

        // "Climbing" Effect + Global Tick Timer
        animTimer = new Timer(20, e -> {
            glowTick += 0.1f;
            if (bgYOffset < 0) {
                bgYOffset += 3.5f;
                if (bgYOffset > 0) bgYOffset = 0f;
            }
            lp.repaint(); // Always repaint so GIFs advance their frames natively
        });
        animTimer.start();
    }

    private JPanel buildTowerList() {
        List<Character> enemies   = arcadeManager.getEnemyQueue();
        int             nextIndex = arcadeManager.getCurrentIndex();

        JPanel tower = new JPanel();
        tower.setOpaque(false);
        tower.setLayout(new BoxLayout(tower, BoxLayout.Y_AXIS));
        tower.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Build from Top (Boss) down to Bottom (Enemy 1)
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Character enemy      = enemies.get(i);
            boolean   isDefeated = (i < nextIndex);
            boolean   isNext     = (i == nextIndex);
            boolean   isBoss     = (i == enemies.size() - 1);
            CharacterAnimator anim = towerAnimators[i];

            JPanel row = buildEnemyRow(enemy, anim, i + 1, isDefeated, isNext, isBoss);
            tower.add(row);
            if (isNext) currentTargetRowRef = row;

            if (i > 0) tower.add(Box.createVerticalStrut(12));
        }

        return tower;
    }

    // ── Individual Tower Row (Enemy Box) ──
    private JPanel buildEnemyRow(Character enemy, CharacterAnimator animator, int pos, boolean isDefeated, boolean isNext, boolean isBoss) {
        Color accent = isBoss ? BOSS_CLR : isDefeated ? DEFEATED : isNext ? ENEMY_CLR : new Color(0x88, 0x55, 0x33);
        Color bg = isDefeated ? new Color(0x10, 0x10, 0x10, 160) : isNext ? new Color(0x30, 0x08, 0x08, 220) : isBoss ? new Color(0x28, 0x00, 0x30, 190) : new Color(0x18, 0x10, 0x08, 170);

        JPanel row = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                // Box
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 12, 12);

                if (isNext) {
                    float pulse = (float)(0.6 + 0.4 * Math.abs(Math.sin(glowTick * 1.5)));
                    g2.setColor(new Color(ENEMY_CLR.getRed(), ENEMY_CLR.getGreen(), ENEMY_CLR.getBlue(), (int)(200 * pulse)));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, w-3, h-3, 12, 12);
                } else {
                    int borderAlpha = isDefeated ? 60 : isBoss ? 190 : 90;
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), borderAlpha));
                    g2.setStroke(new BasicStroke(isBoss ? 2f : 1f));
                    g2.drawRoundRect(0, 0, w-1, h-1, 12, 12);
                }

                // Floor Badge (Left)
                g2.setColor(accent);
                g2.fillRect(0, 0, 36, h);
                g2.setColor(isDefeated ? new Color(20,20,20) : CREAM);
                g2.setFont(new Font("Serif", Font.BOLD, isBoss ? 20 : 18));
                String badgeTxt = isDefeated ? "✓" : isBoss ? "👑" : String.valueOf(pos);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(badgeTxt, (36 - fm.stringWidth(badgeTxt))/2, (h + fm.getAscent() - fm.getDescent())/2);

                // Sprite Space (Left-Middle)
                int spriteSize = h - 10;
                int spriteX = 46;
                int spriteY = 5;

                g2.setColor(new Color(0,0,0, 120));
                g2.fillRoundRect(spriteX, spriteY, spriteSize, spriteSize, 8, 8);

                if (animator != null) {
                    // MIRROR THE ENEMY ANIMATION SO IT FACES LEFT!
                    AffineTransform oldTransform = g2.getTransform();
                    int cx = spriteX + spriteSize / 2;
                    g2.translate(cx, 0); g2.scale(-1, 1); g2.translate(-cx, 0);

                    Shape savedClip = g2.getClip();
                    g2.setClip(spriteX, spriteY, spriteSize, spriteSize);
                    animator.draw(g2, spriteX, spriteY, spriteSize, spriteSize, this);
                    g2.setClip(savedClip);

                    g2.setTransform(oldTransform);
                } else {
                    // Fallback to static portrait
                    int fi = getFrameIndex(enemy.getName());
                    Image img = (fi >= 0) ? loadImage(FRAME_IMGS[fi]) : null;
                    if (img != null) drawImageProportional(g2, img, spriteX, spriteY, spriteSize, spriteSize);
                }

                if (isDefeated) {
                    g2.setColor(new Color(0,0,0,150));
                    g2.fillRoundRect(spriteX, spriteY, spriteSize, spriteSize, 8, 8);
                }

                g2.setColor(accent); g2.drawRoundRect(spriteX, spriteY, spriteSize, spriteSize, 8, 8);

                // Details (Right-Middle)
                int textX = spriteX + spriteSize + 15;
                int textY = h / 2 - 8;

                g2.setFont(new Font("Serif", Font.BOLD, isBoss ? 18 : 16));
                g2.setColor(isDefeated ? DEFEATED : CREAM);
                g2.drawString(enemy.getName(), textX, textY);

                g2.setFont(new Font("Serif", Font.ITALIC, 11));
                g2.setColor(isDefeated ? DEFEATED : MUTED);
                g2.drawString(enemy.getTitle(), textX, textY + 16);

                String tag = isDefeated ? "Fallen" : isNext ? "▶ NEXT TARGET" : isBoss ? "⚡ Final Boss" : "";
                if (!tag.isEmpty()) {
                    g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                    if (isNext) {
                        float pulse = (float)(0.5 + 0.5 * Math.abs(Math.sin(glowTick * 2)));
                        g2.setColor(new Color(ENEMY_CLR.getRed(), ENEMY_CLR.getGreen(), ENEMY_CLR.getBlue(), (int)(255 * pulse)));
                    } else {
                        g2.setColor(isDefeated ? DEFEATED : accent);
                    }
                    g2.drawString(tag, textX, textY + 30);
                }

                g2.dispose();
            }
        };
        row.setPreferredSize(new Dimension(460, 110));
        row.setMaximumSize(new Dimension(600, 110));
        return row;
    }

    // ── Player Panel (Left Side MK1 Style) ──
    private class PlayerSidePanel extends JPanel {
        PlayerSidePanel() { setOpaque(false); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int W = getWidth(), H = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            float pulse = (float)(0.8 + 0.2 * Math.abs(Math.sin(glowTick * 1.2)));

            // Cinematic Backdrop
            GradientPaint grad = new GradientPaint(0, 0, new Color(10, 30, 20, 200), W, H, new Color(0, 10, 5, 230));
            g2.setPaint(grad);
            g2.fillRoundRect(0, 0, W, H, 20, 20);

            g2.setColor(new Color(PLAYER_CLR.getRed(), PLAYER_CLR.getGreen(), PLAYER_CLR.getBlue(), (int)(180 * pulse)));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(1, 1, W-3, H-3, 20, 20);

            // Sprite Rendering
            int spriteSize = (int) (Math.min(W, H) * 0.65);
            int sx = (W - spriteSize) / 2;
            int sy = (H - spriteSize) / 2 - 20;

            // Ground Shadow
            g2.setColor(new Color(0,0,0,120));
            g2.fillOval(sx + spriteSize/4, sy + spriteSize - 20, spriteSize/2, 40);

            if (playerAnimator != null) {
                Shape saved = g2.getClip();
                // Bound it so wide skills don't bleed out of the panel entirely
                g2.setClip(0, 0, W, H);
                playerAnimator.draw(g2, sx, sy, spriteSize, spriteSize, this);
                g2.setClip(saved);
            } else {
                int fi = getFrameIndex(player.getName());
                Image img = (fi >= 0) ? loadImage(FRAME_IMGS[fi]) : null;
                if (img != null) drawImageProportional(g2, img, sx, sy, spriteSize, spriteSize);
            }

            // Information Plate at the bottom
            int plateY = H - 100;
            g2.setColor(new Color(0,0,0,180));
            g2.fillRect(0, plateY, W, 100);
            g2.setColor(PLAYER_CLR);
            g2.fillRect(0, plateY, W, 4);

            g2.setFont(new Font("Serif", Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            int nameX = (W - fm.stringWidth(player.getName())) / 2;
            g2.setColor(CREAM); g2.drawString(player.getName(), nameX, plateY + 35);

            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            fm = g2.getFontMetrics();
            String tag = "▲ ASCENDING";
            int tagX = (W - fm.stringWidth(tag)) / 2;
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), (int)(255 * pulse)));
            g2.drawString(tag, tagX, plateY + 55);

            String hp = "HP: " + player.getMaxHP();
            int hpX = (W - fm.stringWidth(hp)) / 2;
            g2.setColor(PLAYER_CLR);
            g2.drawString(hp, hpX, plateY + 75);

            g2.dispose();
        }
    }

    private void onNextChallenge() {
        if (currentEnemy == null) return;
        dispose();
        new BackstoryShowcase(
                CharacterStories.getEnemyStory(currentEnemy),
                CharacterStories.getEnemyTitle(currentEnemy),
                () -> SwingUtilities.invokeLater(() -> new ArcadeModeBattleFrame(player, arcadeManager)),
                () -> SwingUtilities.invokeLater(() -> new ArcadeTowerFrame(player, arcadeManager))
        );
    }

    private int getFrameIndex(String name) {
        for (int i = 0; i < ALL_NAMES.length; i++) if (ALL_NAMES[i].equals(name)) return i;
        return -1;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
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

    protected void drawImageFill(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        int dx = x + (w - dw) / 2, dy = y + (h - dh) / 2;
        g2.drawImage(img, dx, dy, dw, dh, null);
    }

    private JButton makeGoldButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h = getModel().isRollover();
                g2.setPaint(new GradientPaint(0, 0, h ? new Color(170, 110, 40) : new Color(120, 70, 20), 0, getHeight(), h ? new Color(120, 80, 30) : new Color(80, 40, 10)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                if (h) {
                    g2.setColor(new Color(255, 215, 120, 80));
                    g2.setStroke(new BasicStroke(3f)); g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 16, 16);
                }
                g2.setColor(new Color(220, 180, 90)); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 16, 16);
                g2.setFont(new Font("Serif", Font.BOLD, 16)); FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()-fm.stringWidth(getText()))/2, ty = (getHeight()+fm.getAscent())/2 - 2;
                g2.setColor(Color.BLACK); g2.drawString(getText(), tx+1, ty+1); g2.setColor(new Color(255, 230, 170)); g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(320, 54));
        return btn;
    }

    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth(), h = getHeight();
                int iw = img.getWidth(null), ih = img.getHeight(null);

                // Calculate scale requiring at least h + 300 to accommodate the panning offset safely
                double scale = Math.max((double) w / iw, (double) (h + 300) / ih);
                int dw = (int) (iw * scale);
                int dh = (int) (ih * scale);
                int dx = (w - dw) / 2;

                g2.drawImage(img, dx, (int)bgYOffset, dw, dh, null);
                g2.dispose();
            }
        }
    }
}