package encantadia;

import encantadia.gamemode.GameModeType;
import encantadia.story.*;
import encantadia.ui.frames.CharacterSelectionFrame;
import encantadia.ui.frames.MainMenuFrame;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class BackstoryShowcase extends JFrame {

    private static final String BG_PATH        = "/resources/background (3).png";
    private static final String PARCHMENT_PATH = "/resources/base1SBS.png";

    private static final int PARAGRAPHS_PER_PAGE = 10;
    private static final int CHAR_DELAY_MS       = 14;

    private static final double PARCHMENT_WIDTH_RATIO  = 0.60;
    private static final double PARCHMENT_HEIGHT_RATIO = 0.75;

    private static final Color INK_DARK = new Color(0x3A, 0x18, 0x04);
    private static final Color INK_MID  = new Color(0x5C, 0x2E, 0x08);
    private static final Color INK_GOLD = new Color(0x8B, 0x60, 0x20);
    private static final Color ORNAMENT = new Color(0xA0, 0x72, 0x28);

    private JTextPane storyPane;
    private JButton continueButton;
    private JButton skipButton;
    private JLabel pageLabel;

    private JPanel parchmentPanel;
    private JPanel innerPanel;

    private JLayeredPane layeredPane;
    private JPanel centerWrapper;

    private final String[] paragraphs;
    private final String storyTitle;
    private final Runnable onFinish;

    private final int totalPages;
    private int currentPage = 0;

    private int fontSize = 8;
    private int titleSize = 19;

    private volatile Thread animThread = null;

    // ================= CONSTRUCTORS =================

    public BackstoryShowcase(StoryType storyType) {
        this(storyType, null);
    }

    public BackstoryShowcase(StoryType storyType, GameModeType gameModeType) {
        this(
                GameStories.getParagraphs(storyType),
                GameStories.getTitle(storyType),
                () -> {
                    if (storyType == StoryType.GAME_LORE) {
                        new MainMenuFrame();
                    } else {
                        new CharacterSelectionFrame(
                                gameModeType != null ? gameModeType : GameModeType.PVE
                        );
                    }
                }
        );
    }

    public BackstoryShowcase(String[] paragraphs, String title, Runnable onFinish) {
        this.paragraphs = paragraphs;
        this.storyTitle = title;
        this.onFinish = onFinish;
        this.totalPages = (int) Math.ceil((double) paragraphs.length / PARAGRAPHS_PER_PAGE);

        setTitle("Encantadia – " + title);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);

        // ================= LAYERED UI =================
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        setContentPane(layeredPane);

        // Background
        BgPanel bg = new BgPanel(BG_PATH);
        bg.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(bg, Integer.valueOf(0));

        // Build parchment UI
        buildParchmentPanel();

        // Center wrapper
        centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBounds(0, 0, getWidth(), getHeight());
        centerWrapper.add(parchmentPanel);

        layeredPane.add(centerWrapper, Integer.valueOf(1));

        // Resize handling
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = getWidth();
                int h = getHeight();

                bg.setBounds(0, 0, w, h);
                centerWrapper.setBounds(0, 0, w, h);

                updateLayout();
            }
        });

        ScreenManager.register(this);
        setVisible(true);

        SwingUtilities.invokeLater(() -> {
            updateLayout();
            animatePage(0);
        });
    }

    @Override
    public void dispose() {
        ScreenManager.unregister(this);
        super.dispose();
    }

    // ================= UI BUILD =================

    private void buildParchmentPanel() {
        parchmentPanel = new BgPanel(PARCHMENT_PATH);
        parchmentPanel.setLayout(new BorderLayout());
        parchmentPanel.setOpaque(false);

        storyPane = new JTextPane();
        storyPane.setContentType("text/html");
        storyPane.setEditable(false);
        storyPane.setOpaque(false);
        storyPane.setBackground(new Color(0, 0, 0, 0));
        storyPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane scroll = new JScrollPane(storyPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        styleScrollBar(scroll.getVerticalScrollBar());

        pageLabel = new JLabel("", SwingConstants.LEFT);
        pageLabel.setFont(new Font("Serif", Font.ITALIC, 8));
        pageLabel.setForeground(ORNAMENT);

        continueButton = makeStyledButton("Continue »");
        continueButton.setEnabled(false);
        continueButton.addActionListener(e -> handleContinue());

        skipButton = makeStyledButton("Skip »»");
        skipButton.addActionListener(e -> handleSkip());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(skipButton);
        rightButtons.add(continueButton);

        JPanel btnRow = new JPanel(new BorderLayout());
        btnRow.setOpaque(false);
        btnRow.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        btnRow.add(pageLabel, BorderLayout.WEST);
        btnRow.add(rightButtons, BorderLayout.EAST);

        innerPanel = new JPanel(new BorderLayout(0, 4));
        innerPanel.setOpaque(false);
        innerPanel.add(scroll, BorderLayout.CENTER);
        innerPanel.add(btnRow, BorderLayout.SOUTH);

        parchmentPanel.add(innerPanel, BorderLayout.CENTER);
    }

    // ================= LAYOUT =================

    private void updateLayout() {
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        double scale = Math.min(w / 1024.0, h / 768.0);
        scale = Math.min(scale, 1.4);

        int parchmentW = (int)(w * PARCHMENT_WIDTH_RATIO);
        int parchmentH = (int)(h * PARCHMENT_HEIGHT_RATIO);

        parchmentPanel.setPreferredSize(new Dimension(parchmentW, parchmentH));
        parchmentPanel.setMaximumSize(new Dimension(parchmentW, parchmentH));
        parchmentPanel.setMinimumSize(new Dimension(parchmentW, parchmentH));

        int hPad = (int)(w * 0.08);
        int vTop = (int)(parchmentH * 0.06);
        int vBot = (int)(parchmentH * 0.035);

        innerPanel.setBorder(BorderFactory.createEmptyBorder(vTop, hPad, vBot, hPad));

        int btnW = (int)(140 * scale);
        int btnH = (int)(40 * scale);

        continueButton.setPreferredSize(new Dimension(btnW, btnH));
        skipButton.setPreferredSize(new Dimension(btnW, btnH));

        fontSize = (int)(11 * scale);
        titleSize = (int)(fontSize * 1.35);

        if (animThread == null || !animThread.isAlive()) {
            renderFull(currentPage, fontSize, titleSize);
        }

        revalidate();
        repaint();
    }

    // ================= BACKGROUND =================

    private class BgPanel extends JPanel {
        private final Image img;

        BgPanel(String path) {
            URL url = getClass().getResource(path);
            img = (url != null) ? new ImageIcon(url).getImage() : null;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }

    // ================= ANIMATION =================

    private void animatePage(int page) {
        currentPage = page;
        stopAnimThread();

        continueButton.setEnabled(false);
        continueButton.setText(isLastPage() ? "Begin »" : "Continue »");
        pageLabel.setText("  Page " + (page + 1) + " of " + totalPages);

        final int fs = fontSize;
        final int ts = titleSize;

        int from = page * PARAGRAPHS_PER_PAGE;
        int to = Math.min(from + PARAGRAPHS_PER_PAGE, paragraphs.length);

        StringBuilder body = new StringBuilder();
        for (int i = from; i < to; i++) body.append(paragraphs[i]);

        final String fullHtml = buildPageHtml(body.toString(), page == 0, fs, ts);

        final int bodyStart = fullHtml.indexOf("<body>") + 6;
        final int bodyEnd = fullHtml.lastIndexOf("</body>");

        final String prefix = fullHtml.substring(0, bodyStart);
        final String content = fullHtml.substring(bodyStart, bodyEnd);
        final String suffix = "</body></html>";

        animThread = new Thread(() -> {
            try {
                StringBuilder visible = new StringBuilder();
                boolean inTag = false;

                for (int i = 0; i < content.length(); i++) {
                    if (Thread.currentThread().isInterrupted()) return;

                    char c = content.charAt(i);
                    visible.append(c);

                    if (c == '<') { inTag = true; continue; }
                    if (c == '>') { inTag = false; continue; }
                    if (inTag) continue;

                    final String snap = prefix + visible + suffix;

                    SwingUtilities.invokeLater(() -> {
                        storyPane.setText(snap);
                        storyPane.setCaretPosition(storyPane.getDocument().getLength());
                    });

                    if (c == '.' || c == '!' || c == '?') {
                        Thread.sleep(220);
                    } else if (c == ',') {
                        Thread.sleep(120);
                    } else {
                        Thread.sleep(CHAR_DELAY_MS);
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    storyPane.setText(fullHtml);
                    storyPane.setCaretPosition(0);
                    continueButton.setEnabled(true);
                });

            } catch (InterruptedException ignored) {}
        });

        animThread.setDaemon(true);
        animThread.start();
    }

    private void stopAnimThread() {
        if (animThread != null && animThread.isAlive()) {
            animThread.interrupt();
            try { animThread.join(400); } catch (InterruptedException ignored) {}
        }
        animThread = null;
    }

    private void renderFull(int page, int fs, int ts) {
        int from = page * PARAGRAPHS_PER_PAGE;
        int to = Math.min(from + PARAGRAPHS_PER_PAGE, paragraphs.length);

        StringBuilder body = new StringBuilder();
        for (int i = from; i < to; i++) body.append(paragraphs[i]);

        storyPane.setText(buildPageHtml(body.toString(), page == 0, fs, ts));
        storyPane.setCaretPosition(0);
    }

    private void handleSkip() {
        if (animThread != null && animThread.isAlive()) {
            stopAnimThread();
            renderFull(currentPage, fontSize, titleSize);
            continueButton.setEnabled(true);
        }
    }

    private void handleContinue() {
        if (isLastPage()) proceed();
        else animatePage(currentPage + 1);
    }

    private boolean isLastPage() {
        return currentPage >= totalPages - 1;
    }

    private void proceed() {
        dispose();
        if (onFinish != null) onFinish.run();
    }

    // ================= HTML =================

    private String buildPageHtml(String body, boolean showTitle, int fs, int ts) {
        String inkDark = toHex(INK_DARK);
        String inkMid = toHex(INK_MID);
        String inkGold = toHex(INK_GOLD);
        String ornament = toHex(ORNAMENT);

        String divider = "<div class=\"divider\">&#x2015;&#x2015;&#x2015; &#x2726; &#x2015;&#x2015;&#x2015;</div>";
        String titleBlock = showTitle ? "<h1>" + storyTitle + "</h1>" + divider : "";
        String styledBody = body;

        return "<html><head><style>"
                + "body{font-family:Georgia,serif;font-size:" + fs + "px;color:" + inkDark + ";margin:0;padding:0;line-height:1.6;text-align:justify;}"
                + "h1{font-size:" + ts + "px;color:" + inkMid + ";text-align:center;}"
                + ".divider{text-align:center;color:" + ornament + ";margin:6px 0;}"
                + "</style></head><body>"
                + titleBlock + styledBody
                + "</body></html>";
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    // ================= SCROLLBAR =================

    private void styleScrollBar(JScrollBar bar) {
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(6, 0));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0x8B, 0x5E, 0x3C, 180);
                trackColor = new Color(0, 0, 0, 0);
            }
            @Override protected JButton createDecreaseButton(int o) { return new JButton(); }
            @Override protected JButton createIncreaseButton(int o) { return new JButton(); }
        });
    }

    // ================= BUTTON =================

    private JButton makeStyledButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            private boolean pressed = false;

            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setOpaque(false);

                setForeground(new Color(245, 235, 220));
                setFont(new Font("Serif", Font.BOLD, 14));

                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = false;
                        repaint();
                    }

                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        pressed = true;
                        repaint();
                    }

                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) {
                        pressed = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                int arc = 20;

                // Base gradient colors
                Color top = INK_MID;
                Color bottom = INK_DARK;

                if (hover) {
                    top = INK_GOLD;
                    bottom = INK_MID;
                }

                if (pressed) {
                    top = INK_DARK;
                    bottom = new Color(0x2A, 0x10, 0x03);
                }

                GradientPaint gp = new GradientPaint(
                        0, 0, top,
                        0, height, bottom
                );

                // Fill rounded background
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, width, height, arc, arc);

                // Border glow
                g2.setColor(ORNAMENT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, width - 3, height - 3, arc, arc);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        return btn;
    }
}
