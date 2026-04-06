package encantadia.gamemode;

import encantadia.BackstoryShowcase;
import encantadia.ScreenManager;
import encantadia.story.StoryType;
import encantadia.ui.frames.CharacterSelectionFrame;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public abstract class BaseModeScreen extends JFrame {

    protected static final String HOLDER_PATH   = "/resources/mainMenuHolder.png";
    protected static final String MODE_BTN_PATH = "/resources/chosenGameModeButton.png";
    protected static final String QUESTION_PATH = "/resources/showBackstoryQuestion.png";
    protected static final String BTN_YES       = "/resources/yesButton (1).png";
    protected static final String BTN_NO        = "/resources/noButton (1).png";
    protected static final String BTN_EXIT      = "/resources/exitButton (3).png";

    // ✅ UPDATED BALANCED RATIOS
    private static final double HOLDER_W_PCT  = 0.52;
    private static final double HOLDER_RATIO  = 1.10; // less stretched
    private static final double HOLDER_Y_PCT  = 0.08;
    private static final double MAX_H_PCT     = 0.85;

    private static final double BANNER_W_PCT  = 0.72;
    private static final double BANNER_H_RATIO= 0.20;

    private static final double Q_W_PCT       = 0.70;
    private static final double Q_H_RATIO     = 0.42;
    private static final double Q_Y_PCT       = 0.25; // 🔼 moved UP

    private static final double BTN_H_PCT     = 0.10;
    private static final int    BTN_H_MIN     = 34;
    private static final int    BTN_H_MAX     = 70;
    private static final double YES_W_RATIO   = 2.5;
    private static final double NO_W_RATIO    = 2.0;
    private static final double GAP_PCT       = 0.05;
    private static final double YESNO_Y_PCT   = 0.58; // 🔼 moved UP

    private static final double EXIT_H_PCT    = 0.07;
    private static final int    EXIT_H_MIN    = 28;
    private static final int    EXIT_H_MAX    = 55;
    private static final double EXIT_W_RATIO  = 2.2;
    private static final double EXIT_GAP_PCT  = 0.004; // 🔽 tighter gap

    private JButton yesButton, noButton, exitButton;
    private ImagePanel holderPanel, modeBtnPanel, questionPanel;
    private JPanel yesNoRow, exitRow;

    protected abstract String getBackgroundPath();
    protected abstract String getWindowTitle();
    protected abstract StoryType getStoryType();
    protected abstract GameModeType getGameModeType();

    protected void init() {
        setTitle(getWindowTitle());
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        ImagePanel bg = new ImagePanel(getBackgroundPath());
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        holderPanel = new ImagePanel(HOLDER_PATH);
        lp.add(holderPanel, JLayeredPane.PALETTE_LAYER);

        modeBtnPanel = new ImagePanel(MODE_BTN_PATH);
        lp.add(modeBtnPanel, JLayeredPane.MODAL_LAYER);

        questionPanel = new ImagePanel(QUESTION_PATH);
        lp.add(questionPanel, JLayeredPane.MODAL_LAYER);

        yesButton = makeImageButton(BTN_YES);
        noButton  = makeImageButton(BTN_NO);

        yesNoRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        yesNoRow.setOpaque(false);
        lp.add(yesNoRow, JLayeredPane.POPUP_LAYER);

        exitButton = makeImageButton(BTN_EXIT);
        exitRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        exitRow.setOpaque(false);
        exitRow.add(exitButton);
        lp.add(exitRow, JLayeredPane.POPUP_LAYER);

        // Actions
        yesButton.addActionListener(e -> {
            dispose();
            new BackstoryShowcase(getStoryType(), getGameModeType());
        });

        noButton.addActionListener(e -> {
            dispose();
            new CharacterSelectionFrame(getGameModeType());
        });

        exitButton.addActionListener(e -> {
            dispose();
            new MainMenuFrame();
        });

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reposition(lp, bg);
            }
        });

        setVisible(true);
        ScreenManager.register(this);
        SwingUtilities.invokeLater(() -> reposition(lp, bg));
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void reposition(JLayeredPane pane, ImagePanel bg) {
        int W = pane.getWidth();
        int H = pane.getHeight();
        if (W == 0 || H == 0) return;

        bg.setBounds(0, 0, W, H);

        // ── SCALE ───────────────────────────────────────
        double scale = Math.min(W / 1024.0, H / 768.0);
        scale = Math.min(scale, 1.4);

        // ── HOLDER ──────────────────────────────────────
        int holderW = (int)(590 * scale);
        int holderH = (int)(holderW * 1.2);
        int holderX = (W - holderW) / 2;
        int holderY = (H - holderH) / (int)1.5;

        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        // ── BANNER: TOP CENTER (INSIDE HOLDER) ──────────
        int bannerW = (int)(270 * scale);
        int bannerH = (int)(100 * scale);
        int bannerX = holderX + (holderW - bannerW) / 2;
        int bannerY = holderY + (int)(14 * scale);

        modeBtnPanel.setBounds(bannerX, bannerY, bannerW, bannerH);

        // ── QUESTION: CENTER (slightly higher) ──────────
        int qW = (int)(320 * scale);
        int qH = (int)(140 * scale);
        int qX = holderX + (holderW - qW) / 2;
        int qY = holderY + (holderH / 2) - qH - (int)(14 * scale);

        questionPanel.setBounds(qX, qY, qW, qH);

        // ── YES / NO BUTTONS: BELOW QUESTION ────────────
        int btnH = (int)(55 * scale);
        int yesW = (int)(btnH * 2.5);
        int noW  = (int)(btnH * 2.5);
        int gap  = (int)(50 * scale);

        setFull(yesButton, yesW, btnH);
        setFull(noButton,  noW,  btnH);

        int rowW = yesW + noW + gap;
        int rowX = holderX + (holderW - rowW) / 2;
        int rowY = qY + qH + (int)(50 * scale);

        yesNoRow.removeAll();
        yesNoRow.add(yesButton);
        yesNoRow.add(Box.createHorizontalStrut(gap));
        yesNoRow.add(noButton);
        yesNoRow.setBounds(rowX, rowY, rowW, btnH + 4);

        // ── EXIT: BOTTOM INSIDE HOLDER ──────────────────
        int exitW = (int)(200 * scale);
        int exitH = (int)(90 * scale);

        setFull(exitButton, exitW, exitH);

        int exitX = holderX + (holderW - exitW) / 2;
        int exitY = holderY + holderH - exitH - (int)(30 * scale);

        exitRow.removeAll();
        exitRow.add(exitButton);
        exitRow.setBounds(exitX, exitY, exitW, exitH + 4);

        // ── LAYERS ─────────────────────────────────────
        pane.setLayer(holderPanel,   JLayeredPane.PALETTE_LAYER);
        pane.setLayer(modeBtnPanel,  JLayeredPane.MODAL_LAYER);
        pane.setLayer(questionPanel, JLayeredPane.MODAL_LAYER);
        pane.setLayer(yesNoRow,      JLayeredPane.POPUP_LAYER);
        pane.setLayer(exitRow,       JLayeredPane.POPUP_LAYER);

        pane.revalidate();
        pane.repaint();
    }

    private static void setFull(JButton b, int w, int h) {
        Dimension d = new Dimension(w, h);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(val, max));
    }

    private static void setSz(JButton b, int w, int h) {
        Dimension d = new Dimension(w, h);
        b.setPreferredSize(d);
    }

    private JButton makeImageButton(String path) {
        Image img = loadImage(path);

        JButton btn = new JButton() {
            protected void paintComponent(Graphics g) {
                if (img == null) return;
                g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }
        };

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    protected Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(url).getImage();
    }

    protected class ImagePanel extends JPanel {
        private final Image img;

        ImagePanel(String path) {
            img = loadImage(path);
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}
