package encantadia.ui.frames.battleModeFrames;

import encantadia.ScreenManager;
import encantadia.battle.ai.EnemyAI;
import encantadia.battle.engine.TurnManager;
import encantadia.battle.result.TurnResult;
import encantadia.battle.skill.Skill;
import encantadia.characters.Character;
import encantadia.characters.animation.CharacterAnimator;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.List;

public class PVEBattleFrame extends JFrame {

    private javax.swing.Timer turnCountdownTimer;
    private int               timeLeft = 10;
    private static final int  MATCH_DURATION_SECONDS = 60;
    private javax.swing.Timer matchTimer;
    private int               matchTimeLeft = MATCH_DURATION_SECONDS;

    private static final String BATTLE_BG = "/resources/backgroundPve.png";
    private static final String[] ROUND_TABLETS = { "/resources/round1.png", "/resources/round2.png", "/resources/round3.png" };
    private static final String[] ROUND_TEXTS = { "/resources/round1Text.png", "/resources/round2Text.png", "/resources/round3Text.png" };
    private static final String[] FRAME_IMGS = { "/resources/tyroneFrame (1).png", "/resources/elanFrame (1).png", "/resources/claireFrame (1).png", "/resources/dirkFrame (1).png", "/resources/flamaraFrame (1).png", "/resources/deaFrame (1).png", "/resources/adamusFrame (1).png",  "/resources/teraFrame (1).png" };
    private static final String[] CHAR_NAMES = { "Tyrone","Makelan Shere","Claire","Dirk","Flamara","Dea","Adamus","Tera" };

    private static final int ROUNDS_TO_WIN    = 2;
    private static final int ENEMY_TURN_DELAY = 1100;

    private static final Color PLAYER_CLR = new Color(0x2E,0x8B,0x57);
    private static final Color ENEMY_CLR  = new Color(0xB0,0x2A,0x2A);
    private static final Color GOLD       = new Color(0xC8,0xA0,0x28);
    private static final Color CREAM      = new Color(0xFF,0xF5,0xDC);
    private static final Color LOG_FG     = new Color(0xD4,0xC5,0xA0);
    private static final Color ORANGE_LOW = new Color(0xCC,0x88,0x22);
    private static final Color RED_CRIT   = new Color(0xCC,0x22,0x22);
    private static final Color GREEN_RDY  = new Color(0x60,0xCC,0x60);
    private static final Color BORDER_CLR = new Color(0xC8,0xA0,0x28);

    private final Character playerCharacter;
    private final Character enemyCharacter;
    private TurnManager     turnManager;

    private CharacterAnimator playerAnimator;
    private CharacterAnimator enemyAnimator;
    private float playerFlashAlpha = 0f;
    private float enemyFlashAlpha  = 0f;

    private int     playerWins   = 0;
    private int     enemyWins    = 0;
    private int     currentRound = 1;
    private volatile boolean processingTurn = false;
    private volatile boolean timeUpTriggered = false; // Time Over lock

    private BattleCanvas battleCanvas;
    private JPanel       skillsLayer;
    private JPanel       overlayLayer;
    private RoundOverlay roundOverlay;
    private JTextArea    battleLog;
    private JButton[]    skillBtns = new JButton[3];
    private JLabel[]     cdLabels  = new JLabel[3];

    public PVEBattleFrame(Character playerCharacter, Character enemyCharacter) {
        this.playerCharacter = playerCharacter;
        this.enemyCharacter  = enemyCharacter;
        this.turnManager     = new TurnManager(playerCharacter, enemyCharacter);
        this.playerAnimator = CharacterAnimator.forCharacter(playerCharacter);
        this.enemyAnimator  = CharacterAnimator.forCharacter(enemyCharacter);

        setTitle("PVE — " + playerCharacter.getName() + " vs " + enemyCharacter.getName());
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        buildUI();
        registerHotkeys();
        setVisible(true);
        ScreenManager.register(this);

        refreshUI(); refreshCdRow(); updateTurnState();
        showRoundAnnouncement(currentRound);
        log("⚔  Round " + currentRound + " — First to " + ROUNDS_TO_WIN + " wins!");
        log(playerCharacter.getName() + "  vs  " + enemyCharacter.getName());
        startMatchTimer();
    }

    @Override
    public void dispose() {
        stopMatchTimer();
        stopTurnTimer();
        if (playerAnimator != null) playerAnimator.dispose();
        if (enemyAnimator != null) enemyAnimator.dispose();
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void onPlayerSkill(int si) {
        if (timeUpTriggered) return;
        stopTurnTimer();
        if (matchTimer != null && matchTimer.isRunning()) matchTimer.stop(); // Animation Suspension
        if (processingTurn || !turnManager.isPlayerTurn()) return;

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
            if (enemyAnimator != null) enemyAnimator.triggerHit(); // Kinetic Feedback
            refreshUI();

            Timer recoveryTimer = new Timer((durationMs - impactDelay) + 150, ev -> {
                ((Timer)ev.getSource()).stop();
                if (matchTimeLeft > 0 && !timeUpTriggered) matchTimer.start(); // Resume Timer

                if (res.isTargetDefeated()) { endRound(true); return; }
                if (res.isTurnStolen())     { setPlayerEnabled(true); processingTurn = false; return; }

                turnManager.advanceTurn();
                refreshCdRow();
                updateTurnState();

                Timer aiTrigger = new Timer(500, et -> {
                    ((Timer)et.getSource()).stop();
                    doEnemyTurn();
                });
                aiTrigger.setRepeats(false);
                aiTrigger.start();
            });
            recoveryTimer.setRepeats(false); recoveryTimer.start();
        });
        impactTimer.setRepeats(false); impactTimer.start();
    }

    private void doEnemyTurn() {
        if (timeUpTriggered) return;
        if (matchTimer != null && matchTimer.isRunning()) matchTimer.stop(); // Animation Suspension

        int si = EnemyAI.chooseSkill(enemyCharacter, turnManager.getCooldownManager());
        if (enemyAnimator != null) enemyAnimator.toSkill(si);

        int durationMs  = getSkillDuration(enemyCharacter, si);
        int impactDelay = (int)(durationMs * 0.65);

        Timer impactTimer = new Timer(impactDelay, e -> {
            ((Timer)e.getSource()).stop();
            TurnResult res = turnManager.executeSkill(enemyCharacter, playerCharacter, si);
            flushResult(res);

            playerFlashAlpha = 1.0f;
            if (playerAnimator != null) playerAnimator.triggerHit(); // Kinetic Feedback
            refreshUI();

            Timer recoveryTimer = new Timer((durationMs - impactDelay) + 150, ev -> {
                ((Timer)ev.getSource()).stop();
                if (matchTimeLeft > 0 && !timeUpTriggered) matchTimer.start(); // Resume Timer

                if (res.isTargetDefeated()) { endRound(false); return; }
                if (res.isTurnStolen()) {
                    Timer aiTrigger = new Timer(500, et -> {
                        ((Timer)et.getSource()).stop();
                        doEnemyTurn();
                    });
                    aiTrigger.setRepeats(false); aiTrigger.start();
                    return;
                }

                turnManager.advanceTurn();
                refreshCdRow();
                updateTurnState();
                setPlayerEnabled(true);
                processingTurn = false;
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

    private void buildUI() {
        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BATTLE_BG);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        battleCanvas = new BattleCanvas();
        lp.add(battleCanvas, JLayeredPane.PALETTE_LAYER);

        skillsLayer = new JPanel(null);
        skillsLayer.setOpaque(false);
        buildSkillsLayer();
        lp.add(skillsLayer, JLayeredPane.MODAL_LAYER);

        overlayLayer = new JPanel(null);
        overlayLayer.setOpaque(false);
        overlayLayer.setVisible(false);
        lp.add(overlayLayer, JLayeredPane.POPUP_LAYER);

        roundOverlay = new RoundOverlay();
        roundOverlay.setVisible(false);
        lp.add(roundOverlay, JLayeredPane.POPUP_LAYER);

        lp.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int W = lp.getWidth(), H = lp.getHeight();
                if (W == 0 || H == 0) return;
                bg.setBounds(0, 0, W, H);
                battleCanvas.setBounds(0, 0, W, H);
                skillsLayer.setBounds(0, 0, W, H);
                overlayLayer.setBounds(0, 0, W, H);
                roundOverlay.setBounds(0, 0, W, H);
                layoutSkillsLayer(W, H);
            }
        });
    }

    private void buildSkillsLayer() {
        List<Skill> skills = playerCharacter.getSkills();
        JPanel skillPanel = new JPanel();
        skillPanel.setOpaque(false);
        skillPanel.setLayout(new BoxLayout(skillPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < 3; i++) {
            final int si = i;
            String n = (i < skills.size()) ? skills.get(i).getName() : "—";
            skillBtns[i] = makePillButton(n, new Color(0x70,0x14,0x14), new Color(0xFF,0x99,0x99));
            skillBtns[i].setEnabled(false);
            skillBtns[i].addActionListener(e -> onPlayerSkill(si));
            cdLabels[i] = new JLabel("READY", SwingConstants.CENTER);
            cdLabels[i].setFont(new Font("SansSerif", Font.BOLD, 9));
            cdLabels[i].setForeground(GREEN_RDY);
            JPanel slot = new JPanel(new BorderLayout(0, 2));
            slot.setOpaque(false);
            slot.add(skillBtns[i], BorderLayout.CENTER);
            slot.add(cdLabels[i], BorderLayout.SOUTH);
            skillPanel.add(slot);
            if (i < 2) skillPanel.add(Box.createVerticalStrut(4));
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
                g2.setColor(new Color(0, 0, 0, 145)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(0xC8, 0xA0, 0x28, 70)); g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose(); super.paintComponent(g);
            }
        };
        logHolder.setOpaque(false); logHolder.add(scroll, BorderLayout.CENTER);
        skillsLayer.add(logHolder); skillsLayer.putClientProperty("log", logHolder);
    }

    private void layoutSkillsLayer(int W, int H) {
        JPanel sp = (JPanel) skillsLayer.getClientProperty("skills");
        JPanel lh = (JPanel) skillsLayer.getClientProperty("log");
        if (sp == null) return;
        double sc = Math.min(W / 1024.0, H / 768.0);
        int skillW = (int)(170*sc), skillH = (int)(112*sc);
        sp.setBounds((int)(10*sc), H - skillH - (int)(16*sc), skillW, skillH);
        if (lh != null) {
            int lw = (int)(380*sc), lhh = (int)(130*sc);
            lh.setBounds((W - lw) / 2, H - lhh - (int)(16*sc), lw, lhh);
        }
        skillsLayer.revalidate();
        skillsLayer.repaint();
    }

    private void registerHotkeys() {
        JComponent root = (JComponent) getContentPane();
        InputMap  im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        Object[][] bindings = { { KeyEvent.VK_A, 0, "pve_skill_0" }, { KeyEvent.VK_S, 1, "pve_skill_1" }, { KeyEvent.VK_D, 2, "pve_skill_2" } };
        for (Object[] b : bindings) {
            int    keyCode = (int) b[0];
            int    si      = (int) b[1];
            String id      = (String) b[2];
            im.put(KeyStroke.getKeyStroke(keyCode, 0, false), id);
            am.put(id, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (si < skillBtns.length && skillBtns[si] != null && skillBtns[si].isEnabled()) onPlayerSkill(si);
                }
            });
        }
    }

    private void updateTurnState() {
        if (timeUpTriggered) return;
        boolean pt = turnManager.isPlayerTurn();
        setPlayerEnabled(pt);
        if (battleCanvas != null) battleCanvas.setPlayerActive(pt);
        if (pt) startTurnTimer(); else stopTurnTimer();
    }

    private void endRound(boolean playerWon) {
        stopMatchTimer(); stopTurnTimer();
        setPlayerEnabled(false);
        if (playerAnimator != null) playerAnimator.toIdle();
        if (enemyAnimator != null) enemyAnimator.toIdle();

        if (playerWon) { playerWins++; log("🏆  " + playerCharacter.getName() + " wins Round " + currentRound + "!"); }
        else           { enemyWins++;  log("💀  " + enemyCharacter.getName() + " wins Round " + currentRound + "!"); }

        if (battleCanvas != null) battleCanvas.repaint();
        if (playerWins >= ROUNDS_TO_WIN || enemyWins >= ROUNDS_TO_WIN) {
            Timer t = new Timer(1600, e -> { ((Timer)e.getSource()).stop(); showMatchResult(playerWins >= ROUNDS_TO_WIN); });
            t.setRepeats(false); t.start();
        } else {
            currentRound++;
            Timer t = new Timer(2000, e -> { ((Timer)e.getSource()).stop(); startNextRound(); });
            t.setRepeats(false); t.start();
        }
    }

    private void startNextRound() {
        timeUpTriggered = false;
        stopTurnTimer();
        fullHeal(playerCharacter); fullHeal(enemyCharacter);
        turnManager = new TurnManager(playerCharacter, enemyCharacter);
        refreshUI(); refreshCdRow(); processingTurn = false; updateTurnState();
        showRoundAnnouncement(currentRound);
        log("⚔  Round " + currentRound + " begins!");
        startMatchTimer();
    }

    private void showRoundAnnouncement(int round) {
        if (roundOverlay != null) roundOverlay.show(round);
    }

    private void showMatchResult(boolean playerWon) {
        String winner = playerWon ? playerCharacter.getName() : enemyCharacter.getName();
        log("★  MATCH OVER — " + winner + " wins " + Math.max(playerWins, enemyWins) + "–" + Math.min(playerWins, enemyWins) + "!");

        boolean isVictory = playerWon;
        String title = isVictory ? "🏆  VICTORY" : "💀  DEFEAT";
        String msg = isVictory
                ? playerCharacter.getName() + " claims victory!\n\n" + playerWins + "–" + enemyWins
                : enemyCharacter.getName() + " proved superior.\n\n" + enemyWins + "–" + playerWins;

        showMatchResultOverlay(title, msg, true, () -> {
            dispose();
            fullHeal(playerCharacter); fullHeal(enemyCharacter);
            new PVEBattleFrame(playerCharacter, enemyCharacter);
        }, () -> { dispose(); new MainMenuFrame(); });
    }

    private void showMatchResultOverlay(String title, String msg, boolean showRematch, Runnable onRematch, Runnable onExit) {
        overlayLayer.removeAll(); overlayLayer.setVisible(true);
        JPanel dim = makeDim(); dim.setLayout(new GridBagLayout());
        JPanel card = makeCard(420, 260);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        card.add(cardTitle(title)); card.add(Box.createVerticalStrut(14));
        card.add(cardBody(msg)); card.add(Box.createVerticalStrut(28));

        JPanel btnRow = new JPanel(); btnRow.setOpaque(false); btnRow.setLayout(new BoxLayout(btnRow, BoxLayout.X_AXIS));
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton exitBtn = makeGoldOverlayButton("Main Menu");
        exitBtn.addActionListener(e -> { overlayLayer.setVisible(false); overlayLayer.removeAll(); onExit.run(); });
        btnRow.add(exitBtn);

        if (showRematch) {
            btnRow.add(Box.createHorizontalStrut(16));
            JButton rematchBtn = makeGoldOverlayButton("Rematch");
            rematchBtn.addActionListener(e -> { overlayLayer.setVisible(false); overlayLayer.removeAll(); onRematch.run(); });
            btnRow.add(rematchBtn);
        }

        card.add(btnRow); dim.add(card); mountOverlay(dim);
    }

    // ── Timers ──────────────────────────────────────────────────
    private void startTurnTimer() {
        if (timeUpTriggered) return;
        stopTurnTimer(); timeLeft = 10;
        turnCountdownTimer = new javax.swing.Timer(1000, e -> {
            timeLeft--; if (battleCanvas != null) battleCanvas.repaint();
            if (timeLeft <= 0) { stopTurnTimer(); SwingUtilities.invokeLater(this::onTurnTimerExpired); }
        });
        turnCountdownTimer.start();
    }

    private void stopTurnTimer() {
        if (turnCountdownTimer != null) { turnCountdownTimer.stop(); turnCountdownTimer = null; }
        timeLeft = 10; if (battleCanvas != null) battleCanvas.repaint();
    }

    private void onTurnTimerExpired() {
        if (processingTurn || timeUpTriggered) return;
        processingTurn = true; setPlayerEnabled(false);
        log("⏰  Time's up!  " + playerCharacter.getName() + "'s turn forfeited.");
        turnManager.advanceTurn(); refreshCdRow(); updateTurnState();
        Timer aiTrigger = new Timer(500, e -> { ((Timer)e.getSource()).stop(); doEnemyTurn(); });
        aiTrigger.setRepeats(false); aiTrigger.start();
    }

    private void startMatchTimer() {
        stopMatchTimer(); matchTimeLeft = MATCH_DURATION_SECONDS;
        matchTimer = new javax.swing.Timer(1000, e -> {
            matchTimeLeft--; if (battleCanvas != null) battleCanvas.repaint();
            if (matchTimeLeft <= 0) { stopMatchTimer(); SwingUtilities.invokeLater(this::onMatchTimerExpired); }
        });
        matchTimer.start();
    }

    private void stopMatchTimer() {
        if (matchTimer != null) { matchTimer.stop(); matchTimer = null; }
        matchTimeLeft = MATCH_DURATION_SECONDS; if (battleCanvas != null) battleCanvas.repaint();
    }

    private void onMatchTimerExpired() {
        timeUpTriggered = true;
        stopTurnTimer(); stopMatchTimer();
        setPlayerEnabled(false); processingTurn = true;
        log("⏱  TIME OVER!");

        if (battleCanvas != null) battleCanvas.repaint();

        // Pause to show "TIME UP" overlay on canvas before judging
        Timer judgeTimer = new Timer(2000, e -> {
            ((Timer)e.getSource()).stop();
            double pPct = (double)playerCharacter.getCurrentHP() / playerCharacter.getMaxHP();
            double ePct = (double)enemyCharacter.getCurrentHP() / enemyCharacter.getMaxHP();

            if (pPct > ePct) { log("🏆  " + playerCharacter.getName() + " wins by HP advantage!"); endRound(true); }
            else if (ePct > pPct) { log("💀  " + enemyCharacter.getName() + " wins by HP advantage!"); endRound(false); }
            else { log("⚖  Draw — " + enemyCharacter.getName() + " claims the round."); endRound(false); }
        });
        judgeTimer.setRepeats(false); judgeTimer.start();
    }

    // ── UI Helpers ──────────────────────────────────────────────
    private void refreshUI() { if (battleCanvas != null) battleCanvas.repaint(); }

    private void refreshCdRow() {
        if (turnManager == null) return;
        List<Skill> skills = playerCharacter.getSkills();
        for (int i = 0; i < skillBtns.length; i++) {
            if (skillBtns[i] == null || i >= skills.size()) continue;
            int cd = turnManager.getCooldownManager().getRemainingCooldown(playerCharacter, i);
            if (cd > 0) { cdLabels[i].setText(cd + " turn(s)"); cdLabels[i].setForeground(ORANGE_LOW); skillBtns[i].setEnabled(false); }
            else        { cdLabels[i].setText("READY");          cdLabels[i].setForeground(GREEN_RDY); }
        }
    }

    private void setPlayerEnabled(boolean on) {
        if (timeUpTriggered) on = false;
        for (JButton b : skillBtns) if (b != null) b.setEnabled(on);
        if (on) refreshCdRow();
    }

    private void flushResult(TurnResult r) { for (String m : r.getLogMessages()) log(m); }
    private void log(String msg) { SwingUtilities.invokeLater(() -> { if (battleLog != null) { battleLog.append(msg + "\n"); battleLog.setCaretPosition(battleLog.getDocument().getLength()); } }); }
    private static void fullHeal(Character c) { c.heal(c.getMaxHP()); }

    private String getFrameImg(String name) {
        for (int i = 0; i < CHAR_NAMES.length; i++) if (CHAR_NAMES[i].equals(name)) return FRAME_IMGS[i];
        return null;
    }

    protected void drawImageFill(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return; int iw = img.getWidth(null), ih = img.getHeight(null); if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih); int dw = (int) (iw * scale), dh = (int) (ih * scale); g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }
    protected void drawImageProportional(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return; int iw = img.getWidth(null), ih = img.getHeight(null); if (iw <= 0 || ih <= 0) return;
        double scale = Math.min((double) w / iw, (double) h / ih); int dw = (int) (iw * scale), dh = (int) (ih * scale); g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    // ══════════════════════════════════════════════════════════
    //  BattleCanvas
    // ══════════════════════════════════════════════════════════
    private class BattleCanvas extends JPanel {
        private final Image playerFrame, enemyFrame;
        private final Image[] tablets = new Image[3];
        private boolean playerActive = true;
        private float   glowTick    = 0f;
        private final Timer glowTimer;

        BattleCanvas() {
            setOpaque(false);
            playerFrame = loadImage(getFrameImg(playerCharacter.getName()));
            enemyFrame  = loadImage(getFrameImg(enemyCharacter.getName()));
            for (int i = 0; i < 3; i++) tablets[i] = loadImage(ROUND_TABLETS[i]);

            glowTimer = new Timer(16, e -> {
                glowTick += 0.08f;
                if (playerFlashAlpha > 0) playerFlashAlpha = Math.max(0, playerFlashAlpha - 0.04f);
                if (enemyFlashAlpha > 0)  enemyFlashAlpha  = Math.max(0, enemyFlashAlpha  - 0.04f);
                repaint();
            });
            glowTimer.start();
        }

        void setPlayerActive(boolean v) { playerActive = v; }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int W = getWidth(), H = getHeight();
            double sc = Math.min(W / 1024.0, H / 768.0);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,     RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int tabW = (int)(200*sc), tabH = (int)(68*sc);
            int tabX = (W - tabW) / 2, tabY = (int)(6*sc);
            int ri = Math.min(currentRound - 1, 2);
            Image tab = tablets[ri];
            if (tab != null) drawImageProportional(g2, tab, tabX, tabY, tabW, tabH);

            drawFlankTimers(g2, W, tabX, tabY, tabW, tabH, sc);

            int portW = (int)(82*sc), portH = (int)(82*sc);
            int hpW   = (int)(230*sc), hpH  = (int)(16*sc);
            int pillW = (int)(140*sc), pillH = (int)(24*sc);
            int portY = tabY + tabH + (int)(6*sc);

            // P1
            int ppx = (int)(10*sc);
            drawPortrait(g2, playerFrame, ppx, portY, portW, portH, PLAYER_CLR, playerActive);
            int phx = ppx + portW + (int)(8*sc), phy = portY + (int)(8*sc);
            drawHPBar(g2, phx, phy, hpW, hpH, playerCharacter, PLAYER_CLR);
            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(8, (int)(10*sc))));
            drawShadow(g2, "HP: " + playerCharacter.getCurrentHP() + " / " + playerCharacter.getMaxHP(), phx, phy + hpH + (int)(10*sc), new Color(0xFF,0xF5,0xDC,190));
            drawNamePill(g2, playerCharacter.getName(), phx, phy + hpH + (int)(14*sc), pillW, pillH, new Color(0x60,0x10,0x10,215), new Color(0xFF,0x99,0x99));
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int)(11*sc))));
            drawShadow(g2, "PLAYER", phx, phy - (int)(3*sc), new Color(0xEE,0xEE,0xEE));
            drawWinMarkers(g2, ppx, portY - (int)(16*sc), playerWins, ROUNDS_TO_WIN, true, PLAYER_CLR, sc);

            // P2
            int epx = W - (int)(10*sc) - portW;
            drawPortrait(g2, enemyFrame, epx, portY, portW, portH, ENEMY_CLR, !playerActive);
            int ehx = epx - hpW - (int)(8*sc), ehy = portY + (int)(8*sc);
            drawHPBar(g2, ehx, ehy, hpW, hpH, enemyCharacter, ENEMY_CLR);
            g2.setFont(new Font("SansSerif", Font.PLAIN, Math.max(8, (int)(10*sc))));
            drawShadow(g2, "HP: " + enemyCharacter.getCurrentHP() + " / " + enemyCharacter.getMaxHP(), ehx, ehy + hpH + (int)(10*sc), new Color(0xFF,0xF5,0xDC,190));
            drawNamePill(g2, enemyCharacter.getName(), ehx, ehy + hpH + (int)(14*sc), pillW, pillH, new Color(0x50,0x10,0x10,215), new Color(0xFF,0x77,0x77));
            g2.setFont(new Font("SansSerif", Font.BOLD, Math.max(9, (int)(11*sc))));
            drawShadow(g2, "ENEMY", ehx, ehy - (int)(3*sc), new Color(0xEE,0xEE,0xEE));
            drawWinMarkers(g2, epx + portW, portY - (int)(16*sc), enemyWins, ROUNDS_TO_WIN, false, ENEMY_CLR, sc);

            String tt = playerActive ? "▶ Your Turn" : "⏳ " + enemyCharacter.getName() + " is acting...";
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, Math.max(10, (int)(13*sc))));
            FontMetrics fm = g2.getFontMetrics();
            drawShadow(g2, tt, (W - fm.stringWidth(tt)) / 2, tabY + tabH + (int)(4*sc), GOLD);

            // ── ACTIVE SPRITE ZONE ──
            // ── NEW ──
            // ── NEW ──
            int groundY    = H - (int)(170 * sc);
            int spriteSize = Math.min((int)(220 * sc), (int)(H * 0.42));
            spriteSize     = Math.max(spriteSize, 120);

            int cx         = W / 2;
            int charOffset = (int)(250 * sc);

            int p1SpriteX = cx - charOffset - spriteSize / 2
                    - (playerAnimator != null ? playerAnimator.getKnockbackOffset() : 0);
            int p2SpriteX = cx + charOffset - spriteSize / 2
                    + (enemyAnimator != null ? enemyAnimator.getKnockbackOffset() : 0);

            // Player 1 Sprite
            if (playerAnimator != null) {
                Shape savedClip = g2.getClip();
                playerAnimator.draw(g2, p1SpriteX, groundY - spriteSize, spriteSize, spriteSize, this);
                g2.setClip(savedClip);
            }

            if (playerFlashAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, playerFlashAlpha * 0.8f)); // Intensified
                g2.setColor(new Color(255, 0, 0));
                g2.fillOval(p1SpriteX - spriteSize/4, groundY - spriteSize + spriteSize/4, (int)(spriteSize*1.5), spriteSize);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            // Player 2 Sprite [MIRRORED]
            // Player 2 Sprite [MIRRORED]
            if (enemyAnimator != null) {
                AffineTransform oldTransform = g2.getTransform();
                int mirrorCx = p2SpriteX + spriteSize / 2;
                g2.translate(mirrorCx, 0); g2.scale(-1, 1); g2.translate(-mirrorCx, 0);
                enemyAnimator.draw(g2, p2SpriteX, groundY - spriteSize, spriteSize, spriteSize, this);
                g2.setTransform(oldTransform);
            }

            if (enemyFlashAlpha > 0) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, enemyFlashAlpha * 0.8f)); // Intensified
                g2.setColor(new Color(255, 0, 0));
                g2.fillOval(p2SpriteX - spriteSize/4, groundY - spriteSize + spriteSize/4, (int)(spriteSize*1.5), spriteSize);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            // TIME UP Overlay
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

        private void drawWinMarkers(Graphics2D g2, int startX, int y, int wins, int totalRounds, boolean leftAlign, Color fillClr, double sc) {
            int mSize = (int)(14 * sc);
            int gap   = (int)(6 * sc);

            for (int i = 0; i < totalRounds; i++) {
                int mx = leftAlign ? startX + i * (mSize + gap) : startX - mSize - i * (mSize + gap);
                g2.setColor(new Color(0x08, 0x05, 0x02, 200)); g2.fillOval(mx, y, mSize, mSize);
                g2.setStroke(new BasicStroke(1.5f)); g2.setColor(new Color(150, 120, 40, 120)); g2.drawOval(mx, y, mSize, mSize);
                if (i < wins) {
                    float pulse = (float)(0.7 + 0.3 * Math.abs(Math.sin(glowTick * 2.0)));
                    g2.setColor(new Color(fillClr.getRed(), fillClr.getGreen(), fillClr.getBlue(), (int)(120 * pulse)));
                    g2.fillOval(mx - 2, y - 2, mSize + 4, mSize + 4);
                    g2.setColor(fillClr); g2.fillOval(mx + 2, y + 2, mSize - 4, mSize - 4);
                    g2.setColor(new Color(255, 255, 255, 180)); g2.fillOval(mx + 4, y + 3, mSize / 3, mSize / 3);
                    g2.setColor(new Color(255, 215, 100)); g2.drawOval(mx, y, mSize, mSize);
                }
            }
        }

        private void drawFlankTimers(Graphics2D g2, int W, int tabX, int tabY, int tabW, int tabH, double sc) {
            int padH = (int)(12 * sc), padV = (int)(5 * sc);
            int matchFontSz = Math.max(14, (int)(19 * sc));
            g2.setFont(new Font("Monospaced", Font.BOLD, matchFontSz));
            FontMetrics mfm = g2.getFontMetrics();
            int mins = matchTimeLeft / 60, secs = matchTimeLeft % 60;
            String mTxt = String.format("%02d:%02d", mins, secs);
            Color matchColor = matchTimeLeft > 40 ? GOLD : matchTimeLeft > 15 ? ORANGE_LOW : RED_CRIT;

            int mW = mfm.stringWidth(mTxt) + padH * 2, mH = mfm.getHeight() + padV * 2;
            int mX = Math.max((int)(6*sc), tabX - mW - (int)(8*sc)), mY = tabY + (tabH - mH) / 2;
            if (mX < (int)(4*sc)) mX = (int)(4*sc);

            g2.setColor(new Color(0x06, 0x03, 0x01, 225)); g2.fillRoundRect(mX, mY, mW, mH, 12, 12);
            float mAlpha = matchTimeLeft <= 15 ? 130 + 125*(float)Math.abs(Math.sin(glowTick*2.5f)) : 185;
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(matchColor.getRed(), matchColor.getGreen(), matchColor.getBlue(), Math.min(255,(int)mAlpha)));
            g2.drawRoundRect(mX, mY, mW, mH, 12, 12);
            g2.setStroke(new BasicStroke(1f)); g2.setColor(new Color(255,255,255,16)); g2.drawRoundRect(mX+2, mY+2, mW-4, mH-4, 10, 10);
            int mTX = mX + (mW - mfm.stringWidth(mTxt)) / 2, mTY = mY + padV + mfm.getAscent();
            g2.setColor(new Color(0,0,0,150)); g2.drawString(mTxt, mTX+1, mTY+1); g2.setColor(matchColor); g2.drawString(mTxt, mTX, mTY);

            if (turnCountdownTimer != null && turnCountdownTimer.isRunning()) {
                int turnFontSz = Math.max(13, (int)(17 * sc));
                g2.setFont(new Font("Serif", Font.BOLD, turnFontSz));
                FontMetrics tfm = g2.getFontMetrics();
                Color turnColor = timeLeft > 6 ? new Color(0xEE,0xEE,0xEE) : timeLeft > 3 ? ORANGE_LOW : RED_CRIT;
                String tTxt = "TURN  " + timeLeft;
                int tW = tfm.stringWidth(tTxt) + padH * 2, tH = tfm.getHeight() + padV * 2;
                int tX = tabX + tabW + (int)(8*sc);
                if (tX + tW > W - (int)(4*sc)) tX = W - tW - (int)(4*sc);
                int tY = tabY + (tabH - tH) / 2;

                g2.setColor(new Color(0x06, 0x03, 0x01, 215)); g2.fillRoundRect(tX, tY, tW, tH, tH, tH);
                float tAlpha = timeLeft <= 3 ? 120 + 135*(float)Math.abs(Math.sin(glowTick*3)) : 155;
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(new Color(turnColor.getRed(), turnColor.getGreen(), turnColor.getBlue(), Math.min(255,(int)tAlpha)));
                g2.drawRoundRect(tX, tY, tW, tH, tH, tH);
                int tTX = tX + (tW - tfm.stringWidth(tTxt)) / 2, tTY = tY + padV + tfm.getAscent();
                g2.setColor(new Color(0,0,0,130)); g2.drawString(tTxt, tTX+1, tTY+1); g2.setColor(turnColor); g2.drawString(tTxt, tTX, tTY);
            }
        }

        private void drawPortrait(Graphics2D g2, Image img, int x, int y, int w, int h, Color accent, boolean active) {
            if (active) {
                float a = 0.25f + 0.15f * (float)Math.sin(glowTick);
                for (int r = 5; r >= 1; r--) {
                    int sp = r*3;
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),Math.min(255,(int)(a*80/r))));
                    g2.setStroke(new BasicStroke(sp)); g2.drawRoundRect(x-sp/2,y-sp/2,w+sp,h+sp,10,10);
                }
            }
            g2.setColor(new Color(0x08,0x05,0x02,200)); g2.fillRoundRect(x,y,w,h,8,8);
            if (img != null) drawImageProportional(g2, img, x, y, w, h);
            g2.setStroke(new BasicStroke(2)); g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),active?220:100)); g2.drawRoundRect(x,y,w,h,8,8);
        }

        private void drawHPBar(Graphics2D g2, int x, int y, int w, int h, Character c, Color base) {
            double pct = Math.max(0, Math.min(1.0, (double)c.getCurrentHP() / c.getMaxHP()));
            Color bar = pct <= 0.25 ? RED_CRIT : pct <= 0.50 ? ORANGE_LOW : base;
            g2.setColor(new Color(0x08,0x04,0x02,220)); g2.fillRoundRect(x,y,w,h,h,h);
            int fw = (int)(w*pct);
            if (fw > 2) { g2.setPaint(new GradientPaint(x,y,bar.brighter(),x,y+h,bar.darker())); g2.fillRoundRect(x,y,fw,h,h,h); }
            g2.setStroke(new BasicStroke(1)); g2.setColor(new Color(0xFF,0xFF,0xFF,55)); g2.drawRoundRect(x,y,w,h,h,h);
        }

        private void drawNamePill(Graphics2D g2, String text, int x, int y, int w, int h, Color bg, Color fg) {
            g2.setColor(bg); g2.fillRoundRect(x,y,w,h,h,h);
            g2.setStroke(new BasicStroke(1)); g2.setColor(new Color(fg.getRed(),fg.getGreen(),fg.getBlue(),130)); g2.drawRoundRect(x,y,w,h,h,h);
            g2.setFont(new Font("Serif",Font.BOLD,Math.max(9,h-6))); FontMetrics fm = g2.getFontMetrics();
            int tx = x+(w-fm.stringWidth(text))/2, ty = y+(h+fm.getAscent()-fm.getDescent())/2;
            g2.setColor(new Color(0,0,0,100)); g2.drawString(text,tx+1,ty+1); g2.setColor(fg); g2.drawString(text,tx,ty);
        }

        private void drawShadow(Graphics2D g2, String t, int x, int y, Color c) { g2.setColor(new Color(0,0,0,150)); g2.drawString(t,x+1,y+1); g2.setColor(c); g2.drawString(t,x,y); }
    }

    private class RoundOverlay extends JPanel {
        private float alpha = 0f; private Image textImg; private Timer fadeTimer;
        RoundOverlay() { setOpaque(false); }
        void show(int round) {
            textImg = loadImage(ROUND_TEXTS[Math.min(round-1,2)]);
            alpha = 0f; setVisible(true); repaint();
            if (fadeTimer != null) fadeTimer.stop();
            final boolean[] in = {true}; final long[] hold = {0};
            fadeTimer = new Timer(16, e -> {
                if (in[0]) { alpha = Math.min(1f,alpha+0.07f); if(alpha>=1f){in[0]=false;hold[0]=System.currentTimeMillis();} }
                else if (System.currentTimeMillis()-hold[0]<900) {}
                else { alpha=Math.max(0f,alpha-0.05f); if(alpha<=0f){((Timer)e.getSource()).stop();setVisible(false);} }
                repaint();
            });
            fadeTimer.start();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); if (alpha <= 0f) return;
            int W=getWidth(),H=getHeight(); Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,Math.min(1f,alpha*0.5f)));
            g2.setColor(Color.BLACK); g2.fillRect(0,0,W,H);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
            if (textImg!=null){int iw=textImg.getWidth(null),ih=textImg.getHeight(null);if(iw>0&&ih>0){double sc=Math.min(W*0.78/iw,H*0.40/ih);int dw=(int)(iw*sc),dh=(int)(ih*sc);g2.drawImage(textImg,(W-dw)/2,(H-dh)/2,dw,dh,null);}}
            g2.dispose();
        }
    }

    private JButton makePillButton(String label, Color bg, Color fg) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h=getModel().isRollover(),en=isEnabled();
                Color bc=en?(h?bg.brighter():bg):new Color(0x28,0x20,0x18,140);
                g2.setColor(new Color(bc.getRed(),bc.getGreen(),bc.getBlue(),210)); g2.fillRoundRect(0,0,getWidth(),getHeight(),getHeight(),getHeight());
                g2.setStroke(new BasicStroke(1.5f)); g2.setColor(new Color(fg.getRed(),fg.getGreen(),fg.getBlue(),en?(h?255:180):70)); g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,getHeight()-2,getHeight()-2);
                g2.setFont(new Font("Serif",Font.BOLD,Math.max(9,getHeight()-8))); FontMetrics fm=g2.getFontMetrics();
                int tx=(getWidth()-fm.stringWidth(getText()))/2,ty=(getHeight()+fm.getAscent()-fm.getDescent())/2;
                g2.setColor(new Color(0,0,0,90)); g2.drawString(getText(),tx+1,ty+1); g2.setColor(en?fg:new Color(0x60,0x50,0x40)); g2.drawString(getText(),tx,ty); g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel makeDim() {
        JPanel dim = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0,0,0,180)); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        dim.setOpaque(false); return dim;
    }

    private JPanel makeCard(int w, int h) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x0E,0x09,0x04,248)); g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                g2.setStroke(new BasicStroke(2f)); g2.setColor(BORDER_CLR); g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,20,20); g2.dispose(); super.paintComponent(g);
            }
        };
        card.setOpaque(false); card.setPreferredSize(new Dimension(w,h)); card.setMaximumSize(new Dimension(w,h)); return card;
    }

    private JLabel cardTitle(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER); l.setFont(new Font("Serif",Font.BOLD|Font.ITALIC,22)); l.setForeground(GOLD); l.setAlignmentX(Component.CENTER_ALIGNMENT); return l;
    }

    private JLabel cardBody(String text) {
        String html = "<html><div style='text-align:center;color:#D4C5A0;font-size:13px;font-family:serif'>" + text.replace("\n","<br>") + "</div></html>";
        JLabel l = new JLabel(html, SwingConstants.CENTER); l.setAlignmentX(Component.CENTER_ALIGNMENT); return l;
    }

    private JButton makeGoldOverlayButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean h = getModel().isRollover();
                g2.setPaint(new GradientPaint(0,0,h?new Color(170,110,40):new Color(120,70,20),0,getHeight(),h?new Color(120,80,30):new Color(80,40,10))); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                if(h) { g2.setColor(new Color(255,215,120,80)); g2.setStroke(new BasicStroke(2.5f)); g2.drawRoundRect(2,2,getWidth()-4,getHeight()-4,14,14); }
                g2.setColor(new Color(220,180,90)); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,14,14);
                g2.setFont(new Font("Serif",Font.BOLD,14)); FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()-fm.stringWidth(getText()))/2, ty = (getHeight()+fm.getAscent())/2-2;
                g2.setColor(Color.BLACK); g2.drawString(getText(),tx+1,ty+1); g2.setColor(new Color(255,230,170)); g2.drawString(getText(),tx,ty); g2.dispose();
            }
        };
        btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(140,46)); return btn;
    }

    private void mountOverlay(JPanel dim) {
        overlayLayer.add(dim); dim.setBounds(0,0,getWidth(),getHeight());
        overlayLayer.addComponentListener(new ComponentAdapter() { @Override public void componentResized(ComponentEvent e) { dim.setBounds(0,0,overlayLayer.getWidth(),overlayLayer.getHeight()); } });
        overlayLayer.revalidate(); overlayLayer.repaint();
    }

    private Image loadImage(String path) {
        if (path == null) return null;
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                int iw = img.getWidth(null), ih = img.getHeight(null);
                if (iw <= 0 || ih <= 0) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                // Scale to fully cover the panel
                double scale = Math.max((double) getWidth() / iw, (double) getHeight() / ih);
                int dw = (int)(iw * scale), dh = (int)(ih * scale);
                int dx = (getWidth() - dw) / 2;   // center horizontally
                int dy = getHeight() - dh;          // anchor bottom — floor is always visible
                g2.drawImage(img, dx, dy, dw, dh, null);
                g2.dispose();
            }
        }
    }
}