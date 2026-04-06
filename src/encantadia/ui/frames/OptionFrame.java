package encantadia.ui.frames;

import encantadia.ScreenManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class OptionFrame extends JFrame {

    private static final String BG_PATH       = "/welcomeScreen_JAVA.png";
    private static final String HOLDER_PATH   = "/optionsHolder.png";
    private static final String TITLE_PATH    = "/gameTitle.png";
    private static final String BANNER_PATH   = "/optionsButton (1).png";
    private static final String VOLUME_PATH   = "/adjustVolume (1).png";
    private static final String LANGUAGE_PATH = "/setLanguage (1).png";
    private static final String CANCEL_PATH   = "/cancelButton.png";

    private ImagePanel holderPanel;
    private ImagePanel bannerPanel;
    private FloatingTitle titlePanel;

    private JButton volumeButton;
    private JButton languageButton;
    private JButton cancelButton;

    private float time = 0f;

    public OptionFrame() {
        setTitle("Encantadia — Options");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        JLayeredPane lp = new JLayeredPane();
        lp.setLayout(null);
        setContentPane(lp);

        ImagePanel bg = new ImagePanel(BG_PATH);
        lp.add(bg, JLayeredPane.DEFAULT_LAYER);

        titlePanel = new FloatingTitle(TITLE_PATH);
        lp.add(titlePanel, JLayeredPane.PALETTE_LAYER);

        holderPanel = new ImagePanel(HOLDER_PATH);
        lp.add(holderPanel, JLayeredPane.PALETTE_LAYER);

        bannerPanel = new ImagePanel(BANNER_PATH);
        lp.add(bannerPanel, JLayeredPane.MODAL_LAYER);

        volumeButton   = makeImgButton(VOLUME_PATH);
        languageButton = makeImgButton(LANGUAGE_PATH);
        cancelButton   = makeImgButton(CANCEL_PATH);

        JPanel btnLayer = new JPanel(null);
        btnLayer.setOpaque(false);
        lp.add(btnLayer, JLayeredPane.POPUP_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                reposition(lp, bg, btnLayer);
            }
        });

        new Timer(16, e -> {
            time += 0.016f;
            repaint();
        }).start();

        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
        ScreenManager.register(this);

        SwingUtilities.invokeLater(() -> reposition(lp, bg, btnLayer));
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void reposition(JLayeredPane pane, ImagePanel bg, JPanel btnLayer) {
        int W = pane.getWidth();
        int H = pane.getHeight();
        if (W == 0 || H == 0) return;

        bg.setBounds(0, 0, W, H);

        double scale = Math.min(W / 1024.0, H / 768.0);
        scale = Math.min(scale, 1.4);

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

        // QUESTION
        int qW = (int)(holderW * 0.80);
        int qH = (int)(holderH * 0.30);

        int centerX = holderX + (holderW - qW) / 2;
        int shiftLeft = (int)(holderW * 0.10);

        int qX = centerX - shiftLeft;
        int gap = 0;
        int totalBlockH = (qH * 2) + gap;

        int startY = holderY + (holderH - totalBlockH) / 2;
        int q1Y = startY;
        int q2Y = startY + qH + gap;

        volumeButton.setBounds(qX, q1Y, qW, qH);
        languageButton.setBounds(qX, q2Y, qW, qH);

        // CANCEL BUTTON
        int btnH = (int)(60 * scale);
        int cancelW = (int)(btnH * 3.0);

        setFull(cancelButton, cancelW, btnH);

        int cancelX = holderX + (holderW - cancelW) / 2;
        int cancelY = holderY + (int)(holderH * 0.72);

        cancelButton.setBounds(cancelX, cancelY, cancelW, btnH);

        // ADD BUTTONS TO LAYER
        btnLayer.removeAll();
        btnLayer.setLayout(null);
        btnLayer.setBounds(0, 0, W, H);

        btnLayer.add(volumeButton);
        btnLayer.add(languageButton);
        btnLayer.add(cancelButton);

        // LAYERS
        pane.setLayer(titlePanel,  JLayeredPane.PALETTE_LAYER);
        pane.setLayer(holderPanel, JLayeredPane.PALETTE_LAYER);
        pane.setLayer(bannerPanel, JLayeredPane.MODAL_LAYER);
        pane.setLayer(btnLayer,    JLayeredPane.POPUP_LAYER);

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

        return new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                if (img == null) {
                    super.paintComponent(g);
                    return;
                }

                Graphics2D g2 = (Graphics2D) g.create();

                int iw = img.getWidth(null);
                int ih = img.getHeight(null);

                double s = Math.min((double)getWidth()/iw, (double)getHeight()/ih);

                int dw = (int)(iw * s);
                int dh = (int)(ih * s);

                int x = (getWidth() - dw) / 2;
                int y = (getHeight() - dh) / 2;

                g2.drawImage(img, x, y, dw, dh, null);
                g2.dispose();
            }
        };
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(url).getImage();
    }

    // FLOATING TITLE
    private class FloatingTitle extends JPanel {
        private final Image img;

        FloatingTitle(String path) {
            img = loadImage(path);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;

            Graphics2D g2 = (Graphics2D) g.create();

            float floatY = (float)Math.sin(time * 1.2) * 10f;

            g2.drawImage(img, 0, (int)floatY, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }

    private class ImagePanel extends JPanel {
        private final Image img;

        ImagePanel(String path) {
            img = loadImage(path);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }
        }
    }
}
