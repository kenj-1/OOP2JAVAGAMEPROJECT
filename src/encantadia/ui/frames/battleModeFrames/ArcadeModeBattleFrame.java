package encantadia.ui.frames.battleModeFrames;

import encantadia.BackstoryShowcase;
import encantadia.ScreenManager;
import encantadia.battle.ai.EnemyAI;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.battle.engine.TurnManager;
import encantadia.battle.result.TurnResult;
import encantadia.battle.skill.Skill;
import encantadia.characters.Character;
import encantadia.story.CharacterStories;
import encantadia.ui.frames.ArcadeVictoryFrame;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.URL;
import java.util.List;

/**
 * ArcadeModeBattleFrame
 *
 * Visual architecture mirrors PVEBattleFrame exactly:
 *   • Same colour palette, same top bar / side panels / log / skill bar
 *   • Same refreshUI / tintBar / refreshCdRow / updateTurnState pattern
 *
 * Arcade-specific additions:
 *   • Enemy counter in top bar ("Enemy 3 of 7")
 *   • Final boss gets a purple highlight treatment
 *   • No round system — single HP pool per fight
 *   • Upgrade flow between fights (HP boost at tier 3, ultimate at tier 6)
 *   • Defeat → Main Menu; Victory over all → ArcadeVictoryFrame
 */
public class ArcadeModeBattleFrame extends JFrame {

    // ── Constants ─────────────────────────────────────────────
    private static final int ENEMY_TURN_DELAY = 1100;

    // ── Colour palette ────────────────────────────────────────
    private static final String BATTLE_BG = "/resources/backgroundArcade.png";
    private static final Color BG_PANEL = new Color(0x22,0x1C,0x14,200); // was 0x22,0x1C,0x14
    private static final Color BG_LOG   = new Color(0x10,0x0D,0x07,210); // was 0x10,0x0D,0x07
    private static final Color BORDER_CLR = new Color(0xC8, 0xA0, 0x28);
    private static final Color PLAYER_CLR = new Color(0x2E, 0x8B, 0x57);
    private static final Color ENEMY_CLR  = new Color(0xB0, 0x2A, 0x2A);
    private static final Color BOSS_CLR   = new Color(0xAA, 0x00, 0xFF);
    private static final Color GOLD       = new Color(0xC8, 0xA0, 0x28);
    private static final Color CREAM      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color LOG_FG     = new Color(0xD4, 0xC5, 0xA0);
    private static final Color ORANGE_LOW = new Color(0xCC, 0x88, 0x22);
    private static final Color RED_CRIT   = new Color(0xCC, 0x22, 0x22);
    private static final Color GREEN_RDY  = new Color(0x60, 0xCC, 0x60);

    // ── Characters & engine ───────────────────────────────────
    private final Character         playerCharacter;
    private Character               enemyCharacter;
    private TurnManager             turnManager;
    private final ArcadeModeManager arcadeManager;

    // ── State ─────────────────────────────────────────────────
    private volatile boolean processingTurn = false;

    // ── UI refs ───────────────────────────────────────────────
    private JLabel       enemyCountLabel;
    private JLabel       playerHPLabel, enemyHPLabel;
    private JProgressBar playerHPBar,   enemyHPBar;
    private JTextArea    battleLog;
    private JButton[]    skillBtns = new JButton[4];
    private JLabel[]     cdLabels  = new JLabel[4];
    private JLabel       turnIndicator;
    private JPanel       skillBarInner; // rebuilt when ultimate is unlocked

    // Refs needed to update enemy side between fights
    private JPanel enemyPanel;
    private JLabel enemyRoleLabel, enemyNameLabel, enemyTitleLabel;

    // ══════════════════════════════════════════════════════════
    //  Constructors
    // ══════════════════════════════════════════════════════════

    public ArcadeModeBattleFrame(Character player, ArcadeModeManager arcadeManager) {
        this.playerCharacter = player;
        this.arcadeManager   = arcadeManager;
        this.enemyCharacter  = arcadeManager.getCurrentEnemy();

        setTitle("Arcade Mode — " + player.getName());
        setSize(1024, 768);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        setVisible(true);
        ScreenManager.register(this);

        startFight(); // kick off the first battle
    }

    /** Backward-compat single-arg constructor */
    public ArcadeModeBattleFrame(Character player) {
        this(player, new ArcadeModeManager(player));
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    // ══════════════════════════════════════════════════════════
    //  UI Construction  (mirrors PVEBattleFrame exactly)
    // ══════════════════════════════════════════════════════════

    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        // Background image
        JPanel bg = new JPanel(){
            private final Image img;
            { URL u=getClass().getResource(BATTLE_BG); img=u!=null?new ImageIcon(u).getImage():null; setOpaque(true); setBackground(Color.BLACK); }
            @Override protected void paintComponent(Graphics g){super.paintComponent(g);if(img!=null){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);g2.drawImage(img,0,0,getWidth(),getHeight(),null);g2.dispose();}}
        };
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        // Existing content on top
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(buildTopBar(), BorderLayout.NORTH);
        content.add(buildMainArea(), BorderLayout.CENTER);
        content.add(buildSkillBar(), BorderLayout.SOUTH);
        lp.add(content, JLayeredPane.PALETTE_LAYER);

        lp.addComponentListener(new java.awt.event.ComponentAdapter(){
            @Override public void componentResized(java.awt.event.ComponentEvent e){
                int W=lp.getWidth(),H=lp.getHeight();
                bg.setBounds(0,0,W,H); content.setBounds(0,0,W,H);
            }
        });
    }

    // ── Top bar ───────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x0C, 0x09, 0x05));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_CLR));
        bar.setPreferredSize(new Dimension(0, 62));

        JPanel left  = new JPanel(); left.setOpaque(false);  left.setPreferredSize(new Dimension(120, 0));
        JPanel right = new JPanel(); right.setOpaque(false); right.setPreferredSize(new Dimension(120, 0));

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        JLabel arcadeTitle = new JLabel("⚔  ARCADE MODE", SwingConstants.CENTER);
        arcadeTitle.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 22));
        arcadeTitle.setForeground(GOLD);
        arcadeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        enemyCountLabel = new JLabel("", SwingConstants.CENTER);
        enemyCountLabel.setFont(new Font("Serif", Font.ITALIC, 12));
        enemyCountLabel.setForeground(new Color(0xA0, 0x88, 0x50));
        enemyCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centre.add(Box.createVerticalGlue());
        centre.add(arcadeTitle);
        centre.add(enemyCountLabel);
        centre.add(Box.createVerticalGlue());

        bar.add(left,   BorderLayout.WEST);
        bar.add(centre, BorderLayout.CENTER);
        bar.add(right,  BorderLayout.EAST);
        return bar;
    }

    // ── Main area ─────────────────────────────────────────────
    private JPanel buildMainArea() {
        JPanel area = new JPanel(new GridBagLayout());
        area.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.weighty = 1.0;
        g.weightx = 1.0; // ✅ ADD THIS BEFORE FIRST USE

        g.gridx = 0; g.weightx = 0.26;
        g.gridx = 1; g.weightx = 0.48;
        g.gridx = 2; g.weightx = 0.26;

        g.fill = GridBagConstraints.BOTH; g.weighty = 1.0;

        g.gridx = 0; g.weightx = 0.26; g.insets = new Insets(10, 10, 6, 6);
        area.add(buildPlayerPanel(), g);

        g.gridx = 1; g.weightx = 0.48; g.insets = new Insets(10, 0, 6, 0);
        area.add(buildLogPanel(), g);

        g.gridx = 2; g.weightx = 0.26; g.insets = new Insets(10, 6, 6, 10);
        enemyPanel = buildEnemyPanel();
        area.add(enemyPanel, g);

        return area;
    }

    private JPanel buildPlayerPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PLAYER_CLR, 1),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        panel.add(makePortrait(playerCharacter));
        panel.add(Box.createVerticalStrut(6));

        JLabel roleTag = makeAlignedLabel("PLAYER", new Font("SansSerif", Font.BOLD, 10), PLAYER_CLR);
        JLabel nameLbl = makeAlignedLabel(playerCharacter.getName(), new Font("Serif", Font.BOLD, 14), CREAM);
        JLabel titlLbl = makeAlignedLabel(playerCharacter.getTitle(), new Font("Serif", Font.ITALIC, 9),
                new Color(0xA0, 0x88, 0x50));

        playerHPBar = makeHPBar(playerCharacter, PLAYER_CLR);
        playerHPLabel = makeAlignedLabel("", new Font("SansSerif", Font.PLAIN, 11), LOG_FG);

        turnIndicator = makeAlignedLabel("▶ Your Turn", new Font("Serif", Font.BOLD | Font.ITALIC, 12), GOLD);

        panel.add(roleTag);
        panel.add(Box.createVerticalStrut(2));
        panel.add(nameLbl);
        panel.add(Box.createVerticalStrut(2));
        panel.add(titlLbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(playerHPBar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(playerHPLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(turnIndicator);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildEnemyPanel() {
        boolean isBoss = arcadeManager.isFinalBoss();
        Color   ec     = isBoss ? BOSS_CLR : ENEMY_CLR;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ec, 1),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        panel.add(makePortrait(enemyCharacter));
        panel.add(Box.createVerticalStrut(6));

        enemyRoleLabel = makeAlignedLabel(isBoss ? "⚡ FINAL BOSS" : "ENEMY",
                new Font("SansSerif", Font.BOLD, 10), ec);
        enemyNameLabel  = makeAlignedLabel(enemyCharacter.getName(),  new Font("Serif", Font.BOLD, 14), CREAM);
        enemyTitleLabel = makeAlignedLabel(enemyCharacter.getTitle(), new Font("Serif", Font.ITALIC, 9),
                new Color(0xA0, 0x88, 0x50));

        enemyHPBar   = makeHPBar(enemyCharacter, ec);
        enemyHPLabel = makeAlignedLabel("", new Font("SansSerif", Font.PLAIN, 11), LOG_FG);

        panel.add(enemyRoleLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(enemyNameLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(enemyTitleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(enemyHPBar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(enemyHPLabel);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildLogPanel() {
        battleLog = new JTextArea();
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        battleLog.setForeground(LOG_FG);
        battleLog.setBackground(BG_LOG);
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JScrollPane scroll = new JScrollPane(battleLog,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(new LineBorder(BORDER_CLR, 1));
        scroll.getViewport().setBackground(BG_LOG);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    // ── Skill bar (up to 4 slots; slot 4 unlocked at tier 6) ──
    private JPanel buildSkillBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(0x0C, 0x09, 0x05));
        wrapper.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_CLR));
        wrapper.setPreferredSize(new Dimension(0, 100));

        skillBarInner = new JPanel(new GridLayout(1, 4, 8, 0));
        skillBarInner.setBackground(new Color(0x0C, 0x09, 0x05));
        skillBarInner.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        rebuildSkillSlots();
        wrapper.add(skillBarInner, BorderLayout.CENTER);
        return wrapper;
    }

    private void rebuildSkillSlots() {
        skillBarInner.removeAll();
        List<Skill> skills = playerCharacter.getSkills();

        for (int i = 0; i < 4; i++) {
            JPanel slot = new JPanel(new BorderLayout(0, 3));
            slot.setOpaque(false);

            if (i < skills.size()) {
                final int si = i;
                skillBtns[i] = makeSkillButton(skills.get(i).getName(), PLAYER_CLR);
                skillBtns[i].setEnabled(false);
                skillBtns[i].addActionListener(e -> onPlayerSkill(si));
                cdLabels[i] = makeCdLabel("READY", GREEN_RDY);
            } else {
                skillBtns[i] = makeSkillButton("—", new Color(0x40, 0x30, 0x18));
                skillBtns[i].setEnabled(false);
                cdLabels[i] = makeCdLabel("LOCKED", new Color(0x60, 0x50, 0x30));
            }

            slot.add(skillBtns[i], BorderLayout.CENTER);
            slot.add(cdLabels[i],  BorderLayout.SOUTH);
            skillBarInner.add(slot);
        }
        skillBarInner.revalidate();
        skillBarInner.repaint();
    }

    // ══════════════════════════════════════════════════════════
    //  Battle flow
    // ══════════════════════════════════════════════════════════

    private void startFight() {
        enemyCharacter = arcadeManager.getCurrentEnemy();
        if (enemyCharacter == null) { triggerVictory(); return; }

        boolean isBoss = arcadeManager.isFinalBoss();
        int idx   = arcadeManager.getCurrentIndex() + 1;
        int total = arcadeManager.getTotalEnemies();

        enemyCountLabel.setText(isBoss
                ? "⚡  FINAL BOSS  ⚡"
                : "Enemy  " + idx + "  of  " + total);

        Color ec = isBoss ? BOSS_CLR : ENEMY_CLR;
        enemyPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(ec, 1),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));
        enemyRoleLabel.setText(isBoss ? "⚡ FINAL BOSS" : "ENEMY");
        enemyRoleLabel.setForeground(ec);
        enemyNameLabel.setText(enemyCharacter.getName());
        enemyTitleLabel.setText(enemyCharacter.getTitle());

        playerCharacter.reset();
        enemyCharacter.reset();

        playerHPBar.setMaximum(playerCharacter.getMaxHP());
        enemyHPBar.setMaximum(enemyCharacter.getMaxHP());

        turnManager = new TurnManager(playerCharacter, enemyCharacter);

        // ❌ REMOVE THIS (causing error)
        // turnManager.setTarget(enemyCharacter);

        refreshUI();
        rebuildSkillSlots();
        refreshCdRow();
        updateTurnState();

        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log(isBoss ? "⚡  FINAL BOSS BATTLE!" : "⚔  Arcade Battle " + idx + " of " + total);
        log(playerCharacter.getName() + "  vs  " + enemyCharacter.getName());
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ── Turn logic ────────────────────────────────────────────
    private void onPlayerSkill(int si) {
        if (processingTurn || !turnManager.isPlayerTurn()) return;
        processingTurn = true;
        setPlayerEnabled(false);

        TurnResult res = turnManager.executeSkill(
                playerCharacter,
                arcadeManager.getCurrentEnemy(), // ✅ ALWAYS FRESH
                si
        );
        flushResult(res);
        refreshUI();

        if (res.isTargetDefeated()) { onPlayerWon(); return; }
        if (res.isTurnStolen())     { setPlayerEnabled(true); processingTurn = false; return; }

        turnManager.advanceTurn();
        refreshCdRow();
        updateTurnState();

        new Timer(ENEMY_TURN_DELAY, e -> doEnemyTurn()) {{ setRepeats(false); start(); }};
    }

    private void doEnemyTurn() {
        int si = EnemyAI.chooseSkill(enemyCharacter, turnManager.getCooldownManager());
        TurnResult res = turnManager.executeSkill(enemyCharacter, playerCharacter, si);
        flushResult(res);
        refreshUI();

        if (res.isTargetDefeated()) { onPlayerLost(); return; }
        if (res.isTurnStolen()) {
            new Timer(ENEMY_TURN_DELAY, e -> doEnemyTurn()) {{ setRepeats(false); start(); }};
            return;
        }

        turnManager.advanceTurn();
        refreshCdRow();
        updateTurnState();
        setPlayerEnabled(true);
        processingTurn = false;
    }

    private void updateTurnState() {
        boolean playerTurn = turnManager.isPlayerTurn();
        setPlayerEnabled(playerTurn);
        if (turnIndicator == null) return;
        if (playerTurn) {
            turnIndicator.setText("▶ Your Turn");
            turnIndicator.setForeground(GOLD);
        } else {
            turnIndicator.setText("⏳ " + enemyCharacter.getName() + " is acting...");
            turnIndicator.setForeground(ORANGE_LOW);
        }
    }

    // ── Win / Lose ────────────────────────────────────────────
    private void onPlayerWon() {
        setPlayerEnabled(false);
        arcadeManager.recordVictory();
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("🏆  " + playerCharacter.getName() + " defeated " + enemyCharacter.getName() + "!");

        arcadeManager.recordVictory(); // advances currentIndex

        if (arcadeManager.isFinished()) {
            new Timer(1400, e -> { ((Timer)e.getSource()).stop(); triggerVictory(); })
            {{ setRepeats(false); start(); }};
            return;
        }

        if (arcadeManager.shouldGiveHPBoost()) {
            new Timer(1200, e -> { ((Timer)e.getSource()).stop(); giveHPBoost(); })
            {{ setRepeats(false); start(); }};
        } else if (arcadeManager.shouldGiveUltimate()) {
            new Timer(1200, e -> { ((Timer)e.getSource()).stop(); giveUltimate(); })
            {{ setRepeats(false); start(); }};
        } else {
            new Timer(1500, e -> { ((Timer)e.getSource()).stop(); advanceToNextEnemy(); })
            {{ setRepeats(false); start(); }};
        }
    }

    private void onPlayerLost() {
        setPlayerEnabled(false);
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("💀  " + enemyCharacter.getName() + " has defeated you!");

        new Timer(1600, e -> {
            ((Timer)e.getSource()).stop();
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "You have fallen in battle!\n"
                                + enemyCharacter.getName() + " proved too powerful.\n\n"
                                + "Returning to main menu...",
                        "Defeat", JOptionPane.WARNING_MESSAGE);
                dispose();
                new MainMenuFrame();
            });
        }) {{ setRepeats(false); start(); }};
    }

    // ── Rewards ───────────────────────────────────────────────
    private void giveHPBoost() {
        playerCharacter.increaseMaxHP(ArcadeModeManager.HP_BOOST_AMT);
        log("✨  Reward: +" + ArcadeModeManager.HP_BOOST_AMT + " Max HP granted!");
        playerHPBar.setMaximum(playerCharacter.getMaxHP());
        refreshUI();
        JOptionPane.showMessageDialog(this,
                "✨  Power Reward!\n\n+1,000 Max HP granted!\nYou grow stronger with each victory...",
                "HP Boost Acquired", JOptionPane.INFORMATION_MESSAGE);
        advanceToNextEnemy();
    }

    private void giveUltimate() {
        List<Skill> choices = arcadeManager.getUltimateChoices();
        if (choices.isEmpty()) { advanceToNextEnemy(); return; }

        String[] options = choices.stream()
                .map(s -> s.getName() + "  (" + s.getMinDamage() + "–" + s.getMaxDamage() + ")")
                .toArray(String[]::new);

        int pick = JOptionPane.showOptionDialog(this,
                "🔥  ULTIMATE SKILL REWARD!\n\nChoose one ultimate ability from your defeated foes:",
                "Ultimate Acquired",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (pick >= 0 && pick < choices.size()) {
            Skill chosen = choices.get(pick);
            playerCharacter.addSkill(chosen);
            rebuildSkillSlots();
            log("🔥  Ultimate unlocked: " + chosen.getName() + "!");
        }
        advanceToNextEnemy();
    }

    private void advanceToNextEnemy() {

        Character next = arcadeManager.getCurrentEnemy();
        if (next == null) { triggerVictory(); return; }

        // Show next enemy backstory, then resume this frame
        Runnable resume = () -> SwingUtilities.invokeLater(() -> {
            setVisible(true);
            startFight();
        });

        setVisible(false);
        new BackstoryShowcase(
                CharacterStories.getEnemyStory(next),
                CharacterStories.getEnemyTitle(next),
                resume);
    }

    private void triggerVictory() {
        dispose();
        new ArcadeVictoryFrame(playerCharacter);
    }

    // ══════════════════════════════════════════════════════════
    //  UI helpers  (mirrors PVEBattleFrame exactly)
    // ══════════════════════════════════════════════════════════

    private void refreshUI() {
        if (playerHPBar == null) return;

        playerHPBar.setMaximum(playerCharacter.getMaxHP());
        playerHPBar.setValue(playerCharacter.getCurrentHP());
        enemyHPBar.setMaximum(enemyCharacter.getMaxHP());
        enemyHPBar.setValue(enemyCharacter.getCurrentHP());

        playerHPLabel.setText("HP: " + playerCharacter.getCurrentHP() + " / " + playerCharacter.getMaxHP());
        enemyHPLabel.setText("HP: " + enemyCharacter.getCurrentHP() + " / " + enemyCharacter.getMaxHP());

        tintBar(playerHPBar, playerCharacter, PLAYER_CLR);
        tintBar(enemyHPBar,  enemyCharacter,  arcadeManager.isFinalBoss() ? BOSS_CLR : ENEMY_CLR);
        repaint();
    }

    private void tintBar(JProgressBar bar, Character c, Color base) {
        double pct = (double) c.getCurrentHP() / c.getMaxHP();
        if      (pct <= 0.25) bar.setForeground(RED_CRIT);
        else if (pct <= 0.50) bar.setForeground(ORANGE_LOW);
        else                  bar.setForeground(base);
    }

    private void refreshCdRow() {
        if (turnManager == null) return;
        List<Skill> skills = playerCharacter.getSkills();
        for (int i = 0; i < skillBtns.length; i++) {
            if (skillBtns[i] == null || i >= skills.size()) continue;
            int cd = turnManager.getCooldownManager().getRemainingCooldown(playerCharacter, i);
            if (cd > 0) {
                cdLabels[i].setText(cd + " turn(s)");
                cdLabels[i].setForeground(ORANGE_LOW);
                skillBtns[i].setEnabled(false);
            } else {
                cdLabels[i].setText("READY");
                cdLabels[i].setForeground(GREEN_RDY);
                skillBtns[i].setEnabled(true);
            }
        }
    }

    private void setPlayerEnabled(boolean on) {
        for (JButton b : skillBtns) if (b != null) b.setEnabled(on);
        if (on) refreshCdRow();
    }

    private void flushResult(TurnResult r) { for (String m : r.getLogMessages()) log(m); }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (battleLog != null) {
                battleLog.append(msg + "\n");
                battleLog.setCaretPosition(battleLog.getDocument().getLength());
            }
        });
    }

    // ── Widget factories ──────────────────────────────────────
    private JPanel makePortrait(Character ch) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x18, 0x14, 0x0E));
        p.setBorder(new LineBorder(new Color(0x40, 0x32, 0x18), 1));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        p.setPreferredSize(new Dimension(0, 100));
        JLabel lbl = new JLabel(ch.getName(), SwingConstants.CENTER);
        lbl.setFont(new Font("Serif", Font.ITALIC, 11));
        lbl.setForeground(new Color(0x70, 0x60, 0x40));
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private JProgressBar makeHPBar(Character ch, Color color) {
        JProgressBar bar = new JProgressBar(0, ch.getMaxHP());
        bar.setValue(ch.getCurrentHP());
        bar.setStringPainted(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 13));
        bar.setForeground(color);
        bar.setBackground(new Color(0x2A, 0x1A, 0x10));
        bar.setBorder(BorderFactory.createLineBorder(new Color(0x50, 0x40, 0x20)));
        return bar;
    }

    private JLabel makeAlignedLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JLabel makeCdLabel(String text, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 9));
        lbl.setForeground(color);
        return lbl;
    }

    private JButton makeSkillButton(String label, Color accent) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h  = getModel().isRollover();
                boolean en = isEnabled();
                g2.setPaint(new GradientPaint(0, 0,
                        h ? new Color(0x3C,0x2C,0x10) : new Color(0x28,0x1E,0x0C),
                        0, getHeight(),
                        h ? new Color(0x28,0x1A,0x08) : new Color(0x18,0x10,0x06)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(en
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), h ? 255 : 180)
                        : new Color(0x50, 0x40, 0x20, 100));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.setFont(new Font("Serif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString(getText(), tx+1, ty+1);
                g2.setColor(en ? (h ? new Color(0xFF,0xE0,0x80) : CREAM) : new Color(0x60,0x50,0x30));
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}