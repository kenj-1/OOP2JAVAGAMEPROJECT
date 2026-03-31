package encantadia.ui.frames;

import encantadia.ScreenManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class ExitConfirmDialog extends JFrame {

    private static final String BG_PATH       = "/resources/welcomeScreen_JAVA.png";
    private static final String HOLDER_PATH   = "/resources/optionsHolder.png";
    private static final String BANNER_PATH   = "/resources/exitButton (1).png";
    private static final String QUESTION_PATH = "/resources/exitGame.png";
    private static final String CANCEL_PATH   = "/resources/cancelButton.png";
    private static final String EXITGAME_PATH = "/resources/exitButton (2).png";

    private ImagePanel holderPanel;
    private ImagePanel bannerPanel;
    private ImagePanel questionPanel;
    private JButton    cancelButton;
    private JButton    exitGameButton;
    private JPanel     btnRow;

    public ExitConfirmDialog() {
        setTitle("Encantadia — Exit");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        // ── Layer 0: background ───────────────────────────────
        ImagePanel bg = new ImagePanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        // ── Layer 1: oval holder ──────────────────────────────
        holderPanel = new ImagePanel(HOLDER_PATH);
        lp.add(holderPanel, JLayeredPane.PALETTE_LAYER);

        // ── Layer 2: EXIT banner — straddles top of holder ────
        bannerPanel = new ImagePanel(BANNER_PATH);
        lp.add(bannerPanel, JLayeredPane.MODAL_LAYER);

        // ── Layer 3: question text ────────────────────────────
        questionPanel = new ImagePanel(QUESTION_PATH);
        lp.add(questionPanel, JLayeredPane.MODAL_LAYER);

        // ── Layer 4: buttons ──────────────────────────────────
        cancelButton   = makeImgButton(CANCEL_PATH);
        exitGameButton = makeImgButton(EXITGAME_PATH);

        btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(cancelButton);
        btnRow.add(exitGameButton);
        lp.add(btnRow, JLayeredPane.POPUP_LAYER);

        // ── Actions ───────────────────────────────────────────
        // ✅ Cancel goes back to WelcomeScreenPage
        cancelButton.addActionListener(e -> {
            dispose();
            new WelcomeScreenPage();
        });

        exitGameButton.addActionListener(e -> System.exit(0));

        // ── Resize ────────────────────────────────────────────
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reposition(lp, bg);
            }
        });

        // Apply fullscreen if already active
        if (ScreenManager.isFullscreen()) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

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

        // Scale from base 1024x768, capped at 1.4x
        double scale = Math.min(W / 1024.0, H / 768.0);
        scale = Math.min(scale, 1.4);

        // ── Holder: optionsHolder is a wide oval ~2.5:1 ───────
        int holderW = (int)(500 * scale);
        int holderH = (int)(holderW / 2.5);
        int holderX = (W - holderW) / 2;
        int holderY = (H - holderH) / 2;
        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        // ── Banner: EXIT stone slab straddles top of holder ───
        // exitButton(1) is ~3.5:1
        int bannerW = (int)(200 * scale);
        int bannerH = (int)(bannerW / 3.5);
        int bannerX = holderX + (holderW - bannerW) / 2;
        int bannerY = holderY - (bannerH / 2);
        bannerPanel.setBounds(bannerX, bannerY, bannerW, bannerH);

        // ── Question text: upper-center of holder ─────────────
        // exitGame.png ~5:1 wide
        int qW = (int)(360 * scale);
        int qH = (int)(qW / 5.0);
        int qX = holderX + (holderW - qW) / 2;
        int qY = holderY + (int)(holderH * 0.28);
        questionPanel.setBounds(qX, qY, qW, qH);

        // ── CANCEL + EXIT GAME: lower portion of holder ───────
        // cancelButton ~3:1, exitButton(2) ~5:1
        int btnH   = (int)(38 * scale);
        int cancelW  = (int)(btnH * 3.2);
        int exitGW   = (int)(btnH * 5.0);
        int gap      = (int)(24 * scale);

        setFull(cancelButton,   cancelW, btnH);
        setFull(exitGameButton, exitGW,  btnH);

        int rowW = cancelW + exitGW + gap;
        int rowX = holderX + (holderW - rowW) / 2;
        int rowY = holderY + (int)(holderH * 0.62);

        btnRow.removeAll();
        btnRow.add(cancelButton);
        btnRow.add(Box.createHorizontalStrut(gap));
        btnRow.add(exitGameButton);
        btnRow.setBounds(rowX, rowY, rowW, btnH + 4);

        // Ensure z-order
        pane.setLayer(holderPanel,   JLayeredPane.PALETTE_LAYER);
        pane.setLayer(bannerPanel,   JLayeredPane.MODAL_LAYER);
        pane.setLayer(questionPanel, JLayeredPane.MODAL_LAYER);
        pane.setLayer(btnRow,        JLayeredPane.POPUP_LAYER);

        pane.revalidate();
        pane.repaint();
    }

    private static void setFull(JButton b, int w, int h) {
        Dimension d = new Dimension(w, h);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
    }

    private JButton makeImgButton(String path) {
        Image img = loadImage(path);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                if (img == null) { super.paintComponent(g); return; }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int iw = img.getWidth(null), ih = img.getHeight(null);
                if (iw <= 0 || ih <= 0) { g2.dispose(); return; }
                double s = Math.min((double)getWidth()/iw, (double)getHeight()/ih);
                int dw = (int)(iw*s), dh = (int)(ih*s);
                int x  = (getWidth()-dw)/2, y = (getHeight()-dh)/2;
                if (getModel().isRollover())
                    g2.setComposite(AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 0.80f));
                g2.drawImage(img, x, y, dw, dh, null);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    private class ImagePanel extends JPanel {
        private final Image img;
        ImagePanel(String path) { img = loadImage(path); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }
}