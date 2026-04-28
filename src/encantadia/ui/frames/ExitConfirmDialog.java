package encantadia.ui.frames;

import encantadia.ScreenManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class ExitConfirmDialog extends JFrame {

    // FIXED: Added "/resources/" prefix to all paths
    private static final String BG_PATH       = "/resources/welcomeScreen_JAVA.png";
    private static final String HOLDER_PATH   = "/resources/optionsHolder.png";
    private static final String YES_PATH      = "/resources/exitButton (1).png"; // Replace with your Yes button if different
    private static final String NO_PATH       = "/resources/cancelButton.png";   // Replace with your No button if different

    private ImagePanel holderPanel;
    private JButton    yesButton;
    private JButton    noButton;

    private Timer animTimer;
    private float time = 0f;

    public ExitConfirmDialog() {
        setTitle("Encantadia — Exit Game?");
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        BgPanel bg = new BgPanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        // Add a dark dim overlay to focus attention
        JPanel dim = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        dim.setOpaque(false);
        lp.add(dim, JLayeredPane.PALETTE_LAYER);

        holderPanel = new ImagePanel(HOLDER_PATH);
        lp.add(holderPanel, JLayeredPane.MODAL_LAYER);

        yesButton = makeImgButton(YES_PATH, "Yes");
        noButton  = makeImgButton(NO_PATH, "No");

        JPanel btnLayer = new JPanel(null);
        btnLayer.setOpaque(false);
        lp.add(btnLayer, JLayeredPane.POPUP_LAYER);

        // Labels
        JLabel questionLbl = new JLabel("Are you sure you want to leave Encantadia?", SwingConstants.CENTER);
        questionLbl.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 24));
        questionLbl.setForeground(new Color(0xC8, 0xA0, 0x28));
        btnLayer.add(questionLbl);

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                reposition(lp, bg, dim, btnLayer, questionLbl);
            }
        });

        animTimer = new Timer(16, e -> {
            time += 0.016f;
            lp.repaint();
        });
        animTimer.start();

        // Actions
        yesButton.addActionListener(e -> System.exit(0));
        noButton.addActionListener(e -> { dispose(); new WelcomeScreenPage(); });

        setVisible(true);
        ScreenManager.register(this);

        SwingUtilities.invokeLater(() -> reposition(lp, bg, dim, btnLayer, questionLbl));
    }

    @Override
    public void dispose() {
        if (animTimer != null) animTimer.stop();
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void reposition(JLayeredPane pane, BgPanel bg, JPanel dim, JPanel btnLayer, JLabel questionLbl) {
        int W = pane.getWidth(), H = pane.getHeight();
        if (W == 0 || H == 0) return;

        bg.setBounds(0, 0, W, H);
        dim.setBounds(0, 0, W, H);

        double scale = Math.min(W / 1024.0, H / 768.0);

        // HOLDER
        int holderW = (int)(600 * scale);
        int holderH = (int)(holderW / 2.0);
        int holderX = (W - holderW) / 2;
        int holderY = (H - holderH) / 2;
        holderPanel.setBounds(holderX, holderY, holderW, holderH);

        // TEXT
        int lblH = (int)(40 * scale);
        questionLbl.setBounds(holderX, holderY + (int)(holderH * 0.25), holderW, lblH);
        questionLbl.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, Math.max(16, (int)(24*scale))));

        // BUTTONS
        int btnW = (int)(180 * scale);
        int btnH = (int)(60 * scale);

        int gap = (int)(20 * scale);
        int totalBtnW = (btnW * 2) + gap;
        int startX = holderX + (holderW - totalBtnW) / 2;
        int btnY = holderY + (int)(holderH * 0.60);

        yesButton.setBounds(startX, btnY, btnW, btnH);
        noButton.setBounds(startX + btnW + gap, btnY, btnW, btnH);

        btnLayer.removeAll();
        btnLayer.setBounds(0, 0, W, H);
        btnLayer.add(questionLbl);
        btnLayer.add(yesButton);
        btnLayer.add(noButton);

        pane.revalidate();
        pane.repaint();
    }

    private JButton makeImgButton(String path, String fallback) {
        Image img = loadImage(path);
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                if (img == null) {
                    g2.setColor(new Color(0xC8, 0xA0, 0x28));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.BLACK);
                    g2.drawString(fallback, getWidth()/2 - 15, getHeight()/2 + 5);
                    return;
                }

                double s = Math.min((double)getWidth()/img.getWidth(null), (double)getHeight()/img.getHeight(null));
                if (getModel().isRollover()) s *= 1.05;

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