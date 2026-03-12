package encantadia.ui.frames;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import encantadia.BackstoryShowcase;
import encantadia.story.StoryType;

public class WelcomeScreenPage extends JFrame {

    // ── Asset paths ───────────────────────────────────────────
    private static final String BG_PATH      = "/resources/welcomeScreen_JAVA.png";
    private static final String TITLE_PATH   = "/resources/gameTitle.png";
    private static final String BTN_PLAY     = "/resources/playButton.png";
    private static final String BTN_OPTIONS  = "/resources/optionsButton.png";
    private static final String BTN_EXIT     = "/resources/exitButton.png";

    // ── Form-bound fields ─────────────────────────────────────
    private JButton playButton;
    private JButton optionButton;
    private JButton exitButton;


    public WelcomeScreenPage() {


        setTitle("Encantadia: Echoes of the Gems");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        // ── Layered pane — absolute positioning ───────────────
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);

        // ── Layer 0: Background ───────────────────────────────
        BackgroundPanel bgPanel = new BackgroundPanel(BG_PATH);
        layeredPane.add(bgPanel, JLayeredPane.DEFAULT_LAYER);

        // ── Layer 1: Title image ──────────────────────────────
        ScaledImagePanel titlePanel = new ScaledImagePanel(TITLE_PATH);
        layeredPane.add(titlePanel, JLayeredPane.PALETTE_LAYER);

        // ── Layer 2: Buttons panel ────────────────────────────
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        playButton    = createImageButton(BTN_PLAY,    "Play");
        optionButton  = createImageButton(BTN_OPTIONS, "Options");
        exitButton    = createImageButton(BTN_EXIT,    "Exit");

        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(centerButton(playButton));
        buttonsPanel.add(Box.createVerticalStrut(14));
        buttonsPanel.add(centerButton(optionButton));
        buttonsPanel.add(Box.createVerticalStrut(14));
        buttonsPanel.add(centerButton(exitButton));
        buttonsPanel.add(Box.createVerticalGlue());

        layeredPane.add(buttonsPanel, JLayeredPane.MODAL_LAYER);

        // ── Resize listener ───────────────────────────────────
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionLayers(layeredPane, bgPanel, titlePanel, buttonsPanel);
            }
        });

        // ── Button actions ────────────────────────────────────
        playButton.addActionListener(e -> {

            new BackstoryShowcase(StoryType.GAME_LORE);
            dispose();

        });

        optionButton.addActionListener(e -> {
            // TODO: new OptionFrame(); dispose();
            JOptionPane.showMessageDialog(this, "Options coming soon!");
        });

        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);

        // ✅ Reposition after frame has real dimensions
        SwingUtilities.invokeLater(() ->
                repositionLayers(layeredPane, bgPanel, titlePanel, buttonsPanel)
        );
    }

    // ── Layer positioning ─────────────────────────────────────

    private void repositionLayers(JLayeredPane pane,
                                  JPanel bg,
                                  JPanel title,
                                  JPanel buttons) {
        int w = pane.getWidth();
        int h = pane.getHeight();
        if (w == 0 || h == 0) return;

        // Background fills everything
        bg.setBounds(0, 0, w, h);

        // Title: upper-center, 55% of frame width
        int titleW = (int)(w * 0.55);
        int titleH = (int)(titleW * 0.30); // ratio matches the gameTitle.png proportions
        int titleX = (w - titleW) / 2;
        int titleY = (int)(h * 0.15);
        title.setBounds(titleX, titleY, titleW, titleH);

        // Buttons: scale to frame
        int btnW = (int)(w * 0.26);
        int btnH = (int)(btnW * 0.20);

        playButton.setPreferredSize(new Dimension(btnW, btnH));
        playButton.setMaximumSize(new Dimension(btnW, btnH));
        optionButton.setPreferredSize(new Dimension(btnW, btnH));
        optionButton.setMaximumSize(new Dimension(btnW, btnH));
        exitButton.setPreferredSize(new Dimension(btnW, btnH));
        exitButton.setMaximumSize(new Dimension(btnW, btnH));

        // Buttons panel: centered in lower half
        int btnGap     = 14;
        int btnPanelH  = (btnH * 3) + (btnGap * 2) + 20;
        int btnPanelW  = btnW + 20;
        int btnPanelX  = (w - btnPanelW) / 2;
        int btnPanelY  = (int)(h * 0.52);
        buttons.setBounds(btnPanelX, btnPanelY, btnPanelW, btnPanelH);

        buttons.revalidate();
        buttons.repaint();
    }

    // ── Helpers ───────────────────────────────────────────────

    private JPanel centerButton(JButton btn) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(btn);
        return wrapper;
    }

    private JButton createImageButton(String imagePath, String fallbackText) {
        JButton btn = new JButton() {
            private final Image img = loadImage(imagePath);

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (img != null) {
                    if (getModel().isRollover()) {
                        // Slight dim on hover
                        g2.setComposite(AlphaComposite.getInstance(
                                AlphaComposite.SRC_OVER, 0.80f));
                    }
                    g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Fallback: dark rounded button
                    g2.setColor(new Color(40, 40, 50, 200));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                    int ty = (getHeight() + fm.getAscent()) / 2 - 2;
                    g2.drawString(getText(), tx, ty);
                }
                g2.dispose();
            }
        };

        btn.setText(fallbackText);
        btn.setPreferredSize(new Dimension(260, 52));
        btn.setMaximumSize(new Dimension(260, 52));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Image not found: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // ── Inner panels ──────────────────────────────────────────

    private class BackgroundPanel extends JPanel {
        private final Image img;

        BackgroundPanel(String path) {
            img = loadImage(path);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            } else {
                setBackground(Color.BLACK);
            }
        }
    }

    private class ScaledImagePanel extends JPanel {
        private final Image img;

        ScaledImagePanel(String path) {
            img = loadImage(path);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        }
    }

    private void createUIComponents() {
        // Not used — UI built programmatically
    }
}