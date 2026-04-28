package encantadia.ui.frames;

import encantadia.ScreenManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class OptionFrame extends JFrame {

    // FIXED: Added "/resources/" prefix to all paths
    private static final String BG_PATH       = "/resources/welcomeScreen_JAVA.png";
    private static final String HOLDER_PATH   = "/resources/optionsHolder.png";
    private static final String TITLE_PATH    = "/resources/gameTitle.png";
    private static final String BANNER_PATH   = "/resources/optionsButton (1).png";
    private static final String VOLUME_PATH   = "/resources/adjustVolume (1).png";
    private static final String LANGUAGE_PATH = "/resources/setLanguage (1).png";
    private static final String CANCEL_PATH   = "/resources/cancelButton.png";

    private ImagePanel holderPanel;
    private ImagePanel bannerPanel;
    private FloatingTitle titlePanel;

    private JButton volumeButton;
    private JButton languageButton;
    private JButton cancelButton;

    private Timer animTimer;
    private float time = 0f;

    public OptionFrame() {
        setTitle("Encantadia — Options");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        titlePanel = new FloatingTitle(TITLE_PATH);
        lp.add(titlePanel, JLayeredPane.PALETTE_LAYER);

        holderPanel = new ImagePanel(HOLDER_PATH);
        lp.add(holderPanel, JLayeredPane.MODAL_LAYER);

        bannerPanel = new ImagePanel(BANNER_PATH);
        lp.add(bannerPanel, JLayeredPane.POPUP_LAYER);

        volumeButton   = makeImgButton(VOLUME_PATH);
        languageButton = makeImgButton(LANGUAGE_PATH);
        cancelButton   = makeImgButton(CANCEL_PATH);

        JPanel btnLayer = new JPanel(null);
        btnLayer.setOpaque(false);
        lp.add(btnLayer, JLayeredPane.DRAG_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reposition(lp, bg, btnLayer);
            }
        });

        animTimer = new Timer(16, e -> {
            time += 0.016f;
            lp.repaint();
        });
        animTimer.start();

        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
        ScreenManager.register(this);

        SwingUtilities.invokeLater(() -> reposition(lp, bg, btnLayer));
    }

    @Override
    public void dispose() {
        if (animTimer != null) animTimer.stop();
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void reposition(JLayeredPane pane, BgPanel bg, JPanel btnLayer) {
        int W = pane.getWidth(), H = pane.getHeight();
        if (W == 0 || H == 0) return;

        bg.setBounds(0, 0, W, H);

        double scale = Math.min(W / 1024.0, H / 768.0);

        // TITLE
        int titleW = (int)(500 * scale);
        int titleH = (int)(titleW * 0.30);
        int titleX = (W - titleW) / 2;
        int titleY = (int)(40 * scale);
        titlePanel.setBounds(titleX, titleY, titleW, titleH);

        // HOLDER
        int holderW = (int)(600 * scale);
        int holderH = (int)(holderW / 2.0);
        int holderX = (W - holderW) / 2;
        int holderY = (H - holderH) / 2;
        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        // BANNER
        int bannerW = (int)(200 * scale);
        int bannerH = (int)(bannerW / 2.5);
        int bannerX = holderX + (holderW - bannerW) / 2;
        int bannerY = holderY - (bannerH / 3);
        bannerPanel.setBounds(bannerX, bannerY, bannerW, bannerH);

        // BUTTONS
        int qW = (int)(holderW * 0.80);
        int qH = (int)(holderH * 0.30);
        int centerX = holderX + (holderW - qW) / 2;
        int shiftLeft = (int)(holderW * 0.10);
        int qX = centerX - shiftLeft;
        int startY = holderY + (int)(holderH * 0.15);

        volumeButton.setBounds(qX, startY, qW, qH);
        languageButton.setBounds(qX, startY + qH, qW, qH);

        // CANCEL BUTTON
        int btnH = (int)(60 * scale);
        int cancelW = (int)(btnH * 3.0);
        int cancelX = holderX + (holderW - cancelW) / 2;
        int cancelY = holderY + (int)(holderH * 0.72);

        cancelButton.setBounds(cancelX, cancelY, cancelW, btnH);

        btnLayer.removeAll();
        btnLayer.setBounds(0, 0, W, H);
        btnLayer.add(volumeButton);
        btnLayer.add(languageButton);
        btnLayer.add(cancelButton);

        pane.revalidate();
        pane.repaint();
    }

    private JButton makeImgButton(String path) {
        Image img = loadImage(path);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                if (img == null) { super.paintComponent(g); return; }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                double s = Math.min((double)getWidth()/img.getWidth(null), (double)getHeight()/img.getHeight(null));
                if (getModel().isRollover()) s *= 1.05; // Gentle hover scale

                int dw = (int)(img.getWidth(null) * s);
                int dh = (int)(img.getHeight(null) * s);
                int x = (getWidth() - dw) / 2, y = (getHeight() - dh) / 2;

                g2.drawImage(img, x, y, dw, dh, null);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) { System.err.println("Missing: " + path); return null; }
        return new ImageIcon(url).getImage();
    }

    protected void drawImageFill(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null); if (iw <= 0 || ih <= 0) return;
        double scale = Math.max((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    protected void drawImageProportional(Graphics2D g2, Image img, int x, int y, int w, int h) {
        if (img == null) return;
        int iw = img.getWidth(null), ih = img.getHeight(null); if (iw <= 0 || ih <= 0) return;
        double scale = Math.min((double) w / iw, (double) h / ih);
        int dw = (int) (iw * scale), dh = (int) (ih * scale);
        g2.drawImage(img, x + (w - dw) / 2, y + (h - dh) / 2, dw, dh, null);
    }

    private class BgPanel extends JPanel {
        private final Image img;
        BgPanel(String p) { img = loadImage(p); setOpaque(true); setBackground(Color.BLACK); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawImageFill(g2, img, 0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }

    private class FloatingTitle extends JPanel {
        private final Image img;
        FloatingTitle(String path) { img = loadImage(path); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            float floatY = (float)Math.sin(time * 1.2) * 10f;
            drawImageProportional(g2, img, 0, (int)floatY, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private class ImagePanel extends JPanel {
        private final Image img;
        ImagePanel(String path) { img = loadImage(path); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            drawImageProportional(g2, img, 0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}