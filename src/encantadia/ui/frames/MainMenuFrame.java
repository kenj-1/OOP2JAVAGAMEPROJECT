package encantadia.ui.frames;

import encantadia.gamemode.ArcadeMode;
import encantadia.gamemode.PVEMode;
import encantadia.gamemode.PVPMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class MainMenuFrame extends JFrame {

    // ── Form-bound fields (keep these — matched to .form field names) ──
    private JPanel MainMenuFramePanel;
    private JButton arcadeButton;
    private JButton PVPButton;
    private JButton PVEButton;
    private JButton exitGameButton;

    // ── Asset paths — put all images in src/assets/images/ ────────────
    private static final String BG_PATH      = "/resources/background.png";
    private static final String COLUMNS_PATH = "/resources/columns.png";
    private static final String TITLE_PATH   = "/resources/mainMenu (2).png";
    private static final String BTN_ARCADE   = "/resources/arcadeButton.png";
    private static final String BTN_PVE      = "/resources/pveButton.png";
    private static final String BTN_PVP      = "/resources/pvpButton.png";

    public MainMenuFrame() {
        setContentPane(MainMenuFramePanel);
        setTitle("Encantadia: Echoes of the Gem - Main Menu");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setResizable(true);

        // ── Build the layered content pane ─────────────────────────────
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null); // absolute layout — we reposition on resize
        setContentPane(layeredPane);

        // ── Layer 0: Background (waterfall) ───────────────────────────
        BackgroundPanel backgroundPanel = new BackgroundPanel(BG_PATH);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        // ── Layer 1: Columns overlay ───────────────────────────────────
        ScaledImagePanel columnsPanel = new ScaledImagePanel(COLUMNS_PATH);
        layeredPane.add(columnsPanel, JLayeredPane.PALETTE_LAYER);

        // ── Layer 2: Title image (MAIN MENU text) ─────────────────────
        ScaledImagePanel titlePanel = new ScaledImagePanel(TITLE_PATH);
        layeredPane.add(titlePanel, JLayeredPane.MODAL_LAYER);

        // ── Layer 3: Buttons panel ─────────────────────────────────────
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        arcadeButton = createImageButton(BTN_ARCADE, "Arcade");
        PVEButton    = createImageButton(BTN_PVE,    "Player V.S Enemy");
        PVPButton    = createImageButton(BTN_PVP,    "Player V.S Player");
        exitGameButton = createTextButton("Back to Main Menu");

        buttonsPanel.add(Box.createVerticalGlue());
        buttonsPanel.add(centerButton(arcadeButton));
        buttonsPanel.add(Box.createVerticalStrut(16));
        buttonsPanel.add(centerButton(PVEButton));
        buttonsPanel.add(Box.createVerticalStrut(16));
        buttonsPanel.add(centerButton(PVPButton));
        buttonsPanel.add(Box.createVerticalStrut(16));
        buttonsPanel.add(centerButton(exitGameButton));
        buttonsPanel.add(Box.createVerticalGlue());

        layeredPane.add(buttonsPanel, JLayeredPane.POPUP_LAYER);

        // ── Resize listener — keep all layers filling the frame ────────
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionLayers(layeredPane, backgroundPanel,
                        columnsPanel, titlePanel, buttonsPanel);
            }
        });

        // Initial position
        repositionLayers(layeredPane, backgroundPanel, columnsPanel, titlePanel, buttonsPanel);

        // ── Button actions ─────────────────────────────────────────────
        arcadeButton.addActionListener(ev -> { new ArcadeMode(); dispose(); });
        PVEButton.addActionListener(   ev -> { new PVEMode();    dispose(); });
        PVPButton.addActionListener(   ev -> { new PVPMode();    dispose(); });
        exitGameButton.addActionListener(ev -> {new WelcomeScreenPage(); dispose();});

        setVisible(true);

// ✅ Reposition AFTER the frame has been painted and has real dimensions
        SwingUtilities.invokeLater(() ->
                repositionLayers(layeredPane, backgroundPanel, columnsPanel, titlePanel, buttonsPanel)
        );
    }

    // ── Reposition all layers to fill current frame size ──────────────

    private void repositionLayers(JLayeredPane pane,
                                  JPanel bg, JPanel columns,
                                  JPanel title, JPanel buttons) {
        int w = pane.getWidth();
        int h = pane.getHeight();
        if (w == 0 || h == 0) return;

        // Background + columns fill everything
        bg.setBounds(0, 0, w, h);
        columns.setBounds(0, 0, w, h);

        // Title: top-center, 50% width
        int titleW = (int)(w * 0.50);
        int titleH = (int)(titleW * 0.22);
        int titleX = (w - titleW) / 2;
        int titleY = (int)(h * 0.06);
        title.setBounds(titleX, titleY, titleW, titleH);

        // ✅ Buttons: scale width to 28% of frame, height auto
        int btnW = (int)(w * 0.28);   // e.g. 286px at 1024 wide
        int btnH = (int)(btnW * 0.22); // proportional height ~63px

        // ✅ Update each button's size to match the scaled width
        arcadeButton.setPreferredSize(new Dimension(btnW, btnH));
        arcadeButton.setMaximumSize(new Dimension(btnW, btnH));
        PVEButton.setPreferredSize(new Dimension(btnW, btnH));
        PVEButton.setMaximumSize(new Dimension(btnW, btnH));
        PVPButton.setPreferredSize(new Dimension(btnW, btnH));
        PVPButton.setMaximumSize(new Dimension(btnW, btnH));
        exitGameButton.setPreferredSize(new Dimension(btnW, (int)(btnH * 0.75)));
        exitGameButton.setMaximumSize(new Dimension(btnW, (int)(btnH * 0.75)));

        // Panel: centered, tall enough to hold all 4 buttons + gaps
        int btnPanelH = (btnH * 3) + (int)(btnH * 0.75) + (22 * 3) + 40;
        int btnPanelW = btnW + 20;
        int btnPanelX = (w - btnPanelW) / 2;
        int btnPanelY = (int)(h * 0.30);
        buttons.setBounds(btnPanelX, btnPanelY, btnPanelW, btnPanelH);

        buttons.revalidate();
        buttons.repaint();
    }

    // ── Helper: wrap button in a centered flow panel ──────────────────

    private JPanel centerButton(JButton btn) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(btn);
        return wrapper;
    }

    // ── Image button factory ──────────────────────────────────────────

    private JButton createImageButton(String imagePath, String fallbackText) {
        JButton btn = new JButton() {
            private Image img = loadImage(imagePath);
            private Image hoverImg = null; // optional: add a hover variant later

            {
                // Slightly brighten on hover
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
                    // Hover: slight brightness boost
                    if (getModel().isRollover()) {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
                    }
                    g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Fallback if image missing
                    g2.setColor(new Color(60, 60, 70, 200));
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
        btn.setPreferredSize(new Dimension(300, 70));
        btn.setMaximumSize(new Dimension(300, 70));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Plain styled exit button ──────────────────────────────────────

    private JButton createTextButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = getModel().isRollover()
                        ? new Color(180, 60, 60, 210)
                        : new Color(120, 40, 40, 180);
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(260, 44));
        btn.setMaximumSize(new Dimension(260, 44));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Image loader utility ──────────────────────────────────────────

    private Image loadImage(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.err.println("Image not found: " + path);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // ── Inner: Full-stretch background panel ─────────────────────────

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

    // ── Inner: Transparent overlay panel (columns, title) ────────────

    private class ScaledImagePanel extends JPanel {
        private final Image img;

        ScaledImagePanel(String path) {
            img = loadImage(path);
            setOpaque(false); // transparent so layers below show through
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

    // ── createUIComponents (required by IntelliJ form — leave empty) ──
    private void createUIComponents() {
        // Not used — UI built programmatically above
    }
}