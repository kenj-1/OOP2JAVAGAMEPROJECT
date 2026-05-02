package encantadia.ui.frames.battleModeFrames;

import encantadia.ScreenManager;
import encantadia.battle.ai.EnemyAI;
import encantadia.battle.arcade.ArcadeModeManager;
import encantadia.battle.arcade.DatabaseManager;
import encantadia.battle.engine.TurnManager;
import encantadia.battle.result.TurnResult;
import encantadia.battle.skill.Skill;
import encantadia.characters.*;
import encantadia.characters.Character;
import encantadia.characters.animation.CharacterAnimator;
import encantadia.ui.frames.ArcadeTowerFrame;
import encantadia.ui.frames.ArcadeVictoryFrame;
import encantadia.ui.frames.LeaderboardFrame;
import encantadia.ui.frames.CharacterSelectionFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArcadeModeBattleFrame extends JFrame {

    private javax.swing.Timer turnCountdownTimer;
    private int               timeLeft = 10;
    private static final int  MATCH_DURATION_SECONDS = 60;
    private javax.swing.Timer matchTimer;
    private int               matchTimeLeft = MATCH_DURATION_SECONDS;
    private static final String BG_PATH          = "/resources/backgroundArcade.png";
    private static final int    ENEMY_TURN_DELAY = 1100;

    private boolean           ultimateUnlocked = false;
    private int               summonCharges    = 3;
    private boolean           isSummoning      = false;
    private Character         assistCharacter  = null;
    private CharacterAnimator assistAnimator   = null;
    private int[]             assistDurations  = new int[]{1600};

    private static final String[] FRAME_IMGS = {
            "/resources/tyroneFrame (1).png", "/resources/elanFrame (1).png",
            "/resources/claireFrame (1).png", "/resources/dirkFrame (1).png",
            "/resources/flamaraFrame (1).png", "/resources/deaFrame (1).png",
            "/resources/adamusFrame (1).png",  "/resources/teraFrame (1).png"
    };

    private static final String[] CHAR_NAMES = {
            "Tyrone", "Makelan Shere", "Claire", "Dirk", "Flamara", "Dea", "Adamus", "Tera"
    };

    private static final Color PLAYER_CLR = new Color(0x2E, 0x8B, 0x57);
    private static final Color ENEMY_CLR  = new Color(0xB0, 0x2A, 0x2A);
    private static final Color BOSS_CLR   = new Color(0xAA, 0x00, 0xFF);
    private static final Color GOLD       = new Color(0xC8, 0xA0, 0x28);
    private static final Color CREAM      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color LOG_FG     = new Color(0xD4, 0xC5, 0xA0);
    private static final Color ORANGE_LOW = new Color(0xCC, 0x88, 0x22);
    private static final Color RED_CRIT   = new Color(0xCC, 0x22, 0x22);
    private static final Color GREEN_RDY  = new Color(0x60, 0xCC, 0x60);

    private final Character         playerCharacter;
    private final Character         enemyCharacter;
    private final ArcadeModeManager arcadeManager;
    private TurnManager             turnManager;
    private volatile boolean        processingTurn = false;
    private volatile boolean        timeUpTriggered = false;

    private CharacterAnimator playerAnimator;
    private CharacterAnimator enemyAnimator;
    private float playerFlashAlpha = 0f;
    private float enemyFlashAlpha  = 0f;
    private float glowTick         = 0f;
    private Timer glowAnimTimer;

    private BattleCanvas battleCanvas;
    private JPanel       skillsLayer;
    private JPanel       overlayLayer;
    private JTextArea    battleLog;
    private JButton[]    skillBtns = new JButton[4];
    private JLabel[]     cdLabels  = new JLabel[4];

    public ArcadeModeBattleFrame(Character player, ArcadeModeManager manager) {
        this.playerCharacter = player;
        this.arcadeManager   = manager;
        this.enemyCharacter  = manager.getCurrentEnemy();

        if (enemyCharacter == null) {
            SwingUtilities.invokeLater(() -> new ArcadeVictoryFrame(player));
            return;
        }

        playerCharacter.reset();
        enemyCharacter.reset();
        this.turnManager = new TurnManager(playerCharacter, enemyCharacter);

        this.playerAnimator = CharacterAnimator.forCharacter(playerCharacter);
        this.enemyAnimator  = CharacterAnimator.forCharacter(enemyCharacter);

        boolean isBoss = arcadeManager.isFinalBoss();
        int idx   = arcadeManager.getCurrentIndex() + 1;
        int total = arcadeManager.getTotalEnemies();

        setTitle("Arcade Mode — " + player.getName() + " vs " + enemyCharacter.getName());
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        glowAnimTimer = new Timer(20, e -> {
            glowTick += 0.08f;
            if (playerFlashAlpha > 0) playerFlashAlpha = Math.max(0, playerFlashAlpha - 0.04f);
            if (enemyFlashAlpha > 0)  enemyFlashAlpha  = Math.max(0, enemyFlashAlpha  - 0.04f);
            if (battleCanvas != null) battleCanvas.repaint();
        });

        if (playerCharacter.getSkills().size() >= 4) {
            ultimateUnlocked = true;
            restoreAssistAnimator();
        }

        buildUI(isBoss, idx, total);
        registerHotkeys();
        setVisible(true);
        ScreenManager.register(this);

        refreshUI();
        rebuildSkillSlots();
        refreshCdRow();
        updateTurnState();

        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log(isBoss ? "⚡  FINAL BOSS BATTLE!" : "⚔  Arcade Battle " + idx + " of " + total);
        log(playerCharacter.getName() + "  vs  " + enemyCharacter.getName());
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        startMatchTimer();
    }

    public ArcadeModeBattleFrame(Character player) {
        this(player, new ArcadeModeManager(player));
    }

    @Override
    public void dispose() {
        stopMatchTimer();
        stopTurnTimer();
        if (glowAnimTimer != null) glowAnimTimer.stop();
        if (playerAnimator != null) playerAnimator.dispose();
        if (enemyAnimator != null) enemyAnimator.dispose();
        if (assistAnimator != null) assistAnimator.dispose();
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void buildUI(boolean isBoss, int idx, int total) {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        battleCanvas = new BattleCanvas(isBoss, idx, total);
        lp.add(battleCanvas, JLayeredPane.PALETTE_LAYER);

        skillsLayer = new JPanel(null);
        skillsLayer.setOpaque(false);
        buildSkillsLayer();
        lp.add(skillsLayer, JLayeredPane.MODAL_LAYER);

        overlayLayer = new JPanel(null);
        overlayLayer.setOpaque(false);
        overlayLayer.setVisible(false);
        lp.add(overlayLayer, JLayeredPane.POPUP_LAYER);

        lp.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int W = lp.getWidth(), H = lp.getHeight();
                if (W == 0 || H == 0) return;
                bg.setBounds(0, 0, W, H);
                battleCanvas.setBounds(0, 0, W, H);
                skillsLayer.setBounds(0, 0, W, H);
                overlayLayer.setBounds(0, 0, W, H);
                layoutSkillsLayer(W, H);
            }
        });
    }

    private void buildSkillsLayer() {
        JPanel skillPanel = new JPanel();
        skillPanel.setOpaque(false);
        skillPanel.setLayout(new BoxLayout(skillPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < 4; i++) {
            final int si = i;
            skillBtns[i] = makePillButton("—", new Color(0x70,0x14,0x14), new Color(0xFF,0x99,0x99));
            skillBtns[i].setEnabled(false);
            skillBtns[i].addActionListener(e -> onPlayerSkill(si));
            cdLabels[i] = new JLabel("LOCKED", SwingConstants.CENTER);
            cdLabels[i].setFont(new Font("SansSerif", Font.BOLD, 9));
            cdLabels[i].setForeground(new Color(0x60, 0x50, 0x30));
            JPanel slot = new JPanel(new BorderLayout(0, 2));
            slot.setOpaque(false);
            slot.add(skillBtns[i], BorderLayout.CENTER);
            slot.add(cdLabels[i], BorderLayout.SOUTH);
            skillPanel.add(slot);
            if (i < 3) skillPanel.add(Box.createVerticalStrut(4));
        }
        skillsLayer.add(skillPanel);
        skillsLayer.putClientProperty("skills", skillPanel);

        battleLog = new JTextArea();
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        battleLog.setForeground(LOG_FG);
        battleLog.setOpaque(false);
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(battleLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.setBorder(null);

        JPanel logHolder = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 150)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xC8, 0xA0, 0x28, 70)); g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        logHolder.setOpaque(false); logHolder.add(scroll, BorderLayout.CENTER);
        skillsLayer.add(logHolder); skillsLayer.putClientProperty("log", logHolder);
    }

    private void rebuildSkillSlots() {
        List<Skill> skills = playerCharacter.getSkills();
        for (int i = 0; i < 4; i++) {
            if (skillBtns[i] == null) continue;
            if (i < skills.size()) {
                if (i == 3 && ultimateUnlocked) {
                    skillBtns[i].setText(skills.get(i).getName() + " (" + summonCharges + ")");
                    cdLabels[i].setText(summonCharges > 0 ? "READY" : "EXHAUSTED");
                    cdLabels[i].setForeground(summonCharges > 0 ? new Color(0xFF,0xAA,0x30) : RED_CRIT);
                } else {
                    skillBtns[i].setText(skills.get(i).getName());
                    cdLabels[i].setText("READY");
                    cdLabels[i].setForeground(GREEN_RDY);
                }
                skillBtns[i].setEnabled(false);
            } else {
                skillBtns[i].setText("—");
                skillBtns[i].setEnabled(false);
                cdLabels[i].setText("LOCKED");
                cdLabels[i].setForeground(new Color(0x60, 0x50, 0x30));
            }
        }
        skillsLayer.revalidate();
        skillsLayer.repaint();
    }

    private void layoutSkillsLayer(int W, int H) {
        JPanel sp = (JPanel) skillsLayer.getClientProperty("skills");
        JPanel lh = (JPanel) skillsLayer.getClientProperty("log");
        if (sp == null) return;
        double sc = Math.min(W / 1024.0, H / 768.0);
        int skillW = (int)(170*sc), skillH = (int)(140*sc);
        sp.setBounds((int)(10*sc), H - skillH - (int)(16*sc), skillW, skillH);
        if (lh != null) {
            int lw = (int)(380*sc), lhh = (int)(150*sc);
            lh.setBounds((W - lw) / 2, H - lhh - (int)(16*sc), lw, lhh);
        }
        skillsLayer.revalidate();
        skillsLayer.repaint();
    }

    private void onPlayerSkill(int si) {
        if (timeUpTriggered) return;
        stopTurnTimer();
        if (matchTimer != null && matchTimer.isRunning()) matchTimer.stop();
        if (processingTurn || !turnManager.isPlayerTurn()) return;

        if (si == 3) {
            if (summonCharges <= 0) {
                if (matchTimeLeft > 0) matchTimer.start();
                return;
            }
            executeSummonAssist(si);
            return;
        }

        processingTurn = true;
        setPlayerEnabled(false);
        if (playerAnimator != null) playerAnimator.toSkill(si);

        int durationMs  = getSkillDuration(playerCharacter, si);
        int impactDelay = (int)(durationMs * 0.65);

        Timer impactTimer = new Timer(impactDelay, e -> {
            ((Timer)e.getSource()).stop();
            TurnResult res = turnManager.executeSkill(playerCharacter, enemyCharacter, si);
            flushResult(res);

            enemyFlashAlpha = 1.0f;
            if (enemyAnimator != null) enemyAnimator.triggerHit();
            refreshUI();

            Timer recoveryTimer = new Timer((durationMs - impactDelay) + 150, ev -> {
                ((Timer)ev.getSource()).stop();
                if (matchTimeLeft > 0 && !timeUpTriggered) matchTimer.start();

                if (res.isTargetDefeated()) { onPlayerWon(); return; }
                if (res.isTurnStolen())     { setPlayerEnabled(true); processingTurn = false; return; }

                turnManager.advanceTurn(); refreshCdRow(); updateTurnState();
                new Timer(ENEMY_TURN_DELAY, et -> doEnemyTurn()) {{ setRepeats(false); start(); }};
            });
            recoveryTimer.setRepeats(false); recoveryTimer.start();
        });
        impactTimer.setRepeats(false); impactTimer.start();
    }

    private void executeSummonAssist(int si) {
        processingTurn = true;
        setPlayerEnabled(false);
        summonCharges--;
        rebuildSkillSlots();

        isSummoning = true;

        if (assistAnimator != null) {
            assistAnimator.toIdle();
            log("✨ " + playerCharacter.getName() + " summons " + assistCharacter.getName() + "!");

            Timer entrance = new Timer(400, ev -> {
                ((Timer)ev.getSource()).stop();
                assistAnimator.toSkill(0);

                int durationMs = assistDurations[0];
                int impactDelay = (int)(durationMs * 0.65);

                Timer impactTimer = new Timer(impactDelay, e -> {
                    ((Timer)e.getSource()).stop();
                    TurnResult res = turnManager.executeSkill(playerCharacter, enemyCharacter, si);
                    flushResult(res);

                    enemyFlashAlpha = 1.0f;
                    if (enemyAnimator != null) enemyAnimator.triggerHit();
                    refreshUI();

                    Timer recoveryTimer = new Timer((durationMs - impactDelay) + 200, rEv -> {
                        ((Timer)rEv.getSource()).stop();

                        isSummoning = false;
                        if (playerAnimator != null) playerAnimator.toIdle();
                        refreshUI();

                        if (matchTimeLeft > 0 && !timeUpTriggered) matchTimer.start();

                        if (res.isTargetDefeated()) { onPlayerWon(); return; }
                        if (res.isTurnStolen())     { setPlayerEnabled(true); processingTurn = false; return; }

                        turnManager.advanceTurn(); refreshCdRow(); updateTurnState();
                        new Timer(ENEMY_TURN_DELAY, et -> doEnemyTurn()){{setRepeats(false);start();}};
                    });
                    recoveryTimer.setRepeats(false); recoveryTimer.start();
                });
                impactTimer.setRepeats(false); impactTimer.start();
            });
            entrance.setRepeats(false); entrance.start();
        } else {
            TurnResult res = turnManager.executeSkill(playerCharacter, enemyCharacter, si);
            flushResult(res);
            isSummoning = false;
            turnManager.advanceTurn(); updateTurnState(); processingTurn = false;
            if (matchTimeLeft > 0 && !timeUpTriggered) matchTimer.start();
        }
    }

    private void doEnemyTurn() {
        if (timeUpTriggered) return;
        if (matchTimer != null && matchTimer.isRunning()) matchTimer.stop();

        int si = EnemyAI.chooseSkill(enemyCharacter, turnManager.getCooldownManager());
        if (enemyAnimator != null) enemyAnimator.toSkill(si);

        int durationMs  = getSkillDuration(enemyCharacter, si);
        int impactDelay = (int)(durationMs * 0.65);

        Timer impactTimer = new Timer(impactDelay, e -> {
            ((Timer)e.getSource()).stop();
            TurnResult res = turnManager.executeSkill(enemyCharacter, playerCharacter, si);
            flushResult(res);

            playerFlashAlpha = 1.0f;
            if (playerAnimator != null) playerAnimator.triggerHit();
            refreshUI();

            Timer recoveryTimer = new Timer((durationMs - impactDelay) + 150, ev -> {
                ((Timer)ev.getSource()).stop();
                if (matchTimeLeft > 0 && !timeUpTriggered) matchTimer.start();

                if (res.isTargetDefeated()) { onPlayerLost(); return; }
                if (res.isTurnStolen()) {
                    new Timer(ENEMY_TURN_DELAY, et -> doEnemyTurn()) {{ setRepeats(false); start(); }};
                    return;
                }
                turnManager.advanceTurn(); refreshCdRow(); updateTurnState();
                setPlayerEnabled(true); processingTurn = false;
            });
            recoveryTimer.setRepeats(false); recoveryTimer.start();
        });
        impactTimer.setRepeats(false); impactTimer.start();
    }

    private int getSkillDuration(Character ch, int si) {
        int[] durations = ch.getSkillAnimationDurations();
        if (durations != null && si >= 0 && si < durations.length) return durations[si];
        return 1400;
    }

    private void updateTurnState() {
        if (timeUpTriggered) return;
        boolean myTurn = turnManager.isPlayerTurn();
        setPlayerEnabled(myTurn);
        if (battleCanvas != null) battleCanvas.setPlayerActive(myTurn);
        if (myTurn) startTurnTimer(); else stopTurnTimer();
    }

    private void onPlayerWon() {
        stopMatchTimer(); stopTurnTimer();
        setPlayerEnabled(false); processingTurn = false;

        if (playerAnimator != null) playerAnimator.toIdle();
        if (enemyAnimator != null) enemyAnimator.dispose();

        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("🏆  " + playerCharacter.getName() + " defeated " + enemyCharacter.getName() + "!");
        arcadeManager.recordVictory();

        new Timer(1100, e -> {
            ((Timer)e.getSource()).stop();
            SwingUtilities.invokeLater(this::processVictoryOutcome);
        }).start();
    }

    private void processVictoryOutcome() {
        if (arcadeManager.isFinished()) { dispose(); new ArcadeVictoryFrame(playerCharacter); return; }

        if (arcadeManager.shouldGiveHPBoost() || arcadeManager.shouldGiveUltimate()) {
            playRewardSequence(() -> {
                if (arcadeManager.shouldGiveHPBoost()) {
                    playerCharacter.increaseMaxHP(ArcadeModeManager.HP_BOOST_AMT);
                    log("✨  Reward: +" + ArcadeModeManager.HP_BOOST_AMT + " Max HP!");
                    showInfoOverlay("✨  POWER SURGE", "+" + ArcadeModeManager.HP_BOOST_AMT + " Max HP granted!\nYou grow stronger with each victory.", this::transitionToTower);
                } else {
                    showUltimateDraftOverlay(this::transitionToTower);
                }
            });
        } else {
            transitionToTower();
        }
    }

    private void playRewardSequence(Runnable onComplete) {
        JPanel rewardPanel = new JPanel() {
            float alpha = 0f; float pTick = 0f; Timer t;
            {
                setOpaque(false);
                t = new Timer(16, e -> { alpha = Math.min(1f, alpha + 0.035f); pTick += 0.06f; repaint(); });
                t.start();
                new Timer(2800, e -> { t.stop(); overlayLayer.remove(this); overlayLayer.setVisible(false); overlayLayer.repaint(); onComplete.run(); }){{setRepeats(false); start();}};
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(0, 0, 0, (int)(190 * alpha)));
                g2.fillRect(0, 0, getWidth(), getHeight());

                int cx = getWidth() / 2, cy = getHeight() / 2;

                for(int i=0; i<25; i++) {
                    double phase = pTick + i * 0.4;
                    int px = cx + (int)(Math.sin(phase * 0.9 + i) * 120);
                    int py = cy + 150 - (int)((pTick * 35 + i * 25) % 300);
                    int size = (int)(5 + Math.sin(phase)*3);
                    g2.setColor(new Color(255, 215, 100, (int)(220 * alpha)));
                    g2.fillOval(px, py, size, size);
                }

                if (playerAnimator != null) {
                    playerAnimator.toIdle();
                    playerAnimator.draw(g2, cx - 120, cy - 120, 240, 240, this);
                }
                g2.dispose();
            }
        };
        rewardPanel.setBounds(0, 0, getWidth(), getHeight());
        overlayLayer.removeAll();
        overlayLayer.add(rewardPanel);
        overlayLayer.setVisible(true);
    }

    private void showInfoOverlay(String title, String body, Runnable onConfirm) {
        overlayLayer.removeAll();
        overlayLayer.setVisible(true);

        JPanel dim = makeDim();
        dim.setLayout(new GridBagLayout());

        JPanel card = makeCard(420, 240);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        card.add(cardTitle(title));
        card.add(Box.createVerticalStrut(14));
        card.add(cardBody(body));
        card.add(Box.createVerticalStrut(28));

        JButton btn = makeGoldOverlayButton("Continue  →");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> {
            overlayLayer.setVisible(false);
            overlayLayer.removeAll();
            if (onConfirm != null) onConfirm.run();
        });

        card.add(btn);
        dim.add(card);
        mountOverlay(dim);
    }

    private void showUltimateDraftOverlay(Runnable onDone) {
        List<Skill> pool = buildUltimatePool();
        if (pool.isEmpty()) { onDone.run(); return; }

        overlayLayer.removeAll(); overlayLayer.setVisible(true);
        JPanel dim = makeDim(); dim.setLayout(new GridBagLayout());

        int cardH = 140 + pool.size() * 54;
        JPanel card = makeCard(490, cardH);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel tl = new JLabel("🔥  ULTIMATE SKILL DRAFT", SwingConstants.CENTER);
        tl.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,22)); tl.setForeground(new Color(0xFF,0xAA,0x30));
        tl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sl = new JLabel("Choose an assist summon to aid your climb:", SwingConstants.CENTER);
        sl.setFont(new Font("Serif",Font.ITALIC,13)); sl.setForeground(LOG_FG);
        sl.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(tl); card.add(Box.createVerticalStrut(8)); card.add(sl); card.add(Box.createVerticalStrut(16));

        for (Skill skill : pool) {
            JButton btn = makeSkillPickButton(skill.getName(), skill.getMinDamage()+"–"+skill.getMaxDamage()+" dmg");
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> {
                playerCharacter.addSkill(skill);
                ultimateUnlocked = true;
                summonCharges = 3;

                for (Character c : getFullRoster()) {
                    if (c.getName().equals(playerCharacter.getName())) continue;
                    for (int k = 0; k < c.getSkills().size(); k++) {
                        if (c.getSkills().get(k).getName().equals(skill.getName())) {
                            assistCharacter = c;
                            String idle = c.getIdleAnimationPath();
                            String[] skillPaths = c.getSkillAnimationPaths();
                            int[] durs = c.getSkillAnimationDurations();

                            String sPath = (skillPaths != null && skillPaths.length > k) ? skillPaths[k] : null;
                            int sDur = (durs != null && durs.length > k) ? durs[k] : 1600;

                            assistDurations[0] = sDur;
                            assistAnimator = new CharacterAnimator(idle, new String[]{sPath}, assistDurations);
                            break;
                        }
                    }
                }

                rebuildSkillSlots();
                log("🔥  Assist Summon bound: " + (assistCharacter != null ? assistCharacter.getName() : "Unknown") + "!");
                overlayLayer.setVisible(false); overlayLayer.removeAll(); onDone.run();
            });
            card.add(btn); card.add(Box.createVerticalStrut(6));
        }
        dim.add(card); mountOverlay(dim);
    }

    private List<Skill> buildUltimatePool() {
        Character[] globalRoster = getFullRoster();
        String playerUltimateName = "";
        if (playerCharacter.getSkills().size() >= 3) playerUltimateName = playerCharacter.getSkills().get(2).getName();

        List<Skill> pool = new ArrayList<>();
        for (Character c : globalRoster) {
            if (c.getName().equals(playerCharacter.getName())) continue;
            if (c.getSkills().size() >= 3) {
                Skill ultimate = c.getSkills().get(2);
                if (!ultimate.getName().equals(playerUltimateName)) pool.add(ultimate);
            }
        }
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(3, pool.size()));
    }

    private void restoreAssistAnimator() {
        if (playerCharacter.getSkills().size() < 4) return;
        Skill drafted = playerCharacter.getSkills().get(3);

        for (Character c : getFullRoster()) {
            for (int k = 0; k < c.getSkills().size(); k++) {
                if (c.getSkills().get(k).getName().equals(drafted.getName())) {
                    assistCharacter = c;
                    String idle = c.getIdleAnimationPath();
                    String[] skillPaths = c.getSkillAnimationPaths();
                    int[] durs = c.getSkillAnimationDurations();

                    String sPath = (skillPaths != null && skillPaths.length > k) ? skillPaths[k] : null;
                    int sDur = (durs != null && durs.length > k) ? durs[k] : 1600;

                    assistDurations[0] = sDur;
                    assistAnimator = new CharacterAnimator(idle, new String[]{sPath}, assistDurations);
                    return;
                }
            }
        }
    }

    private Character[] getFullRoster() {
        return new Character[]{ new Tyrone(), new MakelanShere(), new Mary(), new Dirk(), new Flamara(), new Dea(), new Adamus(), new Tera() };
    }

    private void transitionToTower() { dispose(); new ArcadeTowerFrame(playerCharacter, arcadeManager); }

    // ── Defeat / Surrender Logic ──────────────────────────────
    private void onPlayerLost() {
        stopMatchTimer(); stopTurnTimer();
        setPlayerEnabled(false); processingTurn = false;

        if (playerAnimator != null) playerAnimator.dispose();
        if (enemyAnimator != null) enemyAnimator.toIdle();

        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("💀  " + enemyCharacter.getName() + " has defeated you!");

        new Timer(1200, e -> {
            ((Timer)e.getSource()).stop();
            showDefeatOptionsOverlay();
        }).start();
    }

    private void showDefeatOptionsOverlay() {
        overlayLayer.removeAll();
        overlayLayer.setVisible(true);

        JPanel dim = makeDim();
        dim.setLayout(new GridBagLayout());

        JPanel card = makeCard(500, 260);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        card.add(cardTitle("💀  FALLEN"));
        card.add(Box.createVerticalStrut(14));
        card.add(cardBody("Your strength wanes.\nWill you try again, or record your legacy and surrender?"));
        card.add(Box.createVerticalStrut(28));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnRow.setOpaque(false);

        JButton continueBtn = makeGoldOverlayButton("Continue (New Run)");
        continueBtn.addActionListener(e -> {
            dispose();
            new encantadia.ui.frames.CharacterSelectionFrame(encantadia.gamemode.GameModeType.ARCADE);
        });

        JButton surrenderBtn = makeGoldOverlayButton("Surrender & Record");
        surrenderBtn.addActionListener(e -> processSurrender());

        btnRow.add(continueBtn);
        btnRow.add(surrenderBtn);
        card.add(btnRow);

        dim.add(card);
        mountOverlay(dim);
    }

    private void processSurrender() {
        String tag = promptForTag();
        if (tag != null) {
            DatabaseManager.getInstance().saveRecord(
                    tag,
                    120,   // TODO: replace with actual time
                    5000,  // TODO: replace with actual damage dealt
                    4000,  // TODO: replace with actual damage received
                    false
            );
            dispose();
            new LeaderboardFrame();
        }
    }

    public String promptForTag() {
        while (true) {
            String tag = JOptionPane.showInputDialog(this, "Enter 3-Letter Player Tag:", "Record Stats", JOptionPane.PLAIN_MESSAGE);
            if (tag == null) return null;
            tag = tag.trim().toUpperCase();

            if (tag.length() != 3 || !tag.matches("[A-Z]{3}")) {
                JOptionPane.showMessageDialog(this, "Tag must be EXACTLY 3 letters (A-Z).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (DatabaseManager.getInstance().isNameTaken(tag)) {
                JOptionPane.showMessageDialog(this, "Tag '" + tag + "' is already taken! Choose another.", "Duplicate Tag", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return tag;
        }
    }

    // ── Timers ────────────────────────────────────────────────
    private void startTurnTimer() {
        if (timeUpTriggered) return;
        stopTurnTimer(); timeLeft = 10;
        turnCountdownTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            if (battleCanvas != null) battleCanvas.repaint();
            if (timeLeft <= 0) { stopTurnTimer(); SwingUtilities.invokeLater(this::onTurnTimerExpired); }
        });
        turnCountdownTimer.start();
    }

    private void stopTurnTimer() {
        if (turnCountdownTimer != null) { turnCountdownTimer.stop(); turnCountdownTimer = null; }
        timeLeft = 10;
        if (battleCanvas != null) battleCanvas.repaint();
    }

    private void onTurnTimerExpired() {
        if (processingTurn || timeUpTriggered) return;
        processingTurn = true; setPlayerEnabled(false);
        log("⏰  Time's up!  " + playerCharacter.getName() + "'s turn forfeited.");
        turnManager.advanceTurn(); refreshCdRow(); updateTurnState();

        Timer aiTrigger = new Timer(500, e -> {
            ((Timer)e.getSource()).stop();
            doEnemyTurn();
        });
        aiTrigger.setRepeats(false); aiTrigger.start();
    }

    private void startMatchTimer() {
        stopMatchTimer(); matchTimeLeft = MATCH_DURATION_SECONDS;
        matchTimer = new javax.swing.Timer(1000, e -> {
            matchTimeLeft--;
            if (battleCanvas != null) battleCanvas.repaint();
            if (matchTimeLeft <= 0) { stopMatchTimer(); SwingUtilities.invokeLater(this::onMatchTimerExpired); }
        });
        matchTimer.start();
        glowAnimTimer.start();
    }

    private void stopMatchTimer() {
        if (matchTimer != null) { matchTimer.stop(); matchTimer = null; }
        matchTimeLeft = MATCH_DURATION_SECONDS;
        if (battleCanvas != null) battleCanvas.repaint();
    }

    private void onMatchTimerExpired() {
        timeUpTriggered = true;
        stopTurnTimer(); stopMatchTimer();
        setPlayerEnabled(false); processingTurn = true;
        log("⏱  TIME OVER!");

        if (battleCanvas != null) battleCanvas.repaint();

        Timer judgeTimer = new Timer(2000, e -> {
            ((Timer)e.getSource()).stop();
            double pPct = (double)playerCharacter.getCurrentHP() / playerCharacter.getMaxHP();
            double ePct = (double)enemyCharacter.getCurrentHP() / enemyCharacter.getMaxHP();

            if (pPct > ePct) { log("🏆  " + playerCharacter.getName() + " wins by HP advantage!"); onPlayerWon(); }
            else if (ePct > pPct) { log("💀  " + enemyCharacter.getName() + " wins by HP advantage!"); onPlayerLost(); }
            else { log("⚖  Draw — " + enemyCharacter.getName() + " claims the round."); onPlayerLost(); }
        });
        judgeTimer.setRepeats(false); judgeTimer.start();
    }

    private void refreshUI() { if (battleCanvas != null) battleCanvas.repaint(); }

    private void refreshCdRow() {
        if (turnManager == null) return;
        List<Skill> skills = playerCharacter.getSkills();
        for (int i = 0; i < skillBtns.length; i++) {
            if (skillBtns[i] == null || i >= skills.size()) continue;

            if (i == 3) {
                if (summonCharges <= 0) {
                    cdLabels[i].setText("EXHAUSTED"); cdLabels[i].setForeground(RED_CRIT); skillBtns[i].setEnabled(false);
                } else {
                    int cd = turnManager.getCooldownManager().getRemainingCooldown(playerCharacter, i);
                    if (cd > 0) { cdLabels[i].setText(cd + " turn(s)"); cdLabels[i].setForeground(ORANGE_LOW); skillBtns[i].setEnabled(false); }
                    else        { cdLabels[i].setText("READY"); cdLabels[i].setForeground(new Color(0xFF,0xAA,0x30)); }
                }
            } else {
                int cd = turnManager.getCooldownManager().getRemainingCooldown(playerCharacter, i);
                if (cd > 0) { cdLabels[i].setText(cd + " turn(s)"); cdLabels[i].setForeground(ORANGE_LOW); skillBtns[i].setEnabled(false); }
                else        { cdLabels[i].setText("READY"); cdLabels[i].setForeground(GREEN_RDY); }
            }
        }
    }

    private void setPlayerEnabled(boolean on) {
        if (timeUpTriggered) on = false;
        List<Skill> skills = playerCharacter.getSkills();
        for (int i = 0; i < skillBtns.length; i++) {
            if (skillBtns[i] != null) {
                if (i == 3) skillBtns[i].setEnabled(on && ultimateUnlocked && summonCharges > 0);
                else        skillBtns[i].setEnabled(on && i < skills.size());
            }
        }
        if (on) refreshCdRow();
    }

    private void flushResult(TurnResult r) { for (String m : r.getLogMessages()) log(m); }
    private void log(String msg) { SwingUtilities.invokeLater(() -> { if (battleLog != null) { battleLog.append(msg + "\n"); battleLog.setCaretPosition(battleLog.getDocument().getLength()); } }); }
    private String getFrameImg(String name) { for (int i = 0; i < CHAR_NAMES.length; i++) if (CHAR_NAMES[i].equals(name)) return FRAME_IMGS[i]; return null; }

    protected void drawImageFill(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return; int iw = img.getWidth(null), ih = img.getHeight(null); if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih); int dw = (int) (iw * scale), dh = (int) (ih * scale); g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    protected void drawImageProportional(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return; int iw = img.getWidth(null), ih = img.getHeight(null); if (iw <= 0 || ih <= 0) return;
        double scale = Math.min((double) w / iw, (double) h / ih); int dw = (int) (iw * scale), dh = (int) (ih * scale); g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    private void registerHotkeys() {
        JComponent root = (JComponent) getContentPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        int[][] coreKeys = {{KeyEvent.VK_A,0},{KeyEvent.VK_S,1},{KeyEvent.VK_D,2}};
        for (int[] kb : coreKeys) {
            int kc=kb[0]; int si=kb[1]; String id="arc_skill_"+si;
            im.put(KeyStroke.getKeyStroke(kc,0,false),id);
            am.put(id,new AbstractAction(){@Override public void actionPerformed(ActionEvent e){if(si<skillBtns.length&&skillBtns[si]!=null&&skillBtns[si].isEnabled())onPlayerSkill(si);}});
        }
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F,0,false),"arc_skill_3");
        am.put("arc_skill_3",new AbstractAction(){@Override public void actionPerformed(ActionEvent e){if(!ultimateUnlocked)return;if(skillBtns[3]!=null&&skillBtns[3].isEnabled())onPlayerSkill(3);}});
    }

    // ── BattleCanvas ──────────────────────────────────────────
    private class BattleCanvas extends JPanel {
        private final Image p1Frame, p2Frame;
        private final boolean isBoss;
        private final int idx, total;
        private boolean p1Active = true;
        private int hudBottomY = 0;

        BattleCanvas(boolean isBoss, int idx, int total) {
            setOpaque(false);
            this.isBoss = isBoss; this.idx = idx; this.total = total;
            p1Frame = loadImage(getFrameImg(playerCharacter.getName()));
            p2Frame = loadImage(getFrameImg(enemyCharacter.getName()));
        }

        void setPlayerActive(boolean v) { p1Active = v; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int W = getWidth(), H = getHeight();
            double sc = Math.min(W / 1024.0, H / 768.0);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            String ctTxt = isBoss ? "⚡  FINAL BOSS  ⚡" : "Enemy  " + idx + "  of  " + total;
            Color  ctClr = isBoss ? BOSS_CLR : GOLD;
            g2.setFont(new Font("Serif", Font.BOLD|Font.ITALIC, Math.max(14,(int)(18*sc))));
            FontMetrics fm = g2.getFontMetrics();

            int tabW = fm.stringWidth(ctTxt) + (int)(48*sc);
            int tabH = fm.getHeight() + (int)(16*sc);
            int tabX = (W - tabW) / 2;
            int tabY = (int)(6*sc);

            g2.setColor(new Color(0x08,0x04,0x02,200));
            g2.fillRoundRect(tabX, tabY, tabW, tabH, tabH, tabH);
            g2.setColor(new Color(ctClr.getRed(),ctClr.getGreen(),ctClr.getBlue(),120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(tabX, tabY, tabW, tabH, tabH, tabH);

            int textX = tabX + (tabW - fm.stringWidth(ctTxt))/2;
            int textY = tabY + fm.getAscent() + (int)(6*sc);
            drawShadow(g2, ctTxt, textX, textY, ctClr);

            hudBottomY = tabY + tabH;

            drawFlankingTimers(g2, W, tabX, tabY, tabW, tabH, sc);

            int portW=(int)(82*sc),portH=(int)(82*sc),hpW=(int)(230*sc),hpH=(int)(16*sc);
            int pillW=(int)(140*sc),pillH=(int)(24*sc),portY = hudBottomY + (int)(8*sc);

            int ppx=(int)(10*sc);
            drawPortrait(g2,p1Frame,ppx,portY,portW,portH,PLAYER_CLR,p1Active);
            int phx=ppx+portW+(int)(8*sc),phy=portY+(int)(8*sc);
            drawHPBar(g2,phx,phy,hpW,hpH,playerCharacter,PLAYER_CLR);
            g2.setFont(new Font("SansSerif",Font.PLAIN,Math.max(8,(int)(10*sc))));
            drawShadow(g2,"HP: "+playerCharacter.getCurrentHP()+" / "+playerCharacter.getMaxHP(),phx,phy+hpH+(int)(10*sc),new Color(0xFF,0xF5,0xDC,190));
            drawNamePill(g2,playerCharacter.getName(),phx,phy+hpH+(int)(14*sc),pillW,pillH,new Color(0x20,0x50,0x20,215),new Color(0x80,0xFF,0xAA));
            g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(9,(int)(11*sc))));
            drawShadow(g2,"PLAYER",phx,phy-(int)(3*sc),new Color(0xEE,0xEE,0xEE));

            Color ec=isBoss?BOSS_CLR:ENEMY_CLR;
            int epx=W-(int)(10*sc)-portW;
            drawPortrait(g2,p2Frame,epx,portY,portW,portH,ec,!p1Active);
            int ehx=epx-hpW-(int)(8*sc),ehy=portY+(int)(8*sc);
            drawHPBar(g2,ehx,ehy,hpW,hpH,enemyCharacter,ec);
            g2.setFont(new Font("SansSerif",Font.PLAIN,Math.max(8,(int)(10*sc))));
            drawShadow(g2,"HP: "+enemyCharacter.getCurrentHP()+" / "+enemyCharacter.getMaxHP(),ehx,ehy+hpH+(int)(10*sc),new Color(0xFF,0xF5,0xDC,190));
            Color pillBg=isBoss?new Color(0x30,0x00,0x40,215):new Color(0x50,0x10,0x10,215);
            Color pillFg=isBoss?new Color(0xDD,0x88,0xFF):new Color(0xFF,0x77,0x77);
            drawNamePill(g2,enemyCharacter.getName(),ehx,ehy+hpH+(int)(14*sc),pillW,pillH,pillBg,pillFg);
            g2.setFont(new Font("SansSerif",Font.BOLD,Math.max(9,(int)(11*sc))));
            drawShadow(g2,isBoss?"⚡ BOSS":"ENEMY",ehx,ehy-(int)(3*sc),new Color(0xEE,0xEE,0xEE));

            String tt=p1Active?"▶ Your Turn":"⏳ "+enemyCharacter.getName()+" is acting...";
            g2.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,Math.max(10,(int)(13*sc))));
            fm=g2.getFontMetrics();
            drawShadow(g2,tt,(W-fm.stringWidth(tt))/2,hudBottomY+(int)(4*sc),GOLD);

            int groundY    = H - (int)(170 * sc);
            int spriteSize = (int)(220 * sc);

            int p1SpriteX = (int)(W * 0.15) - (playerAnimator != null ? playerAnimator.getKnockbackOffset() : 0);
            int p2SpriteX = (int)(W * 0.85) - spriteSize + (enemyAnimator != null ? enemyAnimator.getKnockbackOffset() : 0);

            if (isSummoning && assistAnimator != null) {
                Shape savedClip = g2.getClip();
                assistAnimator.draw(g2, p1SpriteX, groundY - spriteSize, spriteSize, spriteSize, this);
                g2.setClip(savedClip);
            } else if (playerAnimator != null) {
                Shape savedClip = g2.getClip();
                playerAnimator.draw(g2, p1SpriteX, groundY - spriteSize, spriteSize, spriteSize, this);
                g2.setClip(savedClip);
            }

            if (playerFlashAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, playerFlashAlpha * 0.65f));
                g2.setColor(new Color(255, 20, 20));
                g2.fillOval(p1SpriteX - spriteSize/4, groundY - spriteSize + spriteSize/4, (int)(spriteSize*1.5), spriteSize);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            int eSize = isBoss ? (int)(spriteSize * 1.45) : spriteSize;
            int eX    = (int)(W * 0.85) - eSize + (enemyAnimator != null ? enemyAnimator.getKnockbackOffset() : 0);
            int eY    = groundY - eSize;

            if (isBoss) drawBossAura(g2, eX + eSize/2, eY + eSize/2, eSize);

            if (enemyAnimator != null) {
                AffineTransform oldTransform = g2.getTransform();
                int cx = eX + eSize / 2;
                g2.translate(cx, 0); g2.scale(-1, 1); g2.translate(-cx, 0);
                enemyAnimator.draw(g2, eX, eY, eSize, eSize, this);
                g2.setTransform(oldTransform);
            }

            if (enemyFlashAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, enemyFlashAlpha * 0.65f));
                g2.setColor(new Color(255, 20, 20));
                g2.fillOval(eX - eSize/4, eY + eSize/4, (int)(eSize*1.5), eSize);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            if (timeUpTriggered) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, W, H);
                g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, Math.max(50, (int)(70*sc))));
                FontMetrics tfm = g2.getFontMetrics();
                String tMsg = "TIME UP";
                int tx = (W - tfm.stringWidth(tMsg)) / 2;
                int ty = H / 2;
                g2.setColor(new Color(0, 0, 0, 200)); g2.drawString(tMsg, tx + 4, ty + 4);
                g2.setColor(GOLD); g2.drawString(tMsg, tx, ty);
            }

            g2.dispose();
        }

        private void drawBossAura(Graphics2D g2, int cx, int cy, int size) {
            float pulse = (float)(0.8 + 0.2 * Math.sin(glowTick * 2.0));
            int auraSize = (int)(size * 1.4 * pulse);
            RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point(cx, cy), auraSize / 2f,
                    new float[]{0f, 0.4f, 1f},
                    new Color[]{ new Color(150, 0, 255, 120), new Color(80, 0, 150, 60), new Color(0, 0, 0, 0) }
            );
            g2.setPaint(rgp);
            g2.fillOval(cx - auraSize/2, cy - auraSize/2, auraSize, auraSize);
        }

        private void drawFlankingTimers(Graphics2D g2, int W, int tabX, int tabY, int tabW, int tabH, double sc) {
            int matchFontSz = Math.max(16, (int)(22 * sc));
            g2.setFont(new Font("Monospaced", Font.BOLD, matchFontSz));
            FontMetrics mfm = g2.getFontMetrics();

            int mins = matchTimeLeft / 60;
            int secs = matchTimeLeft % 60;
            String matchTxt = String.format("%02d:%02d", mins, secs);

            Color matchColor = matchTimeLeft > 40 ? GOLD
                    : matchTimeLeft > 15 ? ORANGE_LOW : RED_CRIT;

            int mPadH = (int)(16 * sc), mPadV = (int)(5 * sc);
            int mW = mfm.stringWidth(matchTxt) + mPadH * 2;
            int mH = mfm.getHeight() + mPadV * 2;
            int mX = Math.max((int)(6*sc), tabX - mW - (int)(8*sc));
            int mY = tabY + (tabH - mH) / 2;
            if (mX < (int)(4*sc)) mX = (int)(4*sc);

            g2.setColor(new Color(0x06, 0x03, 0x01, 225));
            g2.fillRoundRect(mX, mY, mW, mH, 12, 12);
            float mAlpha = matchTimeLeft <= 15 ? 130 + 125 * (float) Math.abs(Math.sin(glowTick * 2.5f)) : 180;
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(matchColor.getRed(), matchColor.getGreen(), matchColor.getBlue(), Math.min(255, (int) mAlpha)));
            g2.drawRoundRect(mX, mY, mW, mH, 12, 12);
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255, 255, 255, 18));
            g2.drawRoundRect(mX + 2, mY + 2, mW - 4, mH - 4, 10, 10);
            int mTX = mX + (mW - mfm.stringWidth(matchTxt)) / 2;
            int mTY = mY + mPadV + mfm.getAscent();
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(matchTxt, mTX + 1, mTY + 1);
            g2.setColor(matchColor);
            g2.drawString(matchTxt, mTX, mTY);

            if (turnCountdownTimer != null && turnCountdownTimer.isRunning()) {
                int turnFontSz = Math.max(11, (int)(14 * sc));
                g2.setFont(new Font("Serif", Font.BOLD, turnFontSz));
                FontMetrics tfm = g2.getFontMetrics();

                Color turnColor = timeLeft > 6 ? new Color(0xEE, 0xEE, 0xEE)
                        : timeLeft > 3 ? ORANGE_LOW : RED_CRIT;
                String turnTxt = "TURN  " + timeLeft;

                int tPadH = (int)(14 * sc), tPadV = (int)(4 * sc);
                int tW = tfm.stringWidth(turnTxt) + tPadH * 2;
                int tH = tfm.getHeight() + tPadV * 2;
                int tX = tabX + tabW + (int)(8*sc);
                if (tX + tW > W - (int)(4*sc)) tX = W - tW - (int)(4*sc);
                int tY = tabY + (tabH - tH) / 2;

                g2.setColor(new Color(0x06, 0x03, 0x01, 215));
                g2.fillRoundRect(tX, tY, tW, tH, tH, tH);
                float tAlpha = timeLeft <= 3 ? 120 + 135 * (float) Math.abs(Math.sin(glowTick * 3)) : 150;
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(turnColor.getRed(), turnColor.getGreen(), turnColor.getBlue(), Math.min(255, (int) tAlpha)));
                g2.drawRoundRect(tX, tY, tW, tH, tH, tH);
                int tTX = tX + (tW - tfm.stringWidth(turnTxt)) / 2;
                int tTY = tY + tPadV + tfm.getAscent();
                g2.setColor(new Color(0, 0, 0, 130));
                g2.drawString(turnTxt, tTX + 1, tTY + 1);
                g2.setColor(turnColor);
                g2.drawString(turnTxt, tTX, tTY);
            }
        }

        private void drawPortrait(Graphics2D g2, Image img, int x, int y, int w, int h, Color accent, boolean active) {
            if (active) {
                float a = 0.22f + 0.16f * (float)Math.sin(glowTick);
                for (int r = 5; r >= 1; r--) {
                    int sp = r*3;
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),Math.min(255,(int)(a*80/r))));
                    g2.setStroke(new BasicStroke(sp));
                    g2.drawRoundRect(x-sp/2,y-sp/2,w+sp,h+sp,10,10);
                }
            }
            g2.setColor(new Color(0x08,0x05,0x02,210));
            g2.fillRoundRect(x,y,w,h,8,8);
            if (img != null) {
                double scale = Math.min((double) w / img.getWidth(null), (double) h / img.getHeight(null));
                int dw = (int) (img.getWidth(null) * scale), dh = (int) (img.getHeight(null) * scale);
                g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),active?220:90));
            g2.drawRoundRect(x,y,w,h,8,8);
        }

        private void drawHPBar(Graphics2D g2, int x, int y, int w, int h, Character c, Color base) {
            double pct = Math.max(0, Math.min(1.0, (double)c.getCurrentHP() / c.getMaxHP()));
            Color bar = pct <= 0.25 ? RED_CRIT : pct <= 0.50 ? ORANGE_LOW : base;
            g2.setColor(new Color(0x08,0x04,0x02,220));
            g2.fillRoundRect(x,y,w,h,h,h);
            int fw = (int)(w*pct);
            if (fw > 2) {
                g2.setPaint(new GradientPaint(x,y,bar.brighter(),x,y+h,bar.darker()));
                g2.fillRoundRect(x,y,fw,h,h,h);
            }
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(0xFF,0xFF,0xFF,50));
            g2.drawRoundRect(x,y,w,h,h,h);
        }

        private void drawNamePill(Graphics2D g2, String t, int x, int y, int w, int h, Color bg, Color fg) {
            g2.setColor(bg);
            g2.fillRoundRect(x,y,w,h,h,h);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(new Color(fg.getRed(),fg.getGreen(),fg.getBlue(),130));
            g2.drawRoundRect(x,y,w,h,h,h);
            g2.setFont(new Font("Serif",Font.BOLD,Math.max(9,h-6)));
            FontMetrics fm = g2.getFontMetrics();
            int tx = x+(w-fm.stringWidth(t))/2, ty = y+(h+fm.getAscent()-fm.getDescent())/2;
            g2.setColor(new Color(0,0,0,100));
            g2.drawString(t,tx+1,ty+1);
            g2.setColor(fg);
            g2.drawString(t,tx,ty);
        }

        private void drawShadow(Graphics2D g2, String t, int x, int y, Color c) {
            g2.setColor(new Color(0,0,0,150));
            g2.drawString(t,x+1,y+1);
            g2.setColor(c);
            g2.drawString(t,x,y);
        }
    }

    private JButton makePillButton(String label, Color bg, Color fg) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h=getModel().isRollover(),en=isEnabled();
                Color bc=en?(h?bg.brighter():bg):new Color(0x28,0x20,0x18,140);
                g2.setColor(new Color(bc.getRed(),bc.getGreen(),bc.getBlue(),210));g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                g2.setStroke(new BasicStroke(1.5f));g2.setColor(new Color(fg.getRed(),fg.getGreen(),fg.getBlue(),en?(h?255:180):60));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,getHeight()-2,getHeight()-2);
                g2.setFont(new Font("Serif",Font.BOLD,Math.max(9,getHeight()-8)));FontMetrics fm=g2.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(getText()))/2,ty=(getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.setColor(new Color(0,0,0,90));g2.drawString(getText(),tx+1,ty+1);
                g2.setColor(en?fg:new Color(0x60,0x50,0x40));g2.drawString(getText(),tx,ty);g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);btn.setBorderPainted(false);btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeGoldOverlayButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h=getModel().isRollover();
                g2.setPaint(new GradientPaint(0,0,h?new Color(170,110,40):new Color(120,70,20),0,getHeight(),h?new Color(120,80,30):new Color(80,40,10)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                if(h){g2.setColor(new Color(255,215,120,80));g2.setStroke(new BasicStroke(2.5f));g2.drawRoundRect(2,2,getWidth()-4,getHeight()-4,14,14);}
                g2.setColor(new Color(220,180,90));g2.setStroke(new BasicStroke(1.5f));g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,14,14);
                g2.setFont(new Font("Serif",Font.BOLD,14));FontMetrics fm=g2.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(getText()))/2,ty=(getHeight()+fm.getAscent())/2-2;
                g2.setColor(Color.BLACK);g2.drawString(getText(),tx+1,ty+1);
                g2.setColor(new Color(255,230,170));g2.drawString(getText(),tx,ty);g2.dispose();
            }
        };
        btn.setOpaque(false);btn.setContentAreaFilled(false);btn.setBorderPainted(false);btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));btn.setPreferredSize(new Dimension(200,46));
        return btn;
    }

    private JButton makeSkillPickButton(String name, String stat) {
        JButton btn = new JButton(name + "   [" + stat + "]") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h=getModel().isRollover();
                g2.setColor(h?new Color(0x3C,0x18,0x04,240):new Color(0x22,0x0E,0x02,220));g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setStroke(new BasicStroke(1.5f));g2.setColor(h?new Color(0xFF,0xAA,0x30):new Color(0xC8,0x70,0x10,160));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,10,10);
                g2.setFont(new Font("Serif",Font.BOLD,13));FontMetrics fm=g2.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(getText()))/2,ty=(getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.setColor(new Color(0,0,0,80));g2.drawString(getText(),tx+1,ty+1);
                g2.setColor(h?new Color(0xFF,0xE0,0x80):CREAM);g2.drawString(getText(),tx,ty);g2.dispose();
            }
        };
        btn.setOpaque(false);btn.setContentAreaFilled(false);btn.setBorderPainted(false);btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(400,42));btn.setMaximumSize(new Dimension(400,42));
        return btn;
    }

    private Image loadImage(String path) {
        if (path == null) return null;
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    private JPanel makeDim() {
        JPanel dim=new JPanel(){@Override protected void paintComponent(Graphics g){super.paintComponent(g);Graphics2D g2=(Graphics2D)g.create();g2.setColor(new Color(0,0,0,180));g2.fillRect(0,0,getWidth(),getHeight());g2.dispose();}};
        dim.setOpaque(false); return dim;
    }
    private JPanel makeCard(int w,int h){
        JPanel card=new JPanel(){@Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(0x0E,0x09,0x04,248));g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);g2.setStroke(new BasicStroke(2f));g2.setColor(GOLD);g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,20,20);g2.dispose();super.paintComponent(g);}};
        card.setOpaque(false);card.setPreferredSize(new Dimension(w,h));card.setMaximumSize(new Dimension(w,h));return card;
    }
    private JLabel cardTitle(String text){JLabel l=new JLabel(text,SwingConstants.CENTER);l.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,22));l.setForeground(GOLD);l.setAlignmentX(Component.CENTER_ALIGNMENT);return l;}
    private JLabel cardBody(String text){String html="<html><div style='text-align:center;color:#D4C5A0;font-size:13px;font-family:serif'>"+text.replace("\n","<br>")+"</div></html>";JLabel l=new JLabel(html,SwingConstants.CENTER);l.setAlignmentX(Component.CENTER_ALIGNMENT);return l;}
    private void mountOverlay(JPanel dim){
        overlayLayer.add(dim); dim.setBounds(0,0,getWidth(),getHeight());
        overlayLayer.addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent e){dim.setBounds(0,0,overlayLayer.getWidth(),overlayLayer.getHeight());}});
        overlayLayer.revalidate(); overlayLayer.repaint();
    }

    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawImageFill(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}