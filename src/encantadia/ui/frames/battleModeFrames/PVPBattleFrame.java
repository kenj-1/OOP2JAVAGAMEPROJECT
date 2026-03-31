package encantadia.ui.frames.battleModeFrames;

import encantadia.ScreenManager;
import encantadia.battle.engine.TurnManager;
import encantadia.battle.result.TurnResult;
import encantadia.battle.skill.Skill;
import encantadia.characters.*;
import encantadia.characters.Character;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class PVPBattleFrame extends JFrame {

    // ── Constants ─────────────────────────────────────────────
    private static final int    ROUNDS_TO_WIN       = 2;
    private static final double WINNER_VOTE_WEIGHT  = 0.70;

    // ── Colour palette ────────────────────────────────────────
    private static final Color BG_DARK    = new Color(0x18, 0x14, 0x0E);
    private static final Color BG_PANEL   = new Color(0x22, 0x1C, 0x14);
    private static final Color BG_LOG     = new Color(0x10, 0x0D, 0x07);
    private static final Color BORDER_CLR = new Color(0xC8, 0xA0, 0x28);
    private static final Color P1_CLR     = new Color(0x2E, 0x8B, 0x57);  // sea-green
    private static final Color P2_CLR     = new Color(0x2A, 0x6B, 0xB0);  // royal-blue
    private static final Color GOLD       = new Color(0xC8, 0xA0, 0x28);
    private static final Color CREAM      = new Color(0xFF, 0xF5, 0xDC);
    private static final Color LOG_FG     = new Color(0xD4, 0xC5, 0xA0);
    private static final Color ORANGE_LOW = new Color(0xCC, 0x88, 0x22);
    private static final Color RED_CRIT   = new Color(0xCC, 0x22, 0x22);
    private static final Color GREEN_RDY  = new Color(0x60, 0xCC, 0x60);

    // ── Card names ────────────────────────────────────────────
    private static final String CARD_SELECT = "SELECT_P2";
    private static final String CARD_COIN   = "COIN_TOSS";
    private static final String CARD_BATTLE = "BATTLE";

    // ── Characters ────────────────────────────────────────────
    private final Character player1;
    private Character       player2;

    // ── Engine ────────────────────────────────────────────────
    private TurnManager turnManager;

    // ── Match state ───────────────────────────────────────────
    private int     p1Wins       = 0;
    private int     p2Wins       = 0;
    private int     currentRound = 1;
    private boolean p1GoesFirst  = true;
    private volatile boolean processingTurn = false;

    // ── Layout ────────────────────────────────────────────────
    private CardLayout cardLayout;
    private JPanel     mainPanel;

    // ── Coin toss UI refs ─────────────────────────────────────
    private JLabel  coinFaceLabel;
    private JLabel  coinStatusLabel;
    private JLabel  coinPlayerInfoLabel;
    private JButton headsBtn;
    private JButton tailsBtn;
    private String  p1CoinChoice = null;

    // ── Battle UI refs ────────────────────────────────────────
    private JLabel       roundLabel;
    private JLabel       p1WinsLabel, p2WinsLabel;
    private JLabel       p1HPLabel,   p2HPLabel;
    private JProgressBar p1HPBar,     p2HPBar;
    private JTextArea    battleLog;
    private JButton[]    p1SkillBtns   = new JButton[3];
    private JButton[]    p2SkillBtns   = new JButton[3];
    private JLabel[]     p1CdLabels    = new JLabel[3];
    private JLabel[]     p2CdLabels    = new JLabel[3];
    private JLabel       turnIndicator;

    // ══════════════════════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════════════════════
    public PVPBattleFrame(Character player1Character) {
        this.player1 = player1Character;

        setTitle("PVP Battle");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);

        mainPanel.add(buildP2SelectPanel(), CARD_SELECT);
        mainPanel.add(buildCoinTossPanel(), CARD_COIN);
        // Battle panel is added lazily after P2 is known

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, CARD_SELECT);

        setVisible(true);
        ScreenManager.register(this);
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    // ══════════════════════════════════════════════════════════
    //  Phase 1 — Player 2 Character Selection
    // ══════════════════════════════════════════════════════════

    private JPanel buildP2SelectPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        // ── Title ─────────────────────────────────────────────
        JLabel title = new JLabel("PLAYER 2  —  Choose Your Fighter", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 26));
        title.setForeground(P2_CLR);
        title.setBorder(BorderFactory.createEmptyBorder(24, 0, 8, 0));
        panel.add(title, BorderLayout.NORTH);

        // ── Character grid ────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 14));
        grid.setBackground(BG_DARK);
        grid.setBorder(BorderFactory.createEmptyBorder(16, 40, 16, 40));

        Character[] roster = {
                new Dirk(), new Mary(), new MakelanShere(), new Tyrone(),
                new Adamus(), new Tera(), new Flamara(), new Dea()
        };
        for (Character ch : roster) grid.add(buildCharCard(ch));
        panel.add(grid, BorderLayout.CENTER);

        // ── Player 1 info footer ──────────────────────────────
        JLabel footer = new JLabel(
                "Player 1 has chosen:  " + player1.getName() + "  —  " + player1.getTitle(),
                SwingConstants.CENTER);
        footer.setFont(new Font("Serif", Font.ITALIC, 14));
        footer.setForeground(P1_CLR);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildCharCard(Character ch) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(BG_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel name = new JLabel(ch.getName(), SwingConstants.CENTER);
        name.setFont(new Font("Serif", Font.BOLD, 13));
        name.setForeground(CREAM);

        JLabel subtitle = new JLabel(ch.getTitle(), SwingConstants.CENTER);
        subtitle.setFont(new Font("Serif", Font.ITALIC, 9));
        subtitle.setForeground(new Color(0xA0, 0x88, 0x50));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.add(name);
        info.add(Box.createVerticalStrut(2));
        info.add(subtitle);

        JButton btn = makeDarkButton("Select");
        btn.addActionListener(e -> onP2Selected(ch));

        card.add(info, BorderLayout.CENTER);
        card.add(btn,  BorderLayout.SOUTH);
        return card;
    }

    private void onP2Selected(Character selected) {
        this.player2 = selected;
        setTitle("PVP Battle — " + player1.getName() + " vs " + player2.getName());

        // Update coin toss info label
        coinPlayerInfoLabel.setText(
                player1.getName() + "  (P1)   vs   " + player2.getName() + "  (P2)");

        // Reset coin toss state
        p1CoinChoice = null;
        coinFaceLabel.setText("?");
        coinFaceLabel.setForeground(GOLD);
        coinStatusLabel.setText("Player 1 — pick your side:");
        headsBtn.setEnabled(true);
        tailsBtn.setEnabled(true);

        cardLayout.show(mainPanel, CARD_COIN);
    }

    // ══════════════════════════════════════════════════════════
    //  Phase 2 — Coin Toss
    // ══════════════════════════════════════════════════════════

    private JPanel buildCoinTossPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_DARK);

        // Title
        JLabel title = new JLabel("COIN TOSS — Who Goes First?", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 26));
        title.setForeground(GOLD);
        title.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        panel.add(title, BorderLayout.NORTH);

        // Center block
        JPanel center = new JPanel();
        center.setBackground(BG_DARK);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Coin face
        coinFaceLabel = new JLabel("?", SwingConstants.CENTER);
        coinFaceLabel.setFont(new Font("Serif", Font.BOLD, 90));
        coinFaceLabel.setForeground(GOLD);
        coinFaceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        coinFaceLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CLR, 3),
                BorderFactory.createEmptyBorder(16, 44, 16, 44)));

        coinStatusLabel = new JLabel("Player 1 — pick your side:", SwingConstants.CENTER);
        coinStatusLabel.setFont(new Font("Serif", Font.ITALIC, 16));
        coinStatusLabel.setForeground(CREAM);
        coinStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Heads / Tails buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        btnRow.setBackground(BG_DARK);
        headsBtn = makeDarkButton("HEADS");
        tailsBtn = makeDarkButton("TAILS");
        headsBtn.setPreferredSize(new Dimension(140, 50));
        tailsBtn.setPreferredSize(new Dimension(140, 50));
        headsBtn.addActionListener(e -> onP1Picks("HEADS"));
        tailsBtn.addActionListener(e -> onP1Picks("TAILS"));
        btnRow.add(headsBtn);
        btnRow.add(tailsBtn);

        center.add(Box.createVerticalGlue());
        center.add(coinFaceLabel);
        center.add(Box.createVerticalStrut(22));
        center.add(coinStatusLabel);
        center.add(Box.createVerticalStrut(18));
        center.add(btnRow);
        center.add(Box.createVerticalGlue());
        panel.add(center, BorderLayout.CENTER);

        // Player info footer
        coinPlayerInfoLabel = new JLabel("Waiting for Player 2 selection...", SwingConstants.CENTER);
        coinPlayerInfoLabel.setFont(new Font("Serif", Font.ITALIC, 13));
        coinPlayerInfoLabel.setForeground(LOG_FG);
        coinPlayerInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        panel.add(coinPlayerInfoLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void onP1Picks(String choice) {
        p1CoinChoice = choice;
        headsBtn.setEnabled(false);
        tailsBtn.setEnabled(false);
        coinStatusLabel.setText("Player 1 chose " + choice + ". Flipping coin...");
        animateCoin();
    }

    private void animateCoin() {
        String[] sideLabels = {"H","T","H","T","H","T","H","T","H","T","H","T"};
        final int[] idx     = {0};
        final String result = Math.random() < 0.5 ? "HEADS" : "TAILS";

        Timer animator = new Timer(110, null);
        animator.addActionListener(ev -> {
            if (idx[0] < sideLabels.length) {
                coinFaceLabel.setText(sideLabels[idx[0]]);
                coinFaceLabel.setForeground(idx[0] % 2 == 0 ? GOLD : P2_CLR);
                idx[0]++;
            } else {
                animator.stop();
                coinFaceLabel.setText(result.equals("HEADS") ? "H" : "T");
                coinFaceLabel.setForeground(GOLD);

                p1GoesFirst = p1CoinChoice.equals(result);
                String firstPlayer = p1GoesFirst
                        ? player1.getName() + " (P1)"
                        : player2.getName() + " (P2)";
                coinStatusLabel.setText("Result: " + result + "!  " + firstPlayer + " goes FIRST!");

                new Timer(2000, done -> {
                    ((Timer) done.getSource()).stop();
                    launchBattle();
                }) {{ setRepeats(false); start(); }};
            }
        });
        animator.start();
    }

    // ══════════════════════════════════════════════════════════
    //  Phase 3 — Battle
    // ══════════════════════════════════════════════════════════

    private void launchBattle() {
        // Build and add battle panel now that both characters are known
        mainPanel.add(buildBattlePanel(), CARD_BATTLE);
        cardLayout.show(mainPanel, CARD_BATTLE);

        // Init engine — player1 = "player", player2 = "enemy" in TurnManager terms
        fullHeal(player1);
        fullHeal(player2);
        turnManager = new TurnManager(player1, player2);

        // If P2 goes first, flip the turn once.
        // All cooldowns start at 0 so decrement has no side-effects.
        if (!p1GoesFirst) {
            turnManager.advanceTurn();
        }

        refreshUI();
        refreshAllCdLabels();
        updateTurnState();

        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("⚔  Round " + currentRound
                + " — First to " + ROUNDS_TO_WIN + " round wins!");
        log(player1.getName() + " [P1]  vs  " + player2.getName() + " [P2]");
        log("🪙  " + (p1GoesFirst
                ? player1.getName() + " (P1)" : player2.getName() + " (P2)")
                + " goes first!");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // ── Build battle panel ────────────────────────────────────

    private JPanel buildBattlePanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.add(buildTopBar(),   BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);
        root.add(buildSkillBar(), BorderLayout.SOUTH);
        return root;
    }

    // ── Top bar ───────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x0C, 0x09, 0x05));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_CLR));
        bar.setPreferredSize(new Dimension(0, 62));

        p1WinsLabel = new JLabel("P1: 0", SwingConstants.CENTER);
        p1WinsLabel.setFont(new Font("Serif", Font.BOLD, 20));
        p1WinsLabel.setForeground(P1_CLR);
        p1WinsLabel.setPreferredSize(new Dimension(90, 0));
        p1WinsLabel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));

        p2WinsLabel = new JLabel("P2: 0", SwingConstants.CENTER);
        p2WinsLabel.setFont(new Font("Serif", Font.BOLD, 20));
        p2WinsLabel.setForeground(P2_CLR);
        p2WinsLabel.setPreferredSize(new Dimension(90, 0));
        p2WinsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 24));

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));

        roundLabel = new JLabel("Round " + currentRound, SwingConstants.CENTER);
        roundLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 22));
        roundLabel.setForeground(GOLD);
        roundLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Best of " + (ROUNDS_TO_WIN * 2 - 1), SwingConstants.CENTER);
        sub.setFont(new Font("Serif", Font.ITALIC, 12));
        sub.setForeground(new Color(0xA0, 0x88, 0x50));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        centre.add(Box.createVerticalGlue());
        centre.add(roundLabel);
        centre.add(sub);
        centre.add(Box.createVerticalGlue());

        bar.add(p1WinsLabel, BorderLayout.WEST);
        bar.add(centre,      BorderLayout.CENTER);
        bar.add(p2WinsLabel, BorderLayout.EAST);
        return bar;
    }

    // ── Main area: P1 panel | log | P2 panel ─────────────────
    private JPanel buildMainArea() {
        JPanel area = new JPanel(new GridBagLayout());
        area.setBackground(BG_DARK);

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.BOTH;
        g.weighty = 1.0;

        g.gridx = 0; g.weightx = 0.26;
        g.insets = new Insets(10, 10, 6, 6);
        area.add(buildSidePanel(player1, P1_CLR, true), g);

        g.gridx = 1; g.weightx = 0.48;
        g.insets = new Insets(10, 0, 6, 0);
        area.add(buildLogPanel(), g);

        g.gridx = 2; g.weightx = 0.26;
        g.insets = new Insets(10, 6, 6, 10);
        area.add(buildSidePanel(player2, P2_CLR, false), g);

        return area;
    }

    private JPanel buildSidePanel(Character ch, Color cl, boolean isP1) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(cl, 1),
                BorderFactory.createEmptyBorder(12, 14, 14, 14)));

        // Portrait placeholder
        JPanel portrait = new JPanel(new BorderLayout());
        portrait.setBackground(new Color(0x18, 0x14, 0x0E));
        portrait.setBorder(new LineBorder(new Color(0x40, 0x32, 0x18), 1));
        portrait.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        portrait.setPreferredSize(new Dimension(0, 100));
        JLabel pLbl = new JLabel(ch.getName(), SwingConstants.CENTER);
        pLbl.setFont(new Font("Serif", Font.ITALIC, 11));
        pLbl.setForeground(new Color(0x70, 0x60, 0x40));
        portrait.add(pLbl, BorderLayout.CENTER);

        JLabel playerTag = new JLabel(isP1 ? "PLAYER 1" : "PLAYER 2", SwingConstants.CENTER);
        playerTag.setFont(new Font("SansSerif", Font.BOLD, 10));
        playerTag.setForeground(cl);
        playerTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLabel = new JLabel(ch.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Serif", Font.BOLD, 14));
        nameLabel.setForeground(CREAM);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(ch.getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.ITALIC, 9));
        titleLabel.setForeground(new Color(0xA0, 0x88, 0x50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // HP bar
        JProgressBar bar = new JProgressBar(0, ch.getMaxHP());
        bar.setValue(ch.getCurrentHP());
        bar.setStringPainted(false);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 13));
        bar.setForeground(cl);
        bar.setBackground(new Color(0x2A, 0x1A, 0x10));
        bar.setBorder(BorderFactory.createLineBorder(new Color(0x50, 0x40, 0x20)));

        JLabel hpLbl = new JLabel(
                "HP: " + ch.getCurrentHP() + " / " + ch.getMaxHP(), SwingConstants.CENTER);
        hpLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hpLbl.setForeground(LOG_FG);
        hpLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (isP1) { p1HPBar = bar; p1HPLabel = hpLbl; }
        else       { p2HPBar = bar; p2HPLabel = hpLbl; }

        // Turn indicator lives on P1 panel
        if (isP1) {
            turnIndicator = new JLabel("▶ P1's Turn", SwingConstants.CENTER);
            turnIndicator.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 12));
            turnIndicator.setForeground(GOLD);
            turnIndicator.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        panel.add(portrait);
        panel.add(Box.createVerticalStrut(6));
        panel.add(playerTag);
        panel.add(Box.createVerticalStrut(2));
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(2));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(bar);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hpLbl);
        if (isP1) {
            panel.add(Box.createVerticalStrut(8));
            panel.add(turnIndicator);
        }
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
        wrap.setBackground(BG_DARK);
        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    // ── Skill bar: [P1 skills] | [VS] | [P2 skills] ──────────
    private JPanel buildSkillBar() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(0x0C, 0x09, 0x05));
        wrapper.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, BORDER_CLR));
        wrapper.setPreferredSize(new Dimension(0, 100));

        wrapper.add(buildPlayerSkillRow(player1, p1SkillBtns, p1CdLabels, P1_CLR, true),
                BorderLayout.WEST);

        // Center divider
        JPanel divider = new JPanel(new BorderLayout());
        divider.setBackground(new Color(0x0C, 0x09, 0x05));
        divider.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 2, BORDER_CLR));
        JLabel vsLabel = new JLabel("VS", SwingConstants.CENTER);
        vsLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 22));
        vsLabel.setForeground(GOLD);
        divider.add(vsLabel, BorderLayout.CENTER);
        wrapper.add(divider, BorderLayout.CENTER);

        wrapper.add(buildPlayerSkillRow(player2, p2SkillBtns, p2CdLabels, P2_CLR, false),
                BorderLayout.EAST);

        return wrapper;
    }

    private JPanel buildPlayerSkillRow(Character ch, JButton[] btns,
                                       JLabel[] cdLbls, Color accent, boolean isP1) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(0x0C, 0x09, 0x05));
        outer.setPreferredSize(new Dimension(390, 0));

        JPanel inner = new JPanel(new GridLayout(1, 3, 8, 0));
        inner.setBackground(new Color(0x0C, 0x09, 0x05));
        inner.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        List<Skill> skills = ch.getSkills();
        for (int i = 0; i < 3; i++) {
            String skillName = (i < skills.size()) ? skills.get(i).getName() : "—";

            JPanel slot = new JPanel(new BorderLayout(0, 3));
            slot.setOpaque(false);

            final int si = i;
            btns[i] = makeSkillButton(skillName, accent);
            btns[i].setEnabled(false); // enabled when it's this player's turn
            if (isP1) { btns[i].addActionListener(e -> onP1Skill(si)); }
            else       { btns[i].addActionListener(e -> onP2Skill(si)); }

            cdLbls[i] = new JLabel("READY", SwingConstants.CENTER);
            cdLbls[i].setFont(new Font("SansSerif", Font.BOLD, 9));
            cdLbls[i].setForeground(GREEN_RDY);

            slot.add(btns[i],   BorderLayout.CENTER);
            slot.add(cdLbls[i], BorderLayout.SOUTH);
            inner.add(slot);
        }

        outer.add(inner, BorderLayout.CENTER);
        return outer;
    }

    // ══════════════════════════════════════════════════════════
    //  Turn logic
    // ══════════════════════════════════════════════════════════

    /** Player 1 uses a skill (left side). */
    private void onP1Skill(int si) {
        if (processingTurn)            return;
        if (!turnManager.isPlayerTurn()) return; // not P1's turn

        processingTurn = true;
        setP1Enabled(false);
        setP2Enabled(false);

        TurnResult res = turnManager.executeSkill(player1, player2, si);
        flushResult(res);
        refreshUI();

        if (res.isTargetDefeated()) { endRound(true); return; }

        if (res.isTurnStolen()) {
            // P1 acts again — no advanceTurn
            setP1Enabled(true);
            processingTurn = false;
            return;
        }

        turnManager.advanceTurn();
        refreshAllCdLabels();
        updateTurnState(); // enables P2's buttons
        processingTurn = false;
    }

    /** Player 2 uses a skill (right side). */
    private void onP2Skill(int si) {
        if (processingTurn)           return;
        if (turnManager.isPlayerTurn()) return; // not P2's turn

        processingTurn = true;
        setP1Enabled(false);
        setP2Enabled(false);

        TurnResult res = turnManager.executeSkill(player2, player1, si);
        flushResult(res);
        refreshUI();

        if (res.isTargetDefeated()) { endRound(false); return; }

        if (res.isTurnStolen()) {
            // P2 acts again
            setP2Enabled(true);
            processingTurn = false;
            return;
        }

        turnManager.advanceTurn();
        refreshAllCdLabels();
        updateTurnState(); // enables P1's buttons
        processingTurn = false;
    }

    /** Enables the correct player's skill buttons based on TurnManager state. */
    private void updateTurnState() {
        boolean p1Turn = turnManager.isPlayerTurn();
        setP1Enabled(p1Turn);
        setP2Enabled(!p1Turn);

        if (turnIndicator != null) {
            if (p1Turn) {
                turnIndicator.setText("▶ " + player1.getName() + "'s Turn");
                turnIndicator.setForeground(P1_CLR);
            } else {
                turnIndicator.setText("▶ " + player2.getName() + "'s Turn");
                turnIndicator.setForeground(P2_CLR);
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    //  Round / match management
    // ══════════════════════════════════════════════════════════

    private void endRound(boolean p1Won) {
        setP1Enabled(false);
        setP2Enabled(false);

        if (p1Won) {
            p1Wins++;
            p1WinsLabel.setText("P1: " + p1Wins);
            log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log("🏆  " + player1.getName() + " (P1) wins Round " + currentRound + "!");
        } else {
            p2Wins++;
            p2WinsLabel.setText("P2: " + p2Wins);
            log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            log("🏆  " + player2.getName() + " (P2) wins Round " + currentRound + "!");
        }

        if (p1Wins >= ROUNDS_TO_WIN || p2Wins >= ROUNDS_TO_WIN) {
            new Timer(1600, e -> {
                ((Timer) e.getSource()).stop();
                showMatchResult(p1Wins >= ROUNDS_TO_WIN);
            }) {{ setRepeats(false); start(); }};
        } else {
            currentRound++;
            new Timer(2000, e -> {
                ((Timer) e.getSource()).stop();
                startNextRound();
            }) {{ setRepeats(false); start(); }};
        }
    }

    private void startNextRound() {
        fullHeal(player1);
        fullHeal(player2);
        // Alternate first turn each round for fairness
        p1GoesFirst = !p1GoesFirst;
        turnManager  = new TurnManager(player1, player2);
        if (!p1GoesFirst) turnManager.advanceTurn(); // all CDs = 0, no side-effects

        roundLabel.setText("Round " + currentRound);
        refreshUI();
        refreshAllCdLabels();
        processingTurn = false;
        updateTurnState();

        String first = p1GoesFirst
                ? player1.getName() + " (P1)" : player2.getName() + " (P2)";
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log("⚔  Round " + currentRound + " begins!");
        log("🔄  " + first + " goes first this round.");
        log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private void showMatchResult(boolean p1Won) {
        Character winner = p1Won ? player1 : player2;
        Character loser  = p1Won ? player2 : player1;
        int wW = p1Won ? p1Wins : p2Wins;
        int lW = p1Won ? p2Wins : p1Wins;
        String wTag = p1Won ? "(P1)" : "(P2)";
        String lTag = p1Won ? "(P2)" : "(P1)";

        log("★  MATCH OVER — " + winner.getName() + " " + wTag
                + " wins " + wW + "–" + lW + "!");

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    winner.getName() + " " + wTag + " wins the match " + wW + "–" + lW + "!",
                    "Match Over", JOptionPane.INFORMATION_MESSAGE);

            // ── Voting ────────────────────────────────────────
            String[] opts = {"Rematch", "End Match"};

            int winnerVote = JOptionPane.showOptionDialog(this,
                    winner.getName() + " " + wTag + " (Winner) — Rematch or End Match?",
                    "Winner's Vote",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opts, opts[0]);

            int loserVote = JOptionPane.showOptionDialog(this,
                    loser.getName() + " " + lTag + " (Loser) — Rematch or End Match?",
                    "Loser's Vote",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, opts, opts[0]);

            // 0 = Rematch, 1 = End Match
            boolean rematch;
            if (winnerVote == loserVote) {
                // Unanimous
                rematch = (winnerVote == 0);
            } else {
                // Disagree — winner has WINNER_VOTE_WEIGHT (70%) probability
                boolean winnerWantsRematch = (winnerVote == 0);
                rematch = Math.random() < WINNER_VOTE_WEIGHT
                        ? winnerWantsRematch   // winner's choice prevails
                        : !winnerWantsRematch; // loser's choice prevails
                String decided = rematch ? "REMATCH" : "END MATCH";
                JOptionPane.showMessageDialog(this,
                        "Players disagreed — fate decides!\nResult: " + decided,
                        "Decision", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();
            if (rematch) {
                // Restart from P2 character selection so both players can repick
                p1Wins = 0; p2Wins = 0; currentRound = 1;
                new PVPBattleFrame(player1);
            } else {
                new MainMenuFrame();
            }
        });
    }

    // ══════════════════════════════════════════════════════════
    //  UI helpers
    // ══════════════════════════════════════════════════════════

    private void refreshUI() {
        if (p1HPBar == null) return;
        p1HPBar.setValue(player1.getCurrentHP());
        p2HPBar.setValue(player2.getCurrentHP());
        p1HPLabel.setText("HP: " + player1.getCurrentHP() + " / " + player1.getMaxHP());
        p2HPLabel.setText("HP: " + player2.getCurrentHP() + " / " + player2.getMaxHP());
        tintBar(p1HPBar, player1, P1_CLR);
        tintBar(p2HPBar, player2, P2_CLR);
        repaint();
    }

    private void tintBar(JProgressBar bar, Character c, Color base) {
        double pct = (double) c.getCurrentHP() / c.getMaxHP();
        if      (pct <= 0.25) bar.setForeground(RED_CRIT);
        else if (pct <= 0.50) bar.setForeground(ORANGE_LOW);
        else                  bar.setForeground(base);
    }

    private void refreshAllCdLabels() {
        if (turnManager == null) return;
        refreshCdRow(player1, p1SkillBtns, p1CdLabels);
        refreshCdRow(player2, p2SkillBtns, p2CdLabels);
    }

    private void refreshCdRow(Character ch, JButton[] btns, JLabel[] lbls) {
        List<Skill> skills = ch.getSkills();
        for (int i = 0; i < btns.length; i++) {
            if (i >= skills.size()) continue;
            int cd = turnManager.getCooldownManager().getRemainingCooldown(ch, i);
            if (cd > 0) {
                lbls[i].setText(cd + " turn(s)");
                lbls[i].setForeground(ORANGE_LOW);
                btns[i].setEnabled(false);
            } else {
                lbls[i].setText("READY");
                lbls[i].setForeground(GREEN_RDY);
            }
        }
    }

    private void setP1Enabled(boolean on) {
        for (JButton b : p1SkillBtns) if (b != null) b.setEnabled(on);
        if (on) refreshCdRow(player1, p1SkillBtns, p1CdLabels);
    }

    private void setP2Enabled(boolean on) {
        for (JButton b : p2SkillBtns) if (b != null) b.setEnabled(on);
        if (on) refreshCdRow(player2, p2SkillBtns, p2CdLabels);
    }

    private void flushResult(TurnResult res) {
        for (String msg : res.getLogMessages()) log(msg);
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (battleLog != null) {
                battleLog.append(msg + "\n");
                battleLog.setCaretPosition(battleLog.getDocument().getLength());
            }
        });
    }

    private static void fullHeal(Character c) { c.heal(c.getMaxHP()); }

    // ── Button factories ──────────────────────────────────────

    private JButton makeSkillButton(String label, Color accent) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover   = getModel().isRollover();
                boolean enabled = isEnabled();
                Color t = hover ? new Color(0x3C, 0x2C, 0x10) : new Color(0x28, 0x1E, 0x0C);
                Color b = hover ? new Color(0x28, 0x1A, 0x08) : new Color(0x18, 0x10, 0x06);
                g2.setPaint(new GradientPaint(0, 0, t, 0, getHeight(), b));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setStroke(new BasicStroke(1.5f));
                g2.setColor(enabled
                        ? new Color(accent.getRed(), accent.getGreen(),
                        accent.getBlue(), hover ? 255 : 180)
                        : new Color(0x50, 0x40, 0x20, 100));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.setFont(new Font("Serif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(new Color(0, 0, 0, 80));
                g2.drawString(getText(), tx+1, ty+1);
                g2.setColor(enabled
                        ? (hover ? new Color(0xFF, 0xE0, 0x80) : CREAM)
                        : new Color(0x60, 0x50, 0x30));
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeDarkButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()
                        ? new Color(0x3C, 0x2C, 0x10) : new Color(0x28, 0x1E, 0x0C));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_CLR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2.setFont(new Font("Serif", Font.BOLD, 14));
                g2.setColor(CREAM);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 40));
        return btn;
    }

    // ── Public accessors (preserve original contract) ─────────
    public Character getPlayer1Character() { return player1; }
    public Character getPlayer2Character() { return player2; }
    public void setPlayer2Character(Character c) { this.player2 = c; }
}