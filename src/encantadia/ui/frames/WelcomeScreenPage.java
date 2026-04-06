package encantadia.ui.frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import encantadia.ui.frames.MainMenuFrame;
import encantadia.ScreenManager;
import encantadia.story.StoryType;

public class WelcomeScreenPage extends JFrame {

    private static final String BG_PATH      = "/resources/welcomeScreen_JAVA.png";
    private static final String TITLE_PATH   = "/resources/gameTitle.png";
    private static final String BTN_PLAY     = "/resources/playButton (1).png";
    private static final String BTN_OPTIONS  = "/resources/optionsButton (1).png";
    private static final String BTN_EXIT     = "/resources/exitButton (1).png";


    private JButton playButton;
    private JButton optionButton;
    private JButton exitButton;

    private Timer animationTimer;
    private float time = 0f;

    private List<Particle> particles = new ArrayList<>();

    public WelcomeScreenPage() {

        setTitle("Encantadia: Echoes of the Gems");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);

        BackgroundPanel bgPanel = new BackgroundPanel(BG_PATH);
        layeredPane.add(bgPanel, JLayeredPane.DEFAULT_LAYER);

        ScaledImagePanel titlePanel = new ScaledImagePanel(TITLE_PATH);
        layeredPane.add(titlePanel, JLayeredPane.PALETTE_LAYER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(null);

        playButton    = createImageButton(BTN_PLAY,    "Play");
        optionButton  = createImageButton(BTN_OPTIONS, "Options");
        exitButton    = createImageButton(BTN_EXIT,    "Exit");

        buttonsPanel.add(playButton);
        buttonsPanel.add(optionButton);
        buttonsPanel.add(exitButton);

        layeredPane.add(buttonsPanel, JLayeredPane.MODAL_LAYER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionLayers(layeredPane, bgPanel, titlePanel, buttonsPanel);
            }
        });

        playButton.addActionListener(e -> {
            new MainMenuFrame();
            dispose();
        });

        optionButton.addActionListener(e -> {
            new OptionFrame();
        });

        exitButton.addActionListener(e -> {
            dispose();
            new ExitConfirmDialog();
        });

        for (int i = 0; i < 70; i++) {
            particles.add(new Particle(1024, 768));
        }

        setVisible(true);
        ScreenManager.register(this);

        SwingUtilities.invokeLater(() ->
                repositionLayers(layeredPane, bgPanel, titlePanel, buttonsPanel)
        );

        startAnimations();
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    private void startAnimations() {
        animationTimer = new Timer(16, e -> {
            time += 0.016f;
            repaint();
        });
        animationTimer.start();
    }

    private void repositionLayers(JLayeredPane pane,
                                  JPanel bg,
                                  JPanel title,
                                  JPanel buttons) {

        int w = pane.getWidth();
        int h = pane.getHeight();
        if (w == 0 || h == 0) return;

        double scale = Math.min(w / 1024.0, h / 768.0);
        scale = Math.min(scale, 1.5);

        bg.setBounds(0, 0, w, h);

        int titleW = (int)(500 * scale);
        int titleH = (int)(titleW * 0.38);
        int titleX = (w - titleW) / 2;
        int titleY = (int)(50 * scale);

        title.setBounds(titleX, titleY, titleW, titleH);

        int btnW = (int)(190 * scale);
        int btnH = (int)(btnW * 0.40);
        int gap  = (int)(40 * scale);

        int totalH = (btnH * 3) + (gap * 2);
        int startY = (int)(h * 0.60 - totalH / 2);
        int centerX = w / 2 - btnW / 2;

        playButton.setBounds(centerX, startY, btnW, btnH);
        optionButton.setBounds(centerX, startY + btnH + gap, btnW, btnH);
        exitButton.setBounds(centerX, startY + (btnH + gap) * 2, btnW, btnH);

        buttons.setBounds(0, 0, w, h);
    }

    private JButton createImageButton(String imagePath, String fallbackText) {
        JButton btn = new JButton() {
            private final Image img = loadImage(imagePath);
            private float scale = 1.0f;
            private float glowPulse = 0f;

            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { scale = 1.08f; }
                    public void mouseExited(MouseEvent e)  { scale = 1.0f; }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                int w = getWidth();
                int h = getHeight();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                //smooth pulsing glow
                glowPulse += 0.04f;
                float alpha = 0.5f + (float)Math.sin(glowPulse) * 0.25f;

                //outer glow
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, alpha));
                g2.setColor(new Color(153, 102, 204));
                g2.fillRoundRect(0, 0, w, h, 25, 25);

                //inner plate
                g2.setComposite(AlphaComposite.SrcOver);
                g2.setColor(new Color(40, 30, 60, 200));
                g2.fillRoundRect(4, 4, w - 8, h - 8, 20, 20);

                // button image (scaled on hover)
                int dw = (int)(w * scale);
                int dh = (int)(h * scale);
                int dx = (w - dw) / 2;
                int dy = (h - dh) / 2;

                if (img != null) {
                    g2.drawImage(img, dx, dy, dw, dh, null);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.drawString(fallbackText, w / 2 - 20, h / 2);
                }

                // mystical shimmer line (very subtle)
                GradientPaint shimmer = new GradientPaint(
                        0, 0,
                        new Color(255, 255, 255, 40),
                        w, h,
                        new Color(255, 255, 255, 0)
                );
                g2.setPaint(shimmer);
                g2.fillRoundRect(4, 4, w - 8, h - 8, 20, 20);

                g2.dispose();
            }
        };

        btn.setText(fallbackText);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) return null;
        return new ImageIcon(url).getImage();
    }

    private class Particle {
        float x, y, size, speedY, phase;

        Particle(int width, int height) {
            x = (float)(Math.random() * width);
            y = (float)(Math.random() * height);
            size = (float)(2 + Math.random() * 3);
            speedY = (float)(0.1 + Math.random() * 0.4);
            phase = (float)(Math.random() * Math.PI * 2);
        }

        void update(int width, int height) {
            y -= speedY;
            x += Math.sin(phase) * 0.3f; // ✨ drifting motion
            phase += 0.04f;

            if (y < 0) {
                y = height;
                x = (float)(Math.random() * width);
            }
        }

        float getAlpha() {
            return (float)(0.5 + 0.5 * Math.sin(phase));
        }
    }

    private class BackgroundPanel extends JPanel {
        private final Image img;
        private float time = 0;

        BackgroundPanel(String path) {
            img = loadImage(path);
            new Timer(16, e -> {
                time += 0.016f;
                repaint();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            int w = getWidth();
            int h = getHeight();

            if (img != null) {
                g2.drawImage(img, 0, 0, w, h, null);
            }

            // CLOUD LAYERS


            // VIGNETTE
            g2.setPaint(new RadialGradientPaint(
                    new Point(w/2, h/2),
                    w * 0.8f,
                    new float[]{0f, 1f},
                    new Color[]{
                            new Color(0,0,0,0),
                            new Color(0,0,0,150)
                    }
            ));
            g2.fillRect(0, 0, w, h);

            g2.dispose();
        }
    }

    private class ScaledImagePanel extends JPanel {
        private final Image img;

        ScaledImagePanel(String path) {
            img = loadImage(path);
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();

                float floatY = (float)Math.sin(time * 1.2) * 10f;

                g2.drawImage(img,
                        0,
                        (int)floatY,
                        getWidth(),
                        getHeight(),
                        null);

                g2.dispose();
            }
        }
    }
}
